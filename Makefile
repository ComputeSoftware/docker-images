.PHONY: ensure-ci

ensure-ci:
	clojure -M build-images
	circleci config validate .circleci/config.yml