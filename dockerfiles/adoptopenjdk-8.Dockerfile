FROM adoptopenjdk/openjdk8:jdk8u212-b04

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN groupadd -g 999 appuser && \
    useradd -r -u 999 -g appuser appuser
USER appuser