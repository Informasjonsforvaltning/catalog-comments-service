FROM eclipse-temurin:21-jre-alpine

ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

VOLUME /tmp
COPY /target/app.jar app.jar

CMD ["sh", "-c", "java -jar -XX:+UseZGC $JAVA_OPTS app.jar"]
