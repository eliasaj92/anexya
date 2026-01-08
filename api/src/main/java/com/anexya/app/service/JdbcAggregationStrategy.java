package com.anexya.app.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile("mysql")
@RequiredArgsConstructor
public class JdbcAggregationStrategy implements AggregationStrategy
{

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<TagSummary> summarizeByTag(Instant startDate, Instant endDate, Optional<String> siteName,
            Optional<String> epc)
    {
        StringBuilder sql = new StringBuilder();
        sql.append("select epc, count(*) as totalReadCount, avg(rssi) as averageRssi, max(rssi) as peakRssi, ");
        sql.append("count(distinct location) as locationCount, ");
        sql.append("min(read_at) as firstSeen, max(read_at) as lastSeen, ");
        sql.append("(select location from tag_reads rt2 where rt2.epc = rt.epc ");
        sql.append(" and rt2.read_at between ? and ? ");
        sql.append(siteName.isPresent() ? " and rt2.site_name = ? " : " ");
        sql.append(epc.isPresent() ? " and rt2.epc = ? " : " ");
        sql.append(" group by location order by count(*) desc, location asc limit 1) as mostDetectedLocation ");
        sql.append("from tag_reads rt where read_at between ? and ? ");
        if (siteName.isPresent())
        {
            sql.append(" and site_name = ? ");
        }
        if (epc.isPresent())
        {
            sql.append(" and epc = ? ");
        }
        sql.append(" group by epc");

        List<Object> params = new ArrayList<>();
        // subquery params
        params.add(Timestamp.from(startDate));
        params.add(Timestamp.from(endDate));
        siteName.ifPresent(params::add);
        epc.ifPresent(params::add);
        // main query params
        params.add(Timestamp.from(startDate));
        params.add(Timestamp.from(endDate));
        siteName.ifPresent(params::add);
        epc.ifPresent(params::add);

        return jdbcTemplate.query(sql.toString(), params.toArray(),
                (rs, rowNum) -> new TagSummary(rs.getString("epc"), rs.getLong("totalReadCount"),
                        rs.getDouble("averageRssi"), rs.getDouble("peakRssi"), rs.getLong("locationCount"),
                        rs.getString("mostDetectedLocation"), rs.getTimestamp("firstSeen").toInstant(),
                        rs.getTimestamp("lastSeen").toInstant()));
    }
}
