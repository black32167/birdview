FROM openjdk:8-jre-slim

# Copy server
COPY . /opt/birdview/

# Path to config
RUN mkdir /config
ENV CONFIG_LOCATION="/config"

ENTRYPOINT ["/opt/birdview/birdview.sh"]