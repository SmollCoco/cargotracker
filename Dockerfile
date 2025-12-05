FROM payara/server-full:6.2023.12

USER root

# 1. Install Utils & Postgres Driver
RUN apt-get update && apt-get install -y curl dos2unix
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar
RUN chown payara:payara /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar \
    && chmod 644 /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar

# 2. Copy Config Script & Fix Line Endings (Crucial!)
COPY post-boot-commands.asadmin /opt/payara/config/post-boot-commands.asadmin
RUN dos2unix /opt/payara/config/post-boot-commands.asadmin \
    && chown payara:payara /opt/payara/config/post-boot-commands.asadmin \
    && chmod 644 /opt/payara/config/post-boot-commands.asadmin

# 3. FORCE Payara to see the script
ENV POSTBOOT_COMMANDS=/opt/payara/config/post-boot-commands.asadmin

# 4. Copy App
COPY target/cargo-tracker.war /opt/payara/deployments/ROOT.war
RUN chown payara:payara /opt/payara/deployments/ROOT.war

USER payara
