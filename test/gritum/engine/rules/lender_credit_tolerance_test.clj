(ns gritum.engine.rules.lender-credit-tolerance-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.engine.rules.lender-credit-tolerance :as sut]
   [gritum.engine.domain.model :as dom]
   [gritum.engine.test-helper :as h]))

(defn- mock-lender-credit-summary
  "Creates a summary node matching the path in Refinance Fixed UCD v2.0.xml."
  [amount cure]
  {:tag :INTEGRATED_DISCLOSURE_SECTION_SUMMARY
   :content [{:tag :INTEGRATED_DISCLOSURE_SECTION_SUMMARY_DETAIL
              :content [{:tag :IntegratedDisclosureSectionType :content ["TotalClosingCosts"]}
                        {:tag :IntegratedDisclosureSubsectionType :content ["LenderCredits"]}
                        {:tag :IntegratedDisclosureSectionTotalAmount :content [(str amount)]}
                        {:tag :LenderCreditToleranceCureAmount :content [(str cure)]}]}]})

(deftest evaluate-lender-credit-test
  (testing "Violation: Lender Credit decrease from LE to CD"
    (let [le-doc (dom/->document (h/populate-xml [(mock-lender-credit-summary 1000.0 0.0)] dom/path-to-idss))
          cd-doc (dom/->document (h/populate-xml [(mock-lender-credit-summary 800.0 0.0)] dom/path-to-idss))
          result (sut/evaluate le-doc cd-doc)]
      (is (= 200.0 (:required-cure result)) "Should require $200 cure for the credit reduction")
      (is (true? (:violated? result)))))
  (testing "Compliance: Existing cure in CD matches required cure"
    (let [le-doc (dom/->document (h/populate-xml [(mock-lender-credit-summary 1000.0 0.0)] dom/path-to-idss))
          ;; Credit dropped to 700, but lender already provided 300 cure in the XML
          cd-doc (dom/->document (h/populate-xml [(mock-lender-credit-summary 700.0 300.0)] dom/path-to-idss))
          result (sut/evaluate le-doc cd-doc)]
      (is (= 300.0 (:required-cure result)))
      (is (= 300.0 (:existing-cure result)))
      (is (false? (:violated? result)) "Not violated because existing cure satisfies requirements")))
  (testing "Edge Case: Missing Lender Credit section in LE"
    (let [le-doc (dom/->document {:tag :MESSAGE :content []})
          cd-doc (dom/->document (h/populate-xml [(mock-lender-credit-summary 500.0 0.0)] dom/path-to-idss))
          result (sut/evaluate le-doc cd-doc)]
      (is (= 0.0 (:le-total-credit result)))
      (is (= 0.0 (:required-cure result)))
      (is (false? (:violated? result))))))
