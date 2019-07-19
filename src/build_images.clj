(ns build-images
  (:require
    [clojure.string :as str]
    [clojure.java.io :as io]
    [clojure.math.combinatorics :as combo]
    [cljstache.core :as mustache]
    [clj-yaml.core :as yaml]))

(def all-images
  {:bases    [{:name  "adoptopenjdk-8"
               :image "adoptopenjdk/openjdk8:jdk8u212-b04"}
              {:name  "adoptopenjdk-8-alpine"
               :image "adoptopenjdk/openjdk8:jdk8u212-b04-alpine"}
              {:name  "adoptopenjdk-11"
               :image "adoptopenjdk/openjdk11:jdk-11.0.3_7"}
              {:name  "adoptopenjdk-11-alpine"
               :image "adoptopenjdk/openjdk11:jdk-11.0.3_7-alpine"}]
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

(def base-template-path "dockerfile-base-template.txt")

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
                                  (concat [(render-file base-template-path {:from (:image base)})]
                                          (map (fn [{:variant/keys [version template-path]}]
                                                 (render-file template-path {:version version})) variants)))
            :file       (io/file "dockerfiles" (str image-name ".Dockerfile"))}))
       image-variations))

(defn get-circleci-config-map
  [dockerfiles]
  {:version   "2.1"

   :jobs      (reduce (fn [jobs-map {:keys [image-name file]}]
                        (assoc jobs-map
                          (str "publish-" image-name)
                          {:docker [{:image "docker:17.05.0-ce-git"}]
                           :steps  [:checkout
                                    {:run {:name    (str "Build & push" image-name)
                                           :command (str/join "\n"
                                                              [(format "docker build -t computesoftware/%s:$CIRCLE_SHA1 . --file %s"
                                                                       image-name
                                                                       (.getPath file))
                                                               "docker login -u $DOCKER_USER -p $DOCKER_PASS"
                                                               (format "docker push computesoftware/%s:$CIRCLE_SHA1"
                                                                       image-name)])}}]}))
                      {} dockerfiles)

   :workflows {:version     "2"
               :ci-workflow {:jobs (map (fn [{:keys [image-name]}]
                                          {(str "publish-" image-name) {:context "docker-env"}})
                                        dockerfiles)}}})

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