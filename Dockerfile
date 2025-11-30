FROM payara/server-full:6.2023.12

# 1. Switch to Root to perform setup
USER root

# 2. Download Postgres Driver using ADD (No curl required)
#    Docker automatically handles the download and renaming
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar

# 3. Set Permissions (Critical: ADD creates files as root, Payara needs to read them)
RUN chown payara:payara /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar \
    && chmod 644 /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar

# 4. Copy and fix permissions for Config Script
COPY post-boot-commands.asadmin /opt/payara/config/post-boot-commands.asadmin
RUN chown payara:payara /opt/payara/config/post-boot-commands.asadmin

# 5. Copy and fix permissions for App (ROOT.war)
COPY target/cargo-tracker.war /opt/payara/deployments/ROOT.war
RUN chown payara:payara /opt/payara/deployments/ROOT.war

# 6. Switch back to Payara user for security
USER payara
