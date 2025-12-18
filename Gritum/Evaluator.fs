module Gritum.Evaluator

open Gritum.Model

let private severityRank =
    function
    | Severity.Low -> 1
    | Severity.High -> 2

let private statusRank =
    function
    | PrecheckStatus.Clear -> 0
    | PrecheckStatus.Advisory -> 1
    | PrecheckStatus.Critical -> 2
    | PrecheckStatus.Inconclusive -> 3

let isFindingOnHighSeverity (finding:Finding) : bool =
    finding.severity = Severity.High

let isFindingOnLowSeverity (finding:Finding) : bool =
    finding.severity = Severity.Low

let private statusFromFindings (findings : Finding list) : PrecheckStatus =
    let hasCritical: bool = List.exists isFindingOnHighSeverity findings
    let hasAdvisory: bool = List.exists isFindingOnLowSeverity findings
    if hasCritical then PrecheckStatus.Critical
    elif hasAdvisory then PrecheckStatus.Advisory
    else PrecheckStatus.Clear

let summarizeStatus (findings : Finding list) (errors : RuleError list) : PrecheckStatus =
    if not errors.IsEmpty
    then PrecheckStatus.Inconclusive
    else
        let hasHigh : bool = findings |> List.exists (fun (f: Finding) -> f.severity = Severity.High)
        let hasLow : bool = findings |> List.exists (fun (f: Finding) -> f.severity = Severity.Low)
        if hasHigh then PrecheckStatus.Critical
        elif hasLow then PrecheckStatus.Advisory
        else PrecheckStatus.Clear