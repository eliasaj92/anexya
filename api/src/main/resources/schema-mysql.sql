-- Range partitions by quarter (RDS-friendly, DATETIME column)
create table if not exists tag_reads (
    id char(36) not null,
    site_name varchar(255) not null,
    epc varchar(255) not null,
    reference_code varchar(1024) not null,
    location varchar(255) not null,
    rssi double not null,
    read_at datetime(6) not null,
    primary key (id)
)
partition by range columns (read_at) (
    partition p2024q1 values less than ('2024-04-01 00:00:00'),
    partition p2024q2 values less than ('2024-07-01 00:00:00'),
    partition p2024q3 values less than ('2024-10-01 00:00:00'),
    partition p2024q4 values less than ('2025-01-01 00:00:00'),
    partition p2025q1 values less than ('2025-04-01 00:00:00'),
    partition p2025q2 values less than ('2025-07-01 00:00:00'),
    partition p2025q3 values less than ('2025-10-01 00:00:00'),
    partition p2025q4 values less than ('2026-01-01 00:00:00'),
    partition p2026q1 values less than ('2026-04-01 00:00:00'),
    partition p2026q2 values less than ('2026-07-01 00:00:00'),
    partition p2026q3 values less than ('2026-10-01 00:00:00'),
    partition p2026q4 values less than ('2027-01-01 00:00:00'),
    partition pmax   values less than (maxvalue)
);
