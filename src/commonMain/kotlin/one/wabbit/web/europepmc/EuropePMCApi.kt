package one.wabbit.web.europepmc

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json
import one.wabbit.web.common.Etiquette
import one.wabbit.web.common.Timeouts
import one.wabbit.web.common.applyEtiquette
import one.wabbit.web.common.applyTimeouts
import one.wabbit.web.common.responseBodySampleOrNull
import one.wabbit.web.common.retryingIdempotentHttpCall
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

sealed class EuropePMCError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidInput(message: String) : EuropePMCError(message)

    class Http(
        val url: String,
        val status: Int,
        val bodySample: String?,
        cause: Throwable? = null,
    ) : EuropePMCError(
        buildString {
            append("HTTP ")
            append(status)
            append(" from ")
            append(url)
            if (!bodySample.isNullOrBlank()) {
                append(", body sample: ")
                append(bodySample.take(256))
            }
        },
        cause,
    )

    class Network(
        val url: String,
        cause: Throwable,
    ) : EuropePMCError(
        "Network failure talking to $url: ${cause::class.simpleName}: ${cause.message}",
        cause,
    )

    class Parse(
        val url: String,
        val bodySample: String,
        cause: Throwable,
    ) : EuropePMCError(
        "Failed to parse Europe PMC response from $url: ${cause::class.simpleName}: ${cause.message}; body sample: ${bodySample.take(256)}",
        cause,
    )
}

enum class EuropePMCResultType(val apiValue: String) {
    Lite("lite"),
    Core("core"),
}

data class EuropePMCSearchRequest(
    val query: String,
    val resultType: EuropePMCResultType = EuropePMCResultType.Lite,
    val cursorMark: String = "*",
    val pageSize: Int = 25,
    val sort: String? = null,
    val synonym: Boolean = false,
    val email: String? = null,
)

data class EuropePMCArticleLookup(
    val source: String,
    val id: String,
    val resultType: EuropePMCResultType = EuropePMCResultType.Core,
)

interface EuropePMCApi {
    data class Config(
        val baseUrl: String = "https://www.ebi.ac.uk/europepmc/webservices/rest",
        val etiquette: Etiquette = Etiquette("one.wabbit.web.europepmc/2.1"),
        val timeouts: Timeouts = Timeouts(
            request = 30.seconds,
            connect = 30.seconds,
            socket = 30.seconds,
        ),
    ) {
        init {
            require(baseUrl.isNotBlank()) { "baseUrl must not be blank" }
        }
    }

    suspend fun search(request: EuropePMCSearchRequest): EuropePMC.Response

    suspend fun article(lookup: EuropePMCArticleLookup): EuropePMC.ArticleResponse
}

class KtorEuropePMCApi(
    val httpClient: HttpClient,
    val config: EuropePMCApi.Config = EuropePMCApi.Config(),
) : EuropePMCApi {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    init {
        check(runCatching { httpClient.pluginOrNull(HttpTimeout) }.getOrNull() != null) {
            "HttpTimeout plugin must be installed on the provided HttpClient for per-request timeouts to work."
        }
    }

    override suspend fun search(request: EuropePMCSearchRequest): EuropePMC.Response {
        val normalized = request.normalized()
        val url = "${config.baseUrl}/search"
        val body =
            requestBody(url) {
                parameter("query", normalized.query)
                parameter("resultType", normalized.resultType.apiValue)
                parameter("format", "json")
                parameter("cursorMark", normalized.cursorMark)
                parameter("pageSize", normalized.pageSize)
                normalized.sort?.let { parameter("sort", it) }
                parameter("synonym", normalized.synonym)
                normalized.email?.let { parameter("email", it) }
            }

        return decode(url, body)
    }

    override suspend fun article(lookup: EuropePMCArticleLookup): EuropePMC.ArticleResponse {
        val normalized = lookup.normalized()
        val url = "${config.baseUrl}/article/${normalized.source}/${normalized.id}"
        val body =
            requestBody(url) {
                parameter("resultType", normalized.resultType.apiValue)
                parameter("format", "json")
            }

        return decode(url, body)
    }

    private suspend fun requestBody(
        url: String,
        configure: io.ktor.client.request.HttpRequestBuilder.() -> Unit,
    ): String {
        val response = try {
            retryingIdempotentHttpCall {
                httpClient.get(url) {
                    expectSuccess = true
                    applyEtiquette(config.etiquette)
                    applyTimeouts(config.timeouts)
                    accept(ContentType.Application.Json)
                    configure()
                }
            }
        } catch (t: Throwable) {
            throw t.toEuropePMCError(url)
        }

        return response.readBody(url)
    }

    private suspend inline fun <reified T> decode(url: String, body: String): T =
        try {
            json.decodeFromString<T>(body)
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            throw EuropePMCError.Parse(url, body.take(2048), t)
        }

    private suspend fun HttpResponse.readBody(url: String): String =
        try {
            bodyAsText()
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            throw EuropePMCError.Network(url, t)
        }
}

private fun EuropePMCSearchRequest.normalized(): EuropePMCSearchRequest {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) {
        throw EuropePMCError.InvalidInput("query must not be blank")
    }
    if (pageSize !in 1..1000) {
        throw EuropePMCError.InvalidInput("pageSize must be between 1 and 1000")
    }
    val normalizedCursor = cursorMark.trim().ifEmpty { "*" }
    val normalizedSort = sort?.trim()?.takeIf { it.isNotEmpty() }
    val normalizedEmail = email?.trim()?.takeIf { it.isNotEmpty() }
    return copy(
        query = normalizedQuery,
        cursorMark = normalizedCursor,
        sort = normalizedSort,
        email = normalizedEmail,
    )
}

private fun EuropePMCArticleLookup.normalized(): EuropePMCArticleLookup {
    val normalizedSource = source.trim().uppercase()
    val normalizedId = id.trim()
    if (normalizedSource.isEmpty()) {
        throw EuropePMCError.InvalidInput("source must not be blank")
    }
    if (normalizedId.isEmpty()) {
        throw EuropePMCError.InvalidInput("id must not be blank")
    }
    return copy(source = normalizedSource, id = normalizedId)
}

private suspend fun Throwable.toEuropePMCError(url: String): EuropePMCError {
    if (this is CancellationException) throw this
    return if (this is ResponseException) {
        val sample = responseBodySampleOrNull()
        EuropePMCError.Http(url, response.status.value, sample, this)
    } else {
        EuropePMCError.Network(url, this)
    }
}
