create table if not exists tag_reads (
	id char(36) not null,
	site_name varchar(255) not null,
	epc varchar(255) not null,
	reference_code varchar(255) not null,
	location varchar(255) not null,
	rssi double not null,
	read_at timestamp(6) not null,
	primary key (id)
);

create index if not exists idx_tag_reads_epc on tag_reads (epc);
create index if not exists idx_tag_reads_site_name on tag_reads (site_name);
create index if not exists idx_tag_reads_read_at on tag_reads (read_at);
