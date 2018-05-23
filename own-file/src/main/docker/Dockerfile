FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /temp
ADD  own-file-1.0-ALPHA.jar file.jar
ENTRYPOINT ["java","-Xmx200m","-jar","-Djava.security.egd=file:/dev/./urandom","/file.jar"]

EXPOSE 9632