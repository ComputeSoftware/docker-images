FROM adoptopenjdk/openjdk11:jdk-11.0.3_7

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN groupadd --gid 3434 appuser \
  && useradd --uid 3434 --gid appuser --shell /bin/bash --create-home appuser
USER appuser