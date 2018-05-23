FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>
#RUN mkdir /app
#WORKDIR /app
#COPY ./target/own-eureka-server-1.0-ALPHA.jar /app
#CMD ["java","-Xmx200m","-jar","/app/own-eureka-server-1.0-ALPHA.jar"]


VOLUME /tmp
ADD own-eureka-server-1.0-ALPHA.jar eureka.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Xmx200m","/eureka.jar"]
#CMD [ "java", "-jar","-Xmx500m", "/app.jar" ]
EXPOSE 8002