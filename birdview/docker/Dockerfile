FROM openjdk:8-jre-slim

ARG RX_EXTRA_JAVA_OPTS
# Copy server
COPY . /opt/birdview/

# Path to config
RUN mkdir /config
ENV CONFIG_LOCATION="/config"

ENTRYPOINT ["/opt/birdview/birdview.sh"]