FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /temp
ADD  own-send-server-1.0-ALPHA.jar send.jar
ENTRYPOINT ["java","-Xmx200m","-jar","/send.jar","-Djava.security.egd=file:/dev/./urandom"]

EXPOSE 6003