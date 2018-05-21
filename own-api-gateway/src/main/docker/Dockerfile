FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

ADD  ../../target/own-api-gateway.jar /app/
CMD ["java","-Xmx200m","-jar","/app/own-api-gateway.jar"]

EXPOSE 9632