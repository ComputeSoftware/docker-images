FROM adoptopenjdk/openjdk11:jdk-11.0.3_7

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN groupadd -g 999 appuser && \
    useradd -r -u 999 -g appuser appuser
USER appuser

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.466.sh
RUN chmod +x linux-install-1.10.1.466.sh
RUN ./linux-install-1.10.1.466.sh