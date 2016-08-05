create table "co2_hourly_report" (
  co2_hourly_report_id identity primary key not null,

  day                  date                 not null,
  hour                 smallint             not null,
  tags_ordered         varchar(3072)        not null default '',
  agent_name           varchar(255)         not null,

  low_level_total      int                  null,
  medium_level_total   int                  null,
  high_level_total     int                  null,
  unknown_level_total  int                  null
);

create unique index "unique_record"
  on "co2_hourly_report" (day, hour, tags_ordered, agent_name);

create index "aggregate_ident"
  on "co2_hourly_report" (tags_ordered, agent_name);
