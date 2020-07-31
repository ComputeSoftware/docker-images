FROM azul/zulu-openjdk:8u252-8.46.0.19

RUN apt-get update
RUN apt-get install -y wget gnupg



RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci