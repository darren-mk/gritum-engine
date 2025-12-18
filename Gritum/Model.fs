module Gritum.Model

type Money =
    private Money of decimal

module Money =
    let create (x:decimal) : Result<Money, string> =
        if x >= 0m then Ok (Money x)
        else Error "Money must be non-negative"
    let value (Money (x: decimal)) = x

type LoanId =
    LoanId of string

type LoanPurpose =
    | Purchase
    | Refinance

type Loan =
    { id : LoanId
      purpose : LoanPurpose }

[<RequireQualifiedAccess>]
type PrecheckStatus =
    | Clear
    | Advisory
    | Critical
    | Inconclusive

[<RequireQualifiedAccess>]
type Severity =
    | Low
    | High

type RuleId = RuleId of string

type DocumentType =
    | LoanEstimate
    | ClosingDisclosure

type Evidence =
    | TotalClosingCosts of amount : Money * documentType : DocumentType

type Finding =
    { ruleId : RuleId
      severity : Severity
      evidence : Evidence list
      message : string }

type DocumentSnapshot =
    { documentType : DocumentType
      totalClosingCosts : Money option }

type PrecheckInput =
    { loan : Loan
      documentSnapshots : DocumentSnapshot list }

type InvalidReason =
    | NegativeValue
    | ParseFailure
    | OutOfRange

type RuleError =
    | MissingField of DocumentType
    | InvalidField of DocumentType * InvalidReason

type RuleErrors =
    RuleError list

type RuleCheckResult =
    Result<Finding option, RuleErrors>

type Rule =
    { id : RuleId
      check : PrecheckInput -> RuleCheckResult }
