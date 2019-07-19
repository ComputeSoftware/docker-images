.PHONY: ensure-ci

ensure-ci:
	clojure -m build-images
	circleci config validate .circleci/config.yml