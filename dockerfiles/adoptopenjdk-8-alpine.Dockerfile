FROM adoptopenjdk/openjdk8:jdk8u212-b04-alpine

RUN apk update
RUN apk add ca-certificates wget gnupg && update-ca-certificates