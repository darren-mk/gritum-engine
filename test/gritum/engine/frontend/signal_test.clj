(ns gritum.engine.frontend.signal-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.engine.frontend.signal :as sg]
   [malli.core :as m]))

(deftest signal-helper-tests
  (testing "Basic signal binding and referencing"
    (is (= "user-id" (sg/bind :user-id))
        "bind should return the keyword name as a plain string")
    (is (= "$user-id" (sg/cite :user-id))
        "cite should prefix the signal with $")
    (is (= "!$user-id" (sg/cite-not :user-id))
        "cite-not should prefix the signal with !$"))
  (testing "Logic and comparison helpers"
    (is (= "$role=='admin'" (sg/equal? (sg/cite :role) (sg/->val "admin")))
        "equal? should join two strings with == operator")
    (is (= "$is-open=!$is-open" (sg/toggle :is-open))
        "toggle should generate a self-inverting assignment expression"))
  (testing "State modification and checks"
    (is (= "$input-field=''" (sg/erase :input-field))
        "erase should assign an empty string to the signal")
    (is (= "$input-field==''" (sg/ref-empty? :input-field))
        "ref-empty? should check if the signal is exactly an empty string")
    (is (= "!$input-field==''" (sg/ref-not-empty? :input-field))
        "ref-not-empty? should use the negated signal reference for empty check"))
  (testing "String literal wrapping"
    (is (= "'active'" (sg/->val "active"))
        "string should wrap a keyword name in single quotes for JS expressions")))

(deftest malli-schema-validation
  (testing "Ensuring functions match defined Malli schemas"
    (is (m/validate [:=> [:cat :keyword] :string] sg/bind))
    (is (m/validate [:=> [:cat :keyword] :string] sg/cite))
    (is (m/validate [:=> [:cat :keyword] :string] sg/toggle))))
