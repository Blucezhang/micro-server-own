FROM eclipse-temurin:8-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*
COPY own-workflow/target/own-workflow-1.0-ALPHA.jar /app.jar
ENTRYPOINT ["java", "-Xmx200m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
EXPOSE 6008
