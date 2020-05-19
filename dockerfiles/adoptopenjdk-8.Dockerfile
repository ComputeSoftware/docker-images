FROM adoptopenjdk/openjdk8:jdk8u252-b09

RUN apt-get update
RUN apt-get install -y wget gnupg



RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci