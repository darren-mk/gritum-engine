module Helper

open Gritum.Model

let money (x: decimal) : Money =
    Money.create x