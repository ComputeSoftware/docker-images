FROM azul/zulu-openjdk:11.0.7-11.39.15

RUN apt-get update
RUN apt-get install -y wget gnupg curl



RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci