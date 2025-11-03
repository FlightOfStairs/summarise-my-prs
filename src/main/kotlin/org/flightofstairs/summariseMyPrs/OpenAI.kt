package org.flightofstairs.summariseMyPrs

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun openAiClient(secretKey: String) = HttpClient(CIO) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    defaultRequest {
        url("https://api.openai.com/")
        header(HttpHeaders.Authorization, "Bearer $secretKey")
        contentType(ContentType.Application.Json)
    }
}

@Serializable
data class ResponseRequest(val model: String, val input: String)

@Serializable
data class ResponseObject(val id: String, val output: List<ResponseOutputItem>)

@Serializable
data class ResponseOutputItem(val type: String, val role: String? = null, val content: List<ResponseContent>? = null)

@Serializable
data class ResponseContent(val type: String, val text: String? = null)

suspend fun main() {
    val client = openAiClient(dotenv()["OPENAI_SECRET_KEY"])


    val response = client.post("/v1/responses") {
        setBody(ResponseRequest("gpt-4.1-mini", "Test prompt say hello"))
    }.body<ResponseObject>()

    println(response)
}
