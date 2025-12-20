module Gritum.Rules

open System
open Gritum.Model

// -----------------
// Rule implementations
// -----------------

let totalClosingCostsRuleId = RuleId "TCC-001"

let selectEffectiveLE (snapshots: DocumentSnapshot list) : DocumentContent option =
    let pick (best: DocumentContent option) (snapshot: DocumentSnapshot) =
        match best, snapshot with
        | _, CD _ -> best
        | None, LE (c: DocumentContent) ->
            match c.effectiveDate with
            | Some _ -> Some c
            | None -> None
        | Some (b: DocumentContent), LE (c: DocumentContent) ->
            match b.effectiveDate, c.effectiveDate with
            | Some (bd: DateOnly), Some (cd: DateOnly) ->
                if cd > bd then Some c else Some b
            | Some _, None -> Some b
            | None, Some _ -> Some c
            | None, None -> Some b
    List.fold pick None snapshots

let selectEffectiveCD (snapshots: DocumentSnapshot list) : DocumentContent option =
    let pick (best: DocumentContent option) (snapshot: DocumentSnapshot) =
        match best, snapshot with
        | _, LE _ -> best
        | None, CD (c: DocumentContent) ->
            match c.effectiveDate with
            | Some _ -> Some c
            | None -> None
        | Some (b: DocumentContent), CD (c: DocumentContent) ->
            match b.effectiveDate, c.effectiveDate with
            | Some (bd: DateOnly), Some (cd: DateOnly) ->
                if cd > bd then Some c else Some b
            | Some _, None -> Some b
            | None, Some _ -> Some c
            | None, None -> Some b
    List.fold pick None snapshots

let compareTotalClosingCosts (ledc: DocumentContent) (cddc: DocumentContent) : RuleCheckResult =
    match ledc.totalClosingCosts, cddc.totalClosingCosts with
    | Some a, Some b ->
        if a = b then Ok None
        else
            Ok (Some { ruleId = totalClosingCostsRuleId
                       severity = Severity.High
                       evidence = []
                       message = "hi" })
    | Some _, None -> Error [ MissingField (ClosingDisclosure, TotalClosingCosts) ]
    | None, Some _ -> Error [ MissingField (LoanEstimate, TotalClosingCosts) ]
    | None, None ->
        Error [ MissingField (LoanEstimate, TotalClosingCosts)
                MissingField (ClosingDisclosure, TotalClosingCosts) ]

let checkTotalClosingCosts (p: PrecheckInput) : RuleCheckResult =
    let le = selectEffectiveLE p.documentSnapshots
    let cd = selectEffectiveCD p.documentSnapshots
    match le, cd with
    | Some ledc, Some cddc ->
        // Optional: enforce effectiveDate selection success (if you want this rule to require it)
        compareTotalClosingCosts ledc cddc
    | None, Some _ -> Error [ MissingField (LoanEstimate, TotalClosingCosts) ]
    | Some _, None -> Error [ MissingField (ClosingDisclosure, TotalClosingCosts) ]
    | None, None ->
        Error [ MissingField (LoanEstimate, TotalClosingCosts)
                MissingField (ClosingDisclosure, TotalClosingCosts) ]

let totalClosingCostsRule : Rule =
    { id = totalClosingCostsRuleId
      check = checkTotalClosingCosts }

// -----------------
// Registry
// -----------------

let all : Rules =
    [ totalClosingCostsRule ]