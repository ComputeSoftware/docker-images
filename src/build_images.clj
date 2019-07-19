(ns build-images
  (:require
    [clojure.string :as str]
    [clojure.java.io :as io]
    [clojure.math.combinatorics :as combo]
    [cljstache.core :as mustache]
    [clj-yaml.core :as yaml]))

(def debian-path "base-templates/debian.txt")
(def alpine-path "base-templates/alpine.txt")

(def all-images
  {:bases    [{:name          "adoptopenjdk-8"
               :image         "adoptopenjdk/openjdk8:jdk8u212-b04"
               :template-path debian-path}
              {:name          "adoptopenjdk-8-alpine"
               :image         "adoptopenjdk/openjdk8:jdk8u212-b04-alpine"
               :template-path alpine-path}
              {:name          "adoptopenjdk-11"
               :image         "adoptopenjdk/openjdk11:jdk-11.0.3_7"
               :template-path debian-path}
              {:name          "adoptopenjdk-11-alpine"
               :image         "adoptopenjdk/openjdk11:jdk-11.0.3_7-alpine"
               :template-path alpine-path}]
   :variants (sorted-map
               "tools-deps" {:template-path "variant-scripts/tools-deps.txt"
                             :versions      ["1.10.1.466"]}
               "intel-mkl" {:template-path "variant-scripts/intel-mkl.txt"
                            :versions      ["2018.4-057"]})})


(defn expand-variants
  [variants]
  (for [[variant-name {:keys [versions template-path]}] variants
        version versions]
    {:variant/name          variant-name
     :variant/version       version
     :variant/template-path template-path}))

(defn enumerate-image-variations
  [bases variants]
  (let [expanded-variants (expand-variants variants)
        variant-subsets (combo/subsets expanded-variants)]
    (for [base bases
          variant-set variant-subsets]
      {:base     base
       :variants variant-set})))

(defn render-file
  [path template-vars]
  (mustache/render (slurp path) template-vars))

(defn dockerfiles-content
  [image-variations]
  (map (fn [{:keys [base variants]}]
         (let [image-name (str (:name base)
                               (when-not (empty? variants)
                                 (str "-"
                                      (str/join "-"
                                                (map (fn [{variant-name :variant/name
                                                           version      :variant/version}]
                                                       (str variant-name "-" version))
                                                     (sort-by :variant/name variants))))))]
           {:image-name image-name
            :content    (str/join "\n\n"
                                  (concat [(render-file (:template-path base) {:from (:image base)})]
                                          (map (fn [{:variant/keys [version template-path]}]
                                                 (render-file template-path {:version version})) variants)))
            :file       (io/file "dockerfiles" (str image-name ".Dockerfile"))}))
       image-variations))

(def alphabet
  (map (comp str char) (range 97 123)))

(def alphabet-infinite
  (map-indexed (fn [idx letter]
                 (str/join (take (inc (int (/ idx 26))) (cycle [letter])))) (cycle alphabet)))

(defn get-circleci-config-map
  [dockerfiles]
  (let [dockerfiles (map (fn [idx m]
                           (assoc m :job-name idx)) alphabet-infinite dockerfiles)]
    {:version   "2.1"

     :jobs      (reduce (fn [jobs-map {:keys [job-name image-name file]}]
                          (assoc jobs-map
                            job-name
                            {:docker [{:image "docker:17.05.0-ce-git"}]
                             :steps  [:checkout
                                      :setup_remote_docker
                                      {:run {:name    (str "Build & push" image-name)
                                             :command (str/join "\n"
                                                                [(format "echo %s" image-name)
                                                                 (format "docker build -t computesoftware/%s:$CIRCLE_SHA1 . --file %s"
                                                                         image-name
                                                                         (.getPath file))
                                                                 "docker login -u $DOCKER_USER -p $DOCKER_PASS"
                                                                 (format "docker push computesoftware/%s:$CIRCLE_SHA1"
                                                                         image-name)])}}]}))
                        {} dockerfiles)

     :workflows {:version     "2"
                 :ci-workflow {:jobs (map (fn [{:keys [job-name]}]
                                            {job-name {:context "docker-env"}})
                                          dockerfiles)}}}))

(defn write-ci-and-dockerfiles
  [{:keys [bases variants]}]
  (let [image-variations (enumerate-image-variations bases variants)
        dockerfiles (dockerfiles-content image-variations)
        ci-config-map (get-circleci-config-map dockerfiles)]

    (doseq [{:keys [file content]} dockerfiles]
      (spit file content))

    (spit ".circleci/config.yml" (yaml/generate-string ci-config-map))))

(defn -main
  [& args]
  (write-ci-and-dockerfiles all-images))