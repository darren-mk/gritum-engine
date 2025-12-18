module Gritum.Rules

open Gritum.Model

let doesDsMissTotalClosingCosts (ds:DocumentSnapshot) : bool =
    match ds.totalClosingCosts with
    | Some _ -> false
    | None -> true

let checkTotalClosingCosts (p:PrecheckInput) : RuleCheckResult =
    let DSsMissingCosts: DocumentSnapshot list =
        List.filter doesDsMissTotalClosingCosts p.documentSnapshots
    if not (List.isEmpty DSsMissingCosts)
    then
        let sayMissingField (ds:DocumentSnapshot) =
             MissingField ds.documentType
        let ruleErrors: RuleErrors =
            List.map sayMissingField DSsMissingCosts
        Error ruleErrors
    else
        Ok None