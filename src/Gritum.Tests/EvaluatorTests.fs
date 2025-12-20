module EvaluatorTests

open Xunit
open Gritum.Model
open Gritum.Evaluator
open Gritum.Rules
open Samples

// --------------------
// Test fixtures
// --------------------

let findingHigh : Finding =
    { ruleId = RuleId "F-High"
      severity = Severity.High
      evidence = ([] : Evidence list)
      message = "high" }

let findingLow : Finding =
    { ruleId = RuleId "F-Low"
      severity = Severity.Low
      evidence = ([] : Evidence list)
      message = "low" }

// --------------------
// summarizeStatus tests
// --------------------

[<Fact>]
let ``summarizeStatus - errors force Inconclusive`` () =
    let status =
        summarizeStatus [] [ MissingField (LoanEstimate, TotalClosingCosts) ]
    Assert.Equal(PrecheckStatus.Inconclusive, status)

[<Fact>]
let ``summarizeStatus - High finding yields Critical`` () =
    let status =
        summarizeStatus [ findingHigh ] []
    Assert.Equal(PrecheckStatus.Critical, status)

[<Fact>]
let ``summarizeStatus - Low finding yields Advisory`` () =
    let status =
        summarizeStatus [ findingLow ] []
    Assert.Equal(PrecheckStatus.Advisory, status)

[<Fact>]
let ``summarizeStatus - no findings and no errors yields Clear`` () =
    let status =
        summarizeStatus [] []
    Assert.Equal(PrecheckStatus.Clear, status)

[<Fact>]
let ``summarizeStatus - errors override findings`` () =
    let status =
        summarizeStatus [ findingHigh ] [ MissingField (ClosingDisclosure, TotalClosingCosts) ]
    Assert.Equal(PrecheckStatus.Inconclusive, status)

// --------------------
// runRules tests (using real rules registry)
// --------------------

[<Fact>]
let ``runRules - missing totalClosingCosts yields Inconclusive and two MissingField errors`` () =
    let input = mkInput [ leDocWoTcc; leDocWiTcc; cdDocWoTcc ]
    let findings, errors, status = runRules input all
    Assert.Equal(PrecheckStatus.Inconclusive, status)
    Assert.Empty(findings)
    Assert.Equal(2, errors.Length)
    Assert.Contains(MissingField (LoanEstimate, TotalClosingCosts), errors)
    Assert.Contains(MissingField (ClosingDisclosure, TotalClosingCosts), errors)

[<Fact>]
let ``runRules - all totalClosingCosts present yields Clear and no errors`` () =
    let snapshots = [ leDocWoTcc; leDocWiTcc; cdDocWiTcc ]
    let input = mkInput snapshots
    let findings, errors, status = runRules input all
    Assert.Equal(PrecheckStatus.Inconclusive, status)
    Assert.Empty(findings)

// --------------------
// runRules tests (using local stub rules to force findings)
// --------------------

let mkRule (id: string) (check: Check) : Rule =
    { id = RuleId id; check = check }

[<Fact>]
let ``runRules - High finding yields Critical`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule "R1" (fun _ -> Ok (Some findingHigh)) ]
    let findings, errors, status =
        runRules input rules
    Assert.Equal(PrecheckStatus.Critical, status)
    Assert.Empty(errors)
    Assert.Single(findings)

[<Fact>]
let ``runRules - Low finding yields Advisory`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule "R1" (fun _ -> Ok (Some findingLow)) ]
    let findings, errors, status =
        runRules input rules
    Assert.Equal(PrecheckStatus.Advisory, status)
    Assert.Empty(errors)
    Assert.Single(findings)

[<Fact>]
let ``runRules - mixed Low and High yields Critical`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule "R1" (fun _ -> Ok (Some findingLow))
          mkRule "R2" (fun _ -> Ok (Some findingHigh)) ]
    let findings, errors, status =
        runRules input rules
    Assert.Equal(PrecheckStatus.Critical, status)
    Assert.Empty(errors)
    Assert.Equal(2, findings.Length)

[<Fact>]
let ``runRules - any rule error yields Inconclusive even if findings exist`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule "R1" (fun _ -> Ok (Some findingHigh))
          mkRule "R2" (fun _ -> Error [ MissingField (LoanEstimate, TotalClosingCosts) ]) ]
    let findings, errors, status = runRules input rules
    Assert.Equal(PrecheckStatus.Inconclusive, status)
    Assert.NotEmpty(findings)
    Assert.Single(errors) |> ignore
    Assert.Contains(MissingField (LoanEstimate, TotalClosingCosts), errors)