FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>


VOLUME /temp
ADD own-config-1.0-ALPHA.jar config.jar
ENTRYPOINT ["java","-Xmx200m","-jar","-Djava.security.egd=file:/dev/./urandom","/config.jar"]

EXPOSE 8001