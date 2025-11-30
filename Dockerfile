FROM payara/server-full:6.2023.12

# Copy the PostgreSQL JDBC driver (download it instead of relying on target/)
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar /opt/payara/appserver/glassfish/domains/domain1/lib/

# Copy your WAR to the Payara deployments directory
COPY target/cargo-tracker.war /opt/payara/deployments/ROOT.war

# Copy Payara post-boot commands if needed (optional)
# COPY post-boot-commands.asadmin /opt/payara/config/
