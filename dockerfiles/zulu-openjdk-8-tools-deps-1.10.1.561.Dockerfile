FROM azul/zulu-openjdk:8u252-8.46.0.19

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN apt-get update && apt-get install ssh git curl -y

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.561.sh
RUN chmod +x linux-install-1.10.1.561.sh
RUN ./linux-install-1.10.1.561.sh

RUN groupadd --gid 3434 circleci \
  && useradd --uid 3434 --gid circleci --shell /bin/bash --create-home circleci
USER circleci