module Samples

open Gritum.Model

let leDocWoTcc =
    LE { effectiveDate = None
         totalClosingCosts = None }

let leDocWiTcc =
    LE { effectiveDate = None
         totalClosingCosts = Some (Helper.money 123.44m) }

let cdDocWoTcc =
    CD { effectiveDate = None
         totalClosingCosts = None }

let cdDocWiTcc =
    CD { effectiveDate = None
         totalClosingCosts = Some (Helper.money 123.44m) }

let purchaseLoan =
    { id = LoanId "fake-loan-id"
      purpose = Purchase }

let mkInput (snaps: DocumentSnapshot list) : PrecheckInput =
    { loan = purchaseLoan
      documentSnapshots = snaps }