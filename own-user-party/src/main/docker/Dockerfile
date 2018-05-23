FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /temp
ADD  own-user-party-1.0-ALPHA.jar user.jar
ENTRYPOINT ["java","-Xmx200m","-jar","/user.jar","-Djava.security.egd=file:/dev/./urandom"]

EXPOSE 6006