FROM azul/zulu-openjdk:11.0.13-11.52.13

RUN apt-get update && apt-get install -y wget gnupg curl



RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci
