FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /tmp
ADD own-api-gateway-1.0-ALPHA.jar gateway.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Xmx200m","/gateway.jar"]
EXPOSE 8002