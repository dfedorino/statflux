FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests -Dcheckstyle.skip

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S statflux && adduser -S statflux -G statflux

COPY --from=builder /build/target/statflux-*-shaded.jar /app/statflux.jar

USER statflux

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "/app/statflux.jar"]
