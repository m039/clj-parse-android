(ns clj-parse-android.jar
  
  (:import [java.util Collections]
           [java.util.jar JarFile]
           [java.net URL URLClassLoader])

  (:require [clojure.java.io :as io])

  )

(def ^:dynamic *jar-class-loader* nil)

(defn ^JarFile
  jar-file [ path ]
  (if (not (nil? path))
    (let [file (io/as-file path)]
      (if (.exists file)
        (java.util.jar.JarFile. file)
        nil))))

(defn ^ClassLoader
  jar-class-loader [ path ]
  (if (not (nil? path))
    (let [file (io/as-file path)]
      (if (.exists file)
        (URLClassLoader/newInstance (into-array [ (URL. (str "jar:file://" (.getAbsolutePath file) "!/")) ]))
        nil))))

;;
;;
;;

(defn
  get-class-names [^JarFile jar-file]
  "Return the sequence of class-names (as string) in the jar-file"
  (map #(let [name (.getName %1)]
          (.replace (.substring name 0 (- (.length name) (.length ".class"))) "/" "."))
       (filter #(.endsWith (.getName %1) ".class")
               (Collections/list (.entries jar-file)))))

(defn
  get-class [ ^JarFile jar-file ]
  "Return the sequence of classes in the jar-file"
  (let [classes (loop [names (get-class-names jar-file)
                       classes () ]
                  (if (seq? names)
                    (let [class (try
                                  (Class/forName (first names))
                                  (catch Exception e)
                                  (catch ExceptionInInitializerError e)
                                  (catch NoClassDefFoundError e))]
                      (recur (next names) (conj classes class)))
                    classes))
        res (filter #(not (nil? %)) classes)]
    res))
