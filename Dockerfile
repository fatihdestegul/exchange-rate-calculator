FROM maven:3.6.3-adoptopenjdk-11 as build
# Speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

WORKDIR /opt/app

# Copy respources
COPY pom.xml .
RUN mvn dependency:go-offline
COPY ./src ./src

# Compile the source code and package it in a jar file
RUN mvn clean package -Dmaven.test.skip=true

FROM adoptopenjdk:11-jre-hotspot as runtime
WORKDIR /opt/app

# copy over the built artifact from the maven image
COPY --from=build /opt/app/target/internal-0.0.1-SNAPSHOT.jar /opt/app
ENTRYPOINT ["java", "-jar", "/opt/app/internal-0.0.1-SNAPSHOT.jar"]