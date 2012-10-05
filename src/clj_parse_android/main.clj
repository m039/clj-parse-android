(ns clj-parse-android.main
  (:gen-class)
  (:use [clj-parse-android jar class])
  
  (:require [ clojopts.core :as opts]
            [clojure.java.jdbc :as sql]))

(def ^:dynamic *argv* [ "--jar=/opt/android-sdk/platforms/android-15/android.jar"
                        "-o/tmp/android.db" ])

(def ^:dynamic *jar-file* nil)
(def ^:dynamic *output* nil)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (binding [ *argv* args ]
    (let [opts (opts/clojopts
                "clojopts"
                *argv*
                (opts/with-arg jar j "The jar file to parse")
                (opts/with-arg output o "The place to put the database")
                (opts/optional-arg source s "Add extra info from the sources")) ]

      (binding [*jar-file* (jar-file (:jar opts))
                *jar-class-loader* (jar-class-loader (:jar opts))
                *output* (:output opts)]

        ;;
        ;; The whole procedure
        ;; 
        (sql/with-connection {:subprotocol "sqlite"
                              :subname *output* }

          (sql/do-commands "drop table if exists class")

          (sql/create-table "class"
                            ["simple_name" "string"]
                            ["name" "string"]
                            ["methods" "string"])

          (doseq [class (get-class *jar-file*)]
            (sql/insert-values "class"
                               [ "simple_name" "name" "methods" ]
                               [ (get-simple-name class)
                                 (get-name class)
                                 (declared-methods-to-string (get-declared-methods class)) ]
                               )))
        ))
    ))
