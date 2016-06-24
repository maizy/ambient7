create table "co2_hourly_report" (
  co2_hourly_report_id identity primary key not null,
  day date not null ,
  hour smallint not null,
  low_level_total int null,
  middle_level_total int null,
  high_level_total int null
);

create unique index on "co2_hourly_report" (day, hour);
