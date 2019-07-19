FROM adoptopenjdk/openjdk11:jdk-11.0.3_7-alpine

RUN apk update
RUN apk add ca-certificates wget gnupg && update-ca-certificates