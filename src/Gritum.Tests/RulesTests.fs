module Gritum.Tests.RulesTests

open System
open Xunit
open Gritum.Model
open Gritum.Rules

// -----------------
// Helpers
// -----------------

let d (y:int) (m:int) (day:int) = DateOnly(y, m, day)

let content (ed: DateOnly option) (tcc: Money option) : DocumentContent =
    { effectiveDate = ed
      totalClosingCosts = tcc }

let le (c: DocumentContent) : DocumentSnapshot = LE c
let cd (c: DocumentContent) : DocumentSnapshot = CD c

// PrecheckInput requires a loan. This rule does not use it, so a dummy is fine.
let dummyLoan : Loan = Unchecked.defaultof<Loan>

let input (snapshots: DocumentSnapshot list) : PrecheckInput =
    { loan = dummyLoan
      documentSnapshots = snapshots }

// -----------------
// compareTotalClosingCosts tests
// -----------------

[<Fact>]
let ``compareTotalClosingCosts - equal values -> Ok None`` () =
    let ledc = content None (Some (Helper.money 100m))
    let cddc = content None (Some (Helper.money 100m))

    let actual = compareTotalClosingCosts ledc cddc

    Assert.Equal<RuleCheckResult>(Ok None, actual)

[<Fact>]
let ``compareTotalClosingCosts - different values -> Ok (Some finding)`` () =
    let ledc = content None (Some (Helper.money 100m))
    let cddc = content None (Some (Helper.money 200m))

    match compareTotalClosingCosts ledc cddc with
    | Ok (Some f) ->
        Assert.Equal(totalClosingCostsRuleId, f.ruleId)
        Assert.Equal(Severity.High, f.severity)
    | x ->
        failwith $"Expected Ok (Some finding), but got: {x}"

[<Fact>]
let ``compareTotalClosingCosts - CD missing field -> Error MissingField(CD, TotalClosingCosts)`` () =
    let ledc = content None (Some (Helper.money 100m))
    let cddc = content None None

    let actual = compareTotalClosingCosts ledc cddc

    Assert.Equal<RuleCheckResult>(
        Error [ MissingField (ClosingDisclosure, TotalClosingCosts) ],
        actual )

[<Fact>]
let ``compareTotalClosingCosts - LE missing field -> Error MissingField(LE, TotalClosingCosts)`` () =
    let ledc = content None None
    let cddc = content None (Some (Helper.money 100m))

    let actual = compareTotalClosingCosts ledc cddc

    Assert.Equal<RuleCheckResult>(
        Error [ MissingField (LoanEstimate, TotalClosingCosts) ],
        actual )

[<Fact>]
let ``compareTotalClosingCosts - both missing field -> Error both MissingField`` () =
    let ledc = content None None
    let cddc = content None None

    let actual = compareTotalClosingCosts ledc cddc

    Assert.Equal<RuleCheckResult>(
        Error [ MissingField (LoanEstimate, TotalClosingCosts)
                MissingField (ClosingDisclosure, TotalClosingCosts) ],
        actual )

// -----------------
// selectEffectiveLE tests
// -----------------

[<Fact>]
let ``selectEffectiveLE - picks max effectiveDate regardless of input order`` () =
    let older = content (Some (d 2025 1 1)) (Some (Helper.money 10m))
    let newer = content (Some (d 2025 2 1)) (Some (Helper.money 20m))

    let snapshots =
        [ le newer
          le older
          cd (content (Some (d 2025 3 1)) (Some (Helper.money 999m))) ] // should be ignored

    let picked = selectEffectiveLE snapshots

    Assert.Equal(Some newer, picked)

[<Fact>]
let ``selectEffectiveLE - ignores LE with None effectiveDate`` () =
    let noDate = content None (Some (Helper.money 10m))
    let dated = content (Some (d 2025 2 1)) (Some (Helper.money 20m))

    let snapshots = [ le noDate; le dated ]

    let picked = selectEffectiveLE snapshots

    Assert.Equal(Some dated, picked)

[<Fact>]
let ``selectEffectiveLE - returns None when no dated LE exists`` () =
    let snapshots =
        [ le (content None (Some (Helper.money 10m)))
          le (content None (Some (Helper.money 20m))) ]

    let picked = selectEffectiveLE snapshots

    Assert.Equal(None, picked)

// -----------------
// checkTotalClosingCosts tests
// -----------------

[<Fact>]
let ``checkTotalClosingCosts - ok when both have TCC and values equal`` () =
    let le1 = content (Some (d 2025 1 1)) (Some (Helper.money 100m))
    let le2 = content (Some (d 2025 2 1)) (Some (Helper.money 100m)) // effective LE
    let cd1 = content (Some (d 2025 3 1)) (Some (Helper.money 100m))

    let p = input [ le le1; le le2; cd cd1 ]

    let actual = checkTotalClosingCosts p

    Assert.Equal<RuleCheckResult>(Ok None, actual)

[<Fact>]
let ``checkTotalClosingCosts - finding when both have TCC and values differ`` () =
    let le1 = content (Some (d 2025 2 1)) (Some (Helper.money 100m)) // effective LE
    let cd1 = content (Some (d 2025 3 1)) (Some (Helper.money 200m))

    let p = input [ le le1; cd cd1 ]

    match checkTotalClosingCosts p with
    | Ok (Some f) ->
        Assert.Equal(totalClosingCostsRuleId, f.ruleId)
    | x ->
        failwith $"Expected Ok (Some finding), but got: {x}"

[<Fact>]
let ``checkTotalClosingCosts - Error when any snapshot is missing TotalClosingCosts`` () =
    let le1 = content (Some (d 2025 2 1)) (Some (Helper.money 100m))
    let cd1 = content (Some (d 2025 3 1)) None

    let p = input [ le le1; cd cd1 ]

    let actual = checkTotalClosingCosts p

    Assert.Equal<RuleCheckResult>(
        Error [ MissingField (ClosingDisclosure, TotalClosingCosts) ],
        actual )