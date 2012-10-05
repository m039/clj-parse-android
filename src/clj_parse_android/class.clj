(ns clj-parse-android.class

  (:use [clj-parse-android jar])
  
  )

(defn ^Class for-name [ class-name ]
  (Class/forName class-name false *jar-class-loader*))

(defn get-simple-name [ ^Class class ]
  (.getSimpleName class))

;;
;; Class wrapper
;;

(defn class-to-string [ ^Class class ]
  (str (.getSimpleName class)))

;;
;; Method class wrappers
;;

(defn get-name [ method-or-class ]
  (.getName method-or-class))

(defn get-parameter-types [ ^java.lang.reflect.Method method ]
  (.getParameterTypes method))

(defn parameter-types-to-string [ #^Class classes ]
  (when (seq classes)
    (reduce #(str %1 ", " %2) (map class-to-string classes))))

(defn get-return-type [ ^java.lang.reflect.Method method ]
  (.getReturnType method))

(defn return-type-to-string [ return-type ]
  (class-to-string return-type))

;;
;;
;;

(defn is-abstract [ modifier ]
  (when (java.lang.reflect.Modifier/isAbstract modifier) "abstract"))

(defn is-final [ modifier ]
  (when (java.lang.reflect.Modifier/isFinal modifier) "final"))

(defn is-interface [ modifier ]
  (when (java.lang.reflect.Modifier/isInterface modifier) "interface"))

(defn is-native [ modifier ]
  (when (java.lang.reflect.Modifier/isNative modifier) "native"))

(defn is-private [ modifier ]
  (when (java.lang.reflect.Modifier/isPrivate modifier) "private"))

(defn is-protected [ modifier ]
  (when (java.lang.reflect.Modifier/isProtected modifier) "protected"))

(defn is-public [ modifier ]
  (when (java.lang.reflect.Modifier/isPublic modifier) "public"))

(defn is-static [ modifier ]
  (when (java.lang.reflect.Modifier/isStatic modifier) "static"))

(defn is-strict [ modifier ]
  (when (java.lang.reflect.Modifier/isStrict modifier) "strict"))

(defn is-synchronized [ modifier ]
  (when (java.lang.reflect.Modifier/isSynchronized modifier) "synchronized"))

(defn is-transient [ modifier ]
  (when (java.lang.reflect.Modifier/isTransient modifier) "transient"))

(defn is-volatile [ modifier ]
  (when (java.lang.reflect.Modifier/isVolatile modifier) "volatile"))


(defn get-modifiers [ ^java.lang.reflect.Method method ]
  (let [modifiers (.getModifiers method)]
    (filter string?
            (reduce #(conj %1 (%2 modifiers)) [] [#'is-abstract
                                                  #'is-final
                                                  #'is-interface
                                                  #'is-native
                                                  #'is-private
                                                  #'is-protected
                                                  #'is-public
                                                  #'is-static
                                                  #'is-strict
                                                  #'is-synchronized
                                                  #'is-transient
                                                  #'is-volatile
                                                  #'is-protected ]))))

(defn modifiers-to-string [  modifiers-seq ]
  (let [ms (filter #(not (= %1 "abstract")) modifiers-seq)]
    (if (not (empty? ms))
      (reduce #(str %1 " " %2) ms))))

;; 

(defn get-declared-methods
  [ class ]
  (cond

   (instance? Class class)
   (.getDeclaredMethods class)

   (instance? String class)
   (.getDeclaredMethods (for-name class))
   ))

(defn declared-methods-to-string [ methods ]
  (if (not (empty? methods))
    (reduce #(str %1 "\n" %2) (loop [ ms methods res []]
                                (if (not (nil? ms))
                                  (recur (next ms)
                                         (let [method (first ms)]
                                           (conj res (str
                                                      (modifiers-to-string (get-modifiers method))
                                                      " "
                                                      (return-type-to-string (get-return-type method))
                                                      " "
                                                      (get-name method)
                                                      "("
                                                      (parameter-types-to-string (get-parameter-types method))
                                                      ")"))))
                                  res)))))