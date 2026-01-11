-- Test schema without partitioning for MySQL container

create table if not exists tag_reads (
    id char(36) not null primary key,
    site_name varchar(255) not null,
    epc varchar(255) not null,
    reference_code varchar(1024) not null,
    location varchar(255) not null,
    rssi double not null,
    read_at datetime(6) not null
);

create index idx_tag_reads_epc_read_at on tag_reads (epc, read_at);
create index idx_tag_reads_site_read_at on tag_reads (site_name, read_at);
create index idx_tag_reads_location_read_at on tag_reads (location, read_at);
