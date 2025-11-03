@file:OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)

package org.flightofstairs.summariseMyPrs

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.io.File
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

private val BASE_PROMPT = """
    Your task is to summarize PRs opened by a single employee. You will be given PR details grouped by month, and summaries from previous months - for context. Your task is to produce a set of bullet points describing, at a high level, what that employee did that month. The only output should be bullet points without nesting. This will be used by the employee to remember their previous work during preparation for interviewing. Not all items need to be included in output - just broad themes. Most months should have 3 or fewer bullet points. Bullet points should be no more than one short sentence. Bullet points should each be cohesive, rather than collections of disparate items.
""".trimIndent()

suspend fun main(args: Array<String>) {
    check(args.size == 1) { "Usage: ./gradlew run --args \"prs.json\"" }

    val prs = Json.decodeFromString<List<Pr>>(File(args[0]).readText())

    val perMonth = prs.groupBy { it.createdAt.toJavaInstant().atZone(UTC).withDayOfMonth(1).truncatedTo(DAYS) }
        .toSortedMap()
        .mapValues { (_, v) -> v.sortedBy { it.createdAt } }

    openAiClient(dotenv()["OPENAI_SECRET_KEY"]).use { client ->
        perMonth.entries.fold<Map.Entry<ZonedDateTime, List<Pr>>, String?>(null) { prev, (month, prs) ->

            val previousPrompt = "Previous summaries:\n" + (prev ?: "None - this was their first month of employment.")
            val prPrompt = "PRs created this month:" + "\n\n" + prs.joinToString("\n\n") { Json.encodeToString(it) }
            val prompt = "$BASE_PROMPT\n\n$previousPrompt\n\n$prPrompt"

            val response = client.post("/v1/responses") {
                setBody(ResponseRequest("gpt-5", prompt))
            }.body<ResponseObject>()

            val output = response.output.mapNotNull { it.content }.flatten().mapNotNull { it.text }.joinToString("\n")

            val formattedMonth = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))

            val newOutput = "$formattedMonth\n$output"
            println(newOutput + "\n")

            prev + "$newOutput\n\n"
        }
    }
}

@Serializable
@JsonIgnoreUnknownKeys
data class Repository(val nameWithOwner: String)

@Serializable
@JsonIgnoreUnknownKeys
data class Pr(
    val title: String,
    val body: String,
    val createdAt: Instant,
    val repository: Repository
)
