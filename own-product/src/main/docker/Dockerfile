FROM java:8-jre
MAINTAINER Blucezhang <Blucezhang@outlook.com>

VOLUME /temp
ADD  own-product-1.0-ALPHA.jar product.jar
ENTRYPOINT ["java","-Xmx200m","-jar","/product.jar","-Djava.security.egd=file:/dev/./urandom"]

EXPOSE 6007