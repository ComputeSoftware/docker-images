(ns build-images
  (:require
    [clojure.string :as str]
    [clojure.java.io :as io]
    [cljstache.core :as mustache]
    [clj-yaml.core :as yaml]))

(def debian-path "base-templates/debian.txt")
(def alpine-path "base-templates/alpine.txt")

(def all-images
  {:bases    {:adoptopenjdk-8  {:image         "adoptopenjdk/openjdk8:jdk8u212-b04"
                                :template-path debian-path}
              :adoptopenjdk-11 {:image         "adoptopenjdk/openjdk11:jdk-11.0.3_7"
                                :template-path debian-path}}
   :variants {:tools-deps {:template-path "variant-scripts/tools-deps.txt"}
              :intel-mkl  {:template-path "variant-scripts/intel-mkl.txt"}}
   :combos   [{:base :adoptopenjdk-8}

              {:base     :adoptopenjdk-8
               :variants [[:tools-deps {:version "1.10.1.466"}]]}

              {:base     :adoptopenjdk-8
               :variants [[:tools-deps {:version "1.10.1.466"}]
                          [:intel-mkl {:version "2018.4-057"}]]}

              {:base :adoptopenjdk-11}

              {:base     :adoptopenjdk-11
               :variants [[:tools-deps {:version "1.10.1.466"}]]}

              {:base     :adoptopenjdk-11
               :variants [[:tools-deps {:version "1.10.1.466"}]
                          [:intel-mkl {:version "2018.4-057"}]]}]})

(defn render-file
  [path template-vars]
  (mustache/render (slurp path) template-vars))

(defn dockerfiles-content
  [images-spec]
  (map (fn [{:keys [base variants]}]
         (let [variant-str-combo (when-not (empty? variants)
                                   (str/join "-"
                                             (map (fn [[variant-name {:keys [version]}]]
                                                    (str (name variant-name) "-" version))
                                                  (sort-by first variants))))
               file-name (str (name base) (when variant-str-combo
                                            (str "-" variant-str-combo)))
               {base-template-path :template-path
                base-image         :image} (get-in images-spec [:bases base])]
           {:image-name (name base)
            :tag        variant-str-combo
            :file-name  file-name
            :content    (str/join "\n\n"
                                  (concat [(render-file base-template-path {:from base-image})]
                                          (map (fn [[variant template-vars]]
                                                 (render-file (get-in images-spec [:variants variant :template-path])
                                                              template-vars))
                                               variants)))
            :file       (io/file "dockerfiles" (str file-name ".Dockerfile"))}))
       (:combos images-spec)))

(defn get-circleci-config-map
  [dockerfiles]
  (let [dockerfiles (map (fn [m]
                           (assoc m
                             :job-name
                             ;; circleci doesn't allow periods in the job name
                             (str/replace (:file-name m) "." "_"))) dockerfiles)]
    {:version   "2.1"

     :jobs      (reduce (fn [jobs-map {:keys [job-name image-name tag file]}]
                          (assoc jobs-map
                            job-name
                            {:docker [{:image "docker:17.05.0-ce-git"}]
                             :steps  [:checkout
                                      :setup_remote_docker
                                      {:run {:name    (str "Build & push " image-name)
                                             :command (str/join "\n"
                                                                [(format "echo %s" image-name)
                                                                 (format "docker build -t computesoftware/%s:%s . --file %s"
                                                                         image-name
                                                                         tag
                                                                         (.getPath file))
                                                                 "docker login -u $DOCKER_USER -p $DOCKER_PASS"
                                                                 (format "docker push computesoftware/%s:%s"
                                                                         image-name
                                                                         tag)])}}]}))
                        {} dockerfiles)

     :workflows {:version     "2"
                 :ci-workflow {:jobs (map (fn [{:keys [job-name]}]
                                            {job-name {:context "docker-env"}})
                                          dockerfiles)}}}))

(defn write-ci-and-dockerfiles
  [images-spec]
  (let [dockerfiles (dockerfiles-content images-spec)
        ci-config-map (get-circleci-config-map dockerfiles)]

    (doseq [{:keys [file content]} dockerfiles]
      (spit file content))

    (spit ".circleci/config.yml" (yaml/generate-string ci-config-map))))

(defn -main
  [& args]
  (write-ci-and-dockerfiles all-images))