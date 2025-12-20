module Gritum.Model

open System

type Money =
    private Money of decimal

module Money =
    let create (x: decimal) : Money =
        Money x
    let createNonNegative (x: decimal) : Result<Money, string> =
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

type Findings =
    Finding list

type DocumentContent =
    { effectiveDate : DateOnly option
      totalClosingCosts : Money option }

type DocumentSnapshot =
    | LE of DocumentContent
    | CD of DocumentContent

type PrecheckInput =
    { loan : Loan
      documentSnapshots : DocumentSnapshot list }

type InvalidReason =
    | NegativeValue
    | ParseFailure
    | OutOfRange

type FieldName =
    | EffectiveDate
    | TotalClosingCosts

type RuleError =
    | MissingDocument of DocumentType
    | MissingField of DocumentType * FieldName
    | InvalidField of DocumentType * FieldName * InvalidReason

type RuleErrors =
    RuleError list

type RuleCheckResult =
    Result<Finding option, RuleErrors>

type RuleCheckResults =
    RuleCheckResult list

type Check =
    PrecheckInput -> RuleCheckResult

type Rule =
    { id : RuleId
      check : Check }

type Rules =
    Rule list