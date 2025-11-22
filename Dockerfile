FROM payara/server-full:6.2023.12

# Download PostgreSQL driver
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar /tmp/

COPY target/*.war /tmp/
COPY post-boot-commands.asadmin /opt/payara/config/
