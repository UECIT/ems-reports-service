#!/usr/bin/env sh
echo "Starting a Spring Boot CDS Reports Service on $HOSTNAME"
exec java -jar ems-reports-service.war
