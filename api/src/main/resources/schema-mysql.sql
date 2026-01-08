-- Partitioning notes:
--  - Monthly RANGE partitions on read_at for 2024–2026 plus a MAXVALUE catch-all (pmax).
--  - Add future partitions ahead of time (e.g., quarterly) to keep inserts off pmax.
--  - To change granularity after data exists, use a shadow table + rename, or REORGANIZE PARTITION per month.

create table if not exists tag_reads (
	id char(36) not null,
	site_name varchar(255) not null,
	epc varchar(255) not null,
	reference_code varchar(1024) not null,
	location varchar(255) not null,
	rssi double not null,
	read_at timestamp(6) not null,
	primary key (id)
)
partition by range columns (read_at) (
	partition p2024m01 values less than ('2024-02-01 00:00:00.000000'),
	partition p2024m02 values less than ('2024-03-01 00:00:00.000000'),
	partition p2024m03 values less than ('2024-04-01 00:00:00.000000'),
	partition p2024m04 values less than ('2024-05-01 00:00:00.000000'),
	partition p2024m05 values less than ('2024-06-01 00:00:00.000000'),
	partition p2024m06 values less than ('2024-07-01 00:00:00.000000'),
	partition p2024m07 values less than ('2024-08-01 00:00:00.000000'),
	partition p2024m08 values less than ('2024-09-01 00:00:00.000000'),
	partition p2024m09 values less than ('2024-10-01 00:00:00.000000'),
	partition p2024m10 values less than ('2024-11-01 00:00:00.000000'),
	partition p2024m11 values less than ('2024-12-01 00:00:00.000000'),
	partition p2024m12 values less than ('2025-01-01 00:00:00.000000'),
	partition p2025m01 values less than ('2025-02-01 00:00:00.000000'),
	partition p2025m02 values less than ('2025-03-01 00:00:00.000000'),
	partition p2025m03 values less than ('2025-04-01 00:00:00.000000'),
	partition p2025m04 values less than ('2025-05-01 00:00:00.000000'),
	partition p2025m05 values less than ('2025-06-01 00:00:00.000000'),
	partition p2025m06 values less than ('2025-07-01 00:00:00.000000'),
	partition p2025m07 values less than ('2025-08-01 00:00:00.000000'),
	partition p2025m08 values less than ('2025-09-01 00:00:00.000000'),
	partition p2025m09 values less than ('2025-10-01 00:00:00.000000'),
	partition p2025m10 values less than ('2025-11-01 00:00:00.000000'),
	partition p2025m11 values less than ('2025-12-01 00:00:00.000000'),
	partition p2025m12 values less than ('2026-01-01 00:00:00.000000'),
	partition p2026m01 values less than ('2026-02-01 00:00:00.000000'),
	partition p2026m02 values less than ('2026-03-01 00:00:00.000000'),
	partition p2026m03 values less than ('2026-04-01 00:00:00.000000'),
	partition p2026m04 values less than ('2026-05-01 00:00:00.000000'),
	partition p2026m05 values less than ('2026-06-01 00:00:00.000000'),
	partition p2026m06 values less than ('2026-07-01 00:00:00.000000'),
	partition p2026m07 values less than ('2026-08-01 00:00:00.000000'),
	partition p2026m08 values less than ('2026-09-01 00:00:00.000000'),
	partition p2026m09 values less than ('2026-10-01 00:00:00.000000'),
	partition p2026m10 values less than ('2026-11-01 00:00:00.000000'),
	partition p2026m11 values less than ('2026-12-01 00:00:00.000000'),
	partition p2026m12 values less than ('2027-01-01 00:00:00.000000'),
	partition pmax values less than (maxvalue)
);

create index if not exists idx_tag_reads_epc on tag_reads (epc);
create index if not exists idx_tag_reads_location on tag_reads (location);
create index if not exists idx_tag_reads_site_name on tag_reads (site_name);
create index if not exists idx_tag_reads_read_at on tag_reads (read_at);
