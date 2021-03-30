FROM azul/zulu-openjdk:11.0.10-11.45.27

RUN apt-get update
RUN apt-get install -y wget gnupg curl



RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci