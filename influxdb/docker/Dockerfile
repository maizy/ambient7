FROM phusion/baseimage:latest
MAINTAINER Nikita Kovalev <nikita@maizy.ru>

# Install InfluxDB
ENV INFLUXDB_VERSION 0.9.5.1
RUN curl -s -o /tmp/influxdb_latest_amd64.deb https://s3.amazonaws.com/influxdb/influxdb_${INFLUXDB_VERSION}_amd64.deb && \
  dpkg -i /tmp/influxdb_latest_amd64.deb && \
  rm /tmp/influxdb_latest_amd64.deb && \
  rm -rf /var/lib/apt/lists/*

ADD types.db /usr/share/collectd/types.db
ADD config.toml /config/config.toml

RUN mkdir -p /etc/service/influxdb
ADD run /etc/service/influxdb/run
RUN chmod +x /etc/service/influxdb/run

ENV PRE_CREATE_DB **None**
ENV SSL_SUPPORT **False**
ENV SSL_CERT **None**

# Admin server WebUI
EXPOSE 8083

# HTTP API
EXPOSE 8086

# Raft port (for clustering, don't expose publicly!)
#EXPOSE 8090

# Protobuf port (for clustering, don't expose publicly!)
#EXPOSE 8099

VOLUME ["/data"]

CMD ["/sbin/my_init"]
