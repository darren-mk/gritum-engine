(ns gritum.interpret)

(def loan-path
  [:MESSAGE :DOCUMENT_SETS
   :DOCUMENT_SET :DOCUMENTS
   :DOCUMENT :DEAL_SETS :DEAL_SET
   :DEALS :DEAL :LOANS :LOAN])

(def zero-tolerance-fee-types
  #{"AppraisalFee" "CreditReportFee"
    "FloodDeterminationFee"
    "AppraisalFieldReviewFee"
    "TaxServiceFee"})

(defn ->tolerance [section fee-type]
  (case section
    ("OriginationCharges" "ServicesYouCannotShopFor") 0.0
    ("ServicesYouCanShopFor") 0.1
    ("ServicesBorrowerDidNotShopFor") (if (zero-tolerance-fee-types fee-type) 0.0 0.1)
    ("TaxesAndOtherGovernmentFees") (if (= fee-type "RecordingFee") 0.1 0.0)
    ("Prepaids" "InitialEscrowPaymentAtClosing"
     "ServicesBorrowerDidShopFor" "OtherCosts") nil))

(def apr-mandatory-fees
  #{"LoanDiscountPoints"
    "OriginationFee"
    "UnderwritingFee"
    "ProcessingFee"})
