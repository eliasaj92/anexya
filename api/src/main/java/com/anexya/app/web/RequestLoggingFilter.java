package com.anexya.app.web;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.anexya.app.cloud.CloudServiceFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Component
// Run early (but after critical built-ins) so we wrap the full request/response for timing and status logging.
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private final ObjectProvider<CloudServiceFactory> cloudFactory;

    public RequestLoggingFilter(ObjectProvider<CloudServiceFactory> cloudFactory) {
        this.cloudFactory = cloudFactory;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final Instant start = Instant.now();
        final StatusCapturingResponse wrapper = new StatusCapturingResponse(response);
        final CloudServiceFactory cloud = cloudFactory.getIfAvailable();
        try {
            filterChain.doFilter(request, wrapper);
        } catch (Exception ex) {
            if (cloud != null) {
                cloud.logger()
                     .ifPresent(logger -> logger.log("request_exception",
                                                     Map.of("method",
                                                            request.getMethod(),
                                                            "path",
                                                            request.getRequestURI(),
                                                            "message",
                                                            ex.getMessage() == null ? "" : ex.getMessage())));
                cloud.metrics()
                     .ifPresent(metrics -> metrics.increment("http.requests.errors", 1.0, Map.of("method", request.getMethod(), "status", "500")));
            }
            throw ex;
        } finally {
            final int status = wrapper.getStatus();
            final Duration duration = Duration.between(start, Instant.now());
            if (cloud != null) {
                cloud.metrics()
                     .ifPresent(metrics -> metrics.recordDuration("http.requests.duration", duration, Map.of("method", request.getMethod(), "status", String.valueOf(status))));

                if (HttpStatus.valueOf(status)
                              .is5xxServerError()) {
                    cloud.logger()
                         .ifPresent(logger -> logger.log("request_5xx", Map.of("method", request.getMethod(), "path", request.getRequestURI(), "status", String.valueOf(status))));
                    cloud.metrics()
                         .ifPresent(metrics -> metrics.increment("http.requests.5xx", 1.0, Map.of("method", request.getMethod())));
                }
            }
        }
    }

    private static class StatusCapturingResponse extends HttpServletResponseWrapper {
        private int httpStatus = HttpServletResponse.SC_OK;

        StatusCapturingResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.httpStatus = sc;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.httpStatus = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.httpStatus = sc;
        }

        @Override
        public int getStatus() {
            return this.httpStatus;
        }
    }
}
