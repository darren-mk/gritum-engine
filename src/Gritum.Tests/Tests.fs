module Tests

open System.IO
open Xunit

[<Fact>]
let ``My test`` () =
    let path = Path.Combine("data", "samples", "encompass-loan.json")
    let json = File.ReadAllText path
    Assert.True(json.Length > 1)
