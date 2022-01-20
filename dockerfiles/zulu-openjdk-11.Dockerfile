FROM azul/zulu-openjdk:11.0.13-11.52.13

RUN apt-get update && apt-get install -y wget gnupg curl

RUN useradd --uid 3434 --user-group --shell /bin/bash --create-home circleci

USER circleci

