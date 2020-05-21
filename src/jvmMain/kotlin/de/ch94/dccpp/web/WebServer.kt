package de.ch94.dccpp.web

import de.ch94.dccpp.BareBaseStation
import de.ch94.dccpp.BaseStation
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*

fun main() {
    BareBaseStation().use { baseStation ->
        embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
            routing {
                get("/") {
                    call.respondHtml {
                        head {
                            title("Hello from Ktor!")
                        }
                        body {
                            +"Hello from Ktor. Check me value: 0"
                            div {
                                id = "js-response"
                                +"Loading..."
                            }
                            baseStation.switchPower()
                            script(src = "/static/dccpp-web-controller.js") {}
                        }
                    }
                }
                static("/static") {
                    resource("dccpp-web-controller.js")
                }
            }
        }.start(wait = true)
    }
}