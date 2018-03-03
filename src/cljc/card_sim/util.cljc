(ns card-sim.util
  "Utilitiy functions"
  (:require [clojure.string :as str]))

(defn pluralize
  "Build a string of '{count}{word}' where word is correctly pluralized."
  ([n singular]
   (pluralize n singular (str singular "s")))
  ([n singular plural]
   (str n (if (= 1 n) singular plural))))

(defn ucfirst
  "Upper-case the first letter of a string."
  [s]
  (str (str/upper-case (subs s 0 1))
       (subs s 1)))

