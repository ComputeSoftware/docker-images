FROM adoptopenjdk/openjdk11:jdk-11.0.3_7

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.466.sh
RUN chmod +x linux-install-1.10.1.466.sh
RUN sudo ./linux-install-1.10.1.466.sh