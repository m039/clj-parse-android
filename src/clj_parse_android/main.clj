(ns clj-parse-android.main
  (:gen-class)
  (:use [clj-parse-android jar class]
        )

  (:require [clojopts.core :as opts]
            [clojopts.help :as opts-help]
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
                (opts/optional-arg source s "Add extra info from the sources"))]
            
      (binding [*jar-file* (jar-file (:jar opts))
                *jar-class-loader* (jar-class-loader (:jar opts))
                *output* (:output opts)]

        (when (and *jar-file* *output*)
          
          ;;
          ;; The whole procedure
          ;;

          (let [classes (get-class *jar-file*)
                classes-count (count classes)]

            (sql/with-connection {:subprotocol "sqlite"
                                  :subname *output* }

              (sql/do-commands "drop table if exists class")

              (sql/create-table "class"
                                ["simple_name" "string"]
                                ["name" "string"]
                                ["methods" "string"]
                                ["is_interface" "integer"])

              (println (str "Parses '" (:jar opts) "':"))

              (loop [i 1
                     classes classes]
                (when (seq? classes)
                  (let [class (first classes)]
                    (sql/insert-values "class"
                                       [ "simple_name" "name" "methods" "is_interface" ]
                                       [ (get-simple-name class)
                                         (get-name class)
                                         (declared-methods-to-string (get-declared-methods class))
                                         (is-interface class)])
                    (print (format "\r proceseed [ %6d / %6d ]" i classes-count)))

                  (recur (inc i) (next classes))))

              (println)
              (println (str "The output is '" *output* "'")))))
        ))
    ))
