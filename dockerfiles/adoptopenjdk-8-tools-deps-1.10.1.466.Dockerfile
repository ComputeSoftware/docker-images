FROM adoptopenjdk/openjdk8:jdk8u212-b04

RUN apt-get install -y wget gnupg

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.466.sh
RUN chmod +x linux-install-1.10.1.466.sh
RUN sudo ./linux-install-1.10.1.466.sh