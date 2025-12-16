open Microsoft.AspNetCore.Builder
open Giraffe

let webApp =
    choose [
        route "/health" >=> text "ok"
        route "/" >=> text "gritum"
        setStatusCode 404 >=> text "not found" ]

[<EntryPoint>]
let main args =
    let builder = WebApplication.CreateBuilder args
    let app = builder.Build()
    app.UseGiraffe webApp
    app.Run "http://127.0.0.1:3000"
    0

