FROM payara/server-full:6.2023.12

# 1. Switch to Root to install dependencies
USER root

# 2. Download Postgres Driver (Using curl to be explicit)
# We rename it to 'postgresql.jar' to keep it simple
RUN curl -L -o /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar \
    https://jdbc.postgresql.org/download/postgresql-42.7.2.jar

# 3. CRITICAL: Fix Permissions so the 'payara' user can read it
RUN chown -R payara:payara /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar \
    && chmod 644 /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql.jar

# 4. Copy Config Script (and fix permissions)
COPY post-boot-commands.asadmin /opt/payara/config/post-boot-commands.asadmin
RUN chown payara:payara /opt/payara/config/post-boot-commands.asadmin

# 5. Copy App (and fix permissions)
COPY target/cargo-tracker.war /opt/payara/deployments/ROOT.war
RUN chown payara:payara /opt/payara/deployments/ROOT.war

# 6. Switch back to Payara user for runtime
USER payara
