FROM payara/server-full:6.2023.12

# 1. Download Postgres Driver to the correct library folder
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar /opt/payara/appserver/glassfish/domains/domain1/lib/

# 2. Copy the Config Script (Payara runs this automatically on startup)
COPY post-boot-commands.asadmin /opt/payara/config/post-boot-commands.asadmin

# 3. Deploy App as ROOT
COPY target/cargo-tracker.war /opt/payara/deployments/ROOT.war
