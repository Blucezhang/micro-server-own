FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /temp
ADD  own-promotion-1.0-ALPHA.jar promotion.jar
ENTRYPOINT ["java","-Xmx200m","-jar","/promotion.jar","-Djava.security.egd=file:/dev/./urandom"]

EXPOSE 6005