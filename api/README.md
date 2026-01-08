# Tag Read API (Java 21, Spring Boot)

Capture tag read events (site, EPC, reference code, location, RSSI, timestamp) and summarize them by EPC. Runs with either an in-memory repository (default) or MySQL (`mysql` profile).

## Quick start
```bash
./gradlew test
./gradlew bootRun                       # in-memory profile
# or
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun  # uses MySQL; see application-mysql.yml
```

API listens on port 8080 by default.

## Endpoints
- `POST /api/tag-reads` — create a tag read
	```json
	{
		"siteName": "Plant A",
		"epc": "EPC123",
		"referenceCode": "REF1",
		"location": "Dock 1",
		"rssi": -45.5,
		"readAt": "2024-01-01T12:00:00Z"
	}
	```
- `GET /api/tag-reads` — list all tag reads
- `GET /api/tag-reads/{id}` — fetch a tag read by id
- `GET /api/tag-reads/summary/by-epc?startDate=...&endDate=...&siteName=...&epc=...` — aggregate reads by EPC (count, average/peak RSSI, locations, first/last seen)

## Profiles and storage
- **default / inmemory**: in-memory repository; good for local dev/tests.
- **mysql**: uses `JdbcTagReadRepository` and `JdbcAggregationStrategy`. Schema auto-applied from `schema-mysql.sql` (table `tag_reads` with helpful indexes). Configure connection via `application-mysql.yml` or env vars `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD`.

## Packaging & container
```bash
./gradlew bootJar
docker build -t anexya-api:local .
```

## Tech notes
- Spring Boot 3.2, Java 21 toolchain
- Lombok for DTO/domain boilerplate
- Virtual threads enabled via `spring.threads.virtual.enabled=true`
- MockMvc test in `src/test/java`
