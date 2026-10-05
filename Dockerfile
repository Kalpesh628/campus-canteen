# Campus Canteen — multi-stage Docker build for Railway (or any host).
# Stage 1 builds the WAR with Maven; stage 2 runs it on Tomcat 10 (Jakarta).
# DB config comes from env vars (Railway MySQL plugin provides
# MYSQLHOST/MYSQLPORT/MYSQLDATABASE/MYSQLUSER/MYSQLPASSWORD) — see DBUtil.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Copy sources and build. Tests are skipped for deploy speed.
COPY src ./src
RUN mvn -q package -DskipTests

FROM tomcat:10.1-jdk17-temurin
# Deploy as the root app so the site lives at "/" instead of "/campus-canteen".
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/campus-canteen.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
# Railway assigns the port via $PORT; rewrite Tomcat's connector accordingly,
# then start. Locally (no $PORT) it stays on 8080.
CMD sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/" /usr/local/tomcat/conf/server.xml && catalina.sh run
