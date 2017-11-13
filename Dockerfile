FROM java:8-jdk

WORKDIR /usr/src/app

RUN apt-get update && \
    apt-get -y install zip

VOLUME /root/.gradle
COPY . .
EXPOSE 8080

RUN ./gradlew assemble
CMD java -jar build/libs/es-0.0.1-SNAPSHOT.jar

