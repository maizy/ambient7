FROM phusion/baseimage:latest
MAINTAINER Nikita Kovalev <nikita@maizy.ru>

# Install InfluxDB
RUN curl -s -o /tmp/influxdb_latest_amd64.deb https://dl.influxdata.com/influxdb/releases/influxdb_0.11.1-1_amd64.deb && \
  dpkg -i /tmp/influxdb_latest_amd64.deb && \
  rm /tmp/influxdb_latest_amd64.deb && \
  rm -rf /var/lib/apt/lists/*

ADD config.toml /config/config.toml

RUN mkdir -p /etc/service/influxdb

VOLUME ["/data"]

CMD ["/sbin/my_init"]
