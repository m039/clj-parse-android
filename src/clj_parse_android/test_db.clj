(ns clj-parse-android.test
  (:use [clojure.java.jdbc :as sql])
  (:import [java.sql Connection DriverManager PreparedStatement ResultSet Statement]))

;;
;; Java interop
;;

(Class/forName "org.sqlite.JDBC")

(def *path-to-db* "db/test.db")
(def ^:dynamic *connection* (DriverManager/getConnection (str "jdbc:sqlite:" *path-to-db*)))

(binding [ *connection* (DriverManager/getConnection (str "jdbc:sqlite:" *path-to-db*)) ]

  (let [statement (.createStatement *connection*)]
    (doto statement
      (.setQueryTimeout 30)
      (.executeUpdate "drop table if exists person")
      (.executeUpdate "create table person (id integer, name string)")
      (.executeUpdate "insert into person values(1, 'leo')")
      (.executeUpdate "insert into person values(2, 'yui')")))

  (.close *connection*))


;;
;; Clojure wrapper
;;

(def *db* {:subprotocol "sqlite"
           :subname "db/test_wrapper.db"})

(sql/with-connection *db*
  (do-commands (str "drop table if exists " "person"))

  (create-table "person"
                ["simple_name" "string"]
                ["name" "string"])
  
  (insert-values "person"
                 ["simple_name" "name"]
                 ["m039" "m-name"]
                 ["930m" "9-name"]))
  
