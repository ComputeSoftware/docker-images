.PHONY: ensure-ci

ensure-ci:
	clojure -M -m build-images
	circleci config validate .circleci/config.yml