FROM adoptopenjdk/openjdk11:jdk-11.0.3_7

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN groupadd -g 999 appuser && \
    useradd -r -u 999 -g appuser appuser
USER appuser