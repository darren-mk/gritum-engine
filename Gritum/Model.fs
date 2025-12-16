module Gritum.Model

type LoanId =
    LoanId of string

type LoanPurpose =
    | Purchase
    | Refinance

type Loan =
    { Id : LoanId
      Purpose : LoanPurpose }
