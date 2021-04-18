create schema skyscraper2;

create table skyscraper2.satellites
(
	id serial not null
		constraint satellites_pk
			primary key,
	dateadded TIMESTAMP default CURRENT_TIMESTAMP not null,
	lastseen timestamp default CURRENT_TIMESTAMP not null,
	name VARCHAR(24),
	adapter int,
	diseqc int,
	orbitalposition double precision not null,
	west int not null
);

create table skyscraper2.transponder
(
	id serial not null
		constraint transponder_pk
			primary key,
	dateadded timestamp default current_timestamp not null,
	lastseen timestamp default current_timestamp not null,
	frequency bigint not null,
	satellite int not null
		constraint transponder_satellites_id_fk
			references skyscraper2.satellites,
	polarization VARCHAR(10) not null,
	rolloff double precision,
	modulationsystem VARCHAR(12) not null,
	modulationtype VARCHAR(8) not null,
	symbolrate bigint not null,
	fec varchar(4) not null
);

