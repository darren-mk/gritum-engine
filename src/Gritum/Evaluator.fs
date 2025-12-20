module Gritum.Evaluator

open Gritum.Model

let isHighSeverity (finding:Finding) : bool =
    finding.severity = Severity.High

let isLowSeverity (finding:Finding) : bool =
    finding.severity = Severity.Low

let summarizeStatus (findings : Finding list) (ruleErrors : RuleErrors) : PrecheckStatus =
    if not ruleErrors.IsEmpty
    then PrecheckStatus.Inconclusive
    else
        if List.exists isHighSeverity findings then PrecheckStatus.Critical
        elif List.exists isLowSeverity findings then PrecheckStatus.Advisory
        else PrecheckStatus.Clear

let runRules (precheckInput : PrecheckInput) (rules : Rules)
    : Findings * RuleErrors * PrecheckStatus =
    let f (findings: Findings, ruleErrors: RuleErrors) (rule: Rule) : Findings * RuleErrors =
        let result: RuleCheckResult = rule.check precheckInput
        match result with
        | Ok (Some (finding: Finding)) -> finding :: findings, ruleErrors
        | Ok None -> findings, ruleErrors
        | Error (es: RuleErrors) -> findings, es @ ruleErrors
    let (findings: Findings), (ruleErrors: RuleErrors) = List.fold f ([],[]) rules
    let precheckStatus: PrecheckStatus = summarizeStatus findings ruleErrors
    findings, ruleErrors, precheckStatus