FROM azul/zulu-openjdk:11.0.12-11.50.19

RUN apt-get update && apt-get install -y wget gnupg curl



RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci
