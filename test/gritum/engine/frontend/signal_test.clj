(ns gritum.engine.frontend.signal-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.engine.frontend.signal :as sg]
   [malli.core :as m]))

(deftest signal-helper-tests
  (testing "Basic signal binding and referencing"
    (is (= "user-id" (sg/bind :user-id)))
    (is (= "$user-id" (sg/cite :user-id)))
    (is (= "!$user-id" (sg/cite-not :user-id))))
  (testing "Logic and comparison helpers"
    (is (= "$role=='admin'" (sg/equal? (sg/cite :role) (sg/->text "admin"))))
    (is (= "$is-open=!$is-open" (sg/toggle :is-open))))
  (testing "State modification and checks"
    (is (= "$input-field=''" (sg/erase :input-field)))
    (is (= "$input-field==''" (sg/ref-empty? :input-field)))
    (is (= "!$input-field==''" (sg/ref-not-empty? :input-field))))
  (testing "String literal wrapping"
    (is (= "'active'" (sg/->text "active")))))

(deftest malli-schema-validation
  (testing "Ensuring functions match defined Malli schemas"
    (is (m/validate [:=> [:cat :keyword] :string] sg/bind))
    (is (m/validate [:=> [:cat :keyword] :string] sg/cite))
    (is (m/validate [:=> [:cat :keyword] :string] sg/toggle))))
