FROM debian:testing

MAINTAINER lww336@cs.scut.edu

RUN apt-get update && apt-get install -y --no-install-recommends \
  curl git golang openjdk-8-jdk-headless procps \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

LABEL service-type=system
LABEL arg.memory=512M
LABEL arg.restart=always
LABEL arg.ports=8761:8761

ENV JAVA_OPTS=" -Xms64M -Xmx512M -Xss256k -XX:+HeapDumpOnOutOfMemoryError "

EXPOSE 8761

RUN mkdir /data
WORKDIR /data
VOLUME  /data

ADD ./@artifactId@-*-boot.jar /@artifactId@-@version@.jar
ENTRYPOINT java -server -noverify $JAVA_OPTS -jar /@artifactId@-@version@.jar \
   --spring.profiles.active=staging