FROM adoptopenjdk/openjdk11:jdk-11.0.3_7

RUN apt-get update
RUN apt-get install -y wget gnupg

RUN groupadd --gid 3434 appuser \
  && useradd --uid 3434 --gid appuser --shell /bin/bash --create-home appuser \
  && echo 'appuser ALL=NOPASSWD: ALL' >> /etc/sudoers.d/50-appuser \
  && echo 'Defaults    env_keep += "DEBIAN_FRONTEND"' >> /etc/sudoers.d/env_keep
USER appuser

# https://clojure.org/guides/getting_started
RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.466.sh
RUN chmod +x linux-install-1.10.1.466.sh
RUN ./linux-install-1.10.1.466.sh