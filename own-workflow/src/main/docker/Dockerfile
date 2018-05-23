FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /temp
ADD  own-workflow-1.0-ALPHA.jar workflow.jar
ENTRYPOINT ["java","-Xmx200m","-jar","/app/own-workflow-1.0-ALPHA.jar","-Djava.security.egd=file:/dev/./urandom"]

EXPOSE 6008