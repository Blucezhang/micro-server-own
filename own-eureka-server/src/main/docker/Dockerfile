FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>


VOLUME /tmp
ADD own-eureka-server-1.0-ALPHA.jar eureka.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Xmx200m","/eureka.jar"]
EXPOSE 8002