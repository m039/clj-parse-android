(ns clj-parse-android.main
  (:gen-class)
  (:use [clj-parse-android jar class]
        )

  (:require [clojopts.core :as opts]
            [clojopts.help :as opts-help]
            [clojure.java.jdbc :as sql]
            [clojure.java.io :as io]
            [cemerick.pomegranate :as pomegranate]))

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
                (opts/with-arg jar j "The jar files to parse (use ':' as delimeter to pass several jars)" :parse #(.split % ":") :type :str)
                (opts/with-arg output o "The place to put the database")
                (opts/optional-arg source s "Add extra info from the sources"))]

      (doseq [jar (:jar opts)]
        (println (str "Adding to classpath: " jar))
        (pomegranate/add-classpath jar))
      
      (doseq [jar (:jar opts)]
        (binding [*jar-file* (jar-file jar)
                  *output* (:output opts)]

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
                                ["is_interface" "integer"]
                                ["enclosing_class__name" "string"]
                                )

              (println (str "Parses '" jar "':"))

              (loop [i 1
                     classes classes]
                (when (seq? classes)
                  (let [class (first classes)]
                    (sql/insert-values "class"
                                       [ "simple_name" "name" "methods" "is_interface" "enclosing_class__name"
                                         ]
                                       [ (get-simple-name class)
                                         (get-name class)
                                         (declared-methods-to-string (get-declared-methods class))
                                         (is-interface class)
                                         (get-name (get-enclosing-class class))
                                         ])
                    (print (format "\r proceseed [ %6d / %6d ]" i classes-count)))

                  (recur (inc i) (next classes))))

              (println)
              (println (str "The output is '" *output* "'"))))))
        )))

