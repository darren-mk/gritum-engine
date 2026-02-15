(ns gritum.engine.core
  (:require
   [clojure.data.xml :as xml]))

(defn evaluate-xml
  "Main entry point for evaluating tolerance.
   Takes two XML strings (LE and CD) and returns the full report."
  [le-xml-str cd-xml-str]
  (let [le-xml (xml/parse-str le-xml-str)
        cd-xml (xml/parse-str cd-xml-str)]
    nil #_(eval/perform le-xml cd-xml)))

(def version "0.1.0-SNAPSHOT")
