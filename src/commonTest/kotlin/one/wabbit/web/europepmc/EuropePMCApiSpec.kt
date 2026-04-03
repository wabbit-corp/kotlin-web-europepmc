package one.wabbit.web.europepmc

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EuropePMCApiSpec {
    @Test
    fun `search applies parameters and decodes search response`() = runTest {
        val api =
            KtorEuropePMCApi(
                httpClient =
                    testClient { request ->
                        assertEquals("/europepmc/webservices/rest/search", request.url.encodedPath)
                        assertEquals("p53", request.url.parameters["query"])
                        assertEquals("lite", request.url.parameters["resultType"])
                        assertEquals("*", request.url.parameters["cursorMark"])
                        assertEquals("10", request.url.parameters["pageSize"])
                        assertEquals("false", request.url.parameters["synonym"])
                        assertEquals("json", request.url.parameters["format"])

                        respondJson(
                            """
                            {
                              "version": "6.9",
                              "hitCount": 402575,
                              "nextCursorMark": "AoIIQCQGGyg1MzgyMzI5OQ==",
                              "nextPageUrl": "https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=p53",
                              "request": {
                                "queryString": "p53",
                                "resultType": "lite",
                                "cursorMark": "*",
                                "pageSize": 10,
                                "sort": "",
                                "synonym": false
                              },
                              "resultList": {
                                "result": [
                                  {
                                    "id": "IND609221141",
                                    "source": "AGR",
                                    "title": "Molecular mechanisms of cold stress",
                                    "pubTypeList": {
                                      "pubType": ["journal article"]
                                    }
                                  }
                                ]
                              }
                            }
                            """.trimIndent(),
                        )
                    },
            )

        val response =
            api.search(
                EuropePMCSearchRequest(
                    query = "p53",
                    resultType = EuropePMCResultType.Lite,
                    pageSize = 10,
                ),
            )

        assertEquals(402575, response.hitCount)
        assertEquals("IND609221141", response.resultList.result.single().id)
    }

    @Test
    fun `article decodes official article wrapper`() = runTest {
        val api =
            KtorEuropePMCApi(
                httpClient =
                    testClient { request ->
                        assertEquals("/europepmc/webservices/rest/article/MED/21494379", request.url.encodedPath)
                        assertEquals("core", request.url.parameters["resultType"])
                        assertEquals("json", request.url.parameters["format"])

                        respondJson(
                            """
                            {
                              "version": "6.9",
                              "hitCount": 1,
                              "request": {
                                "resultType": "CORE",
                                "id": "21494379",
                                "source": "MED"
                              },
                              "result": {
                                "id": "21494379",
                                "source": "MED",
                                "pmid": "21494379",
                                "title": "Fluoride concentration of some brands of fermented milks available in the market.",
                                "pubTypeList": {
                                  "pubType": ["research-article", "Journal Article"]
                                }
                              }
                            }
                            """.trimIndent(),
                        )
                    },
            )

        val response = api.article(EuropePMCArticleLookup(source = "med", id = "21494379"))

        assertEquals("21494379", response.result.id)
        assertEquals("MED", response.request.source)
    }

    @Test
    fun `search rejects invalid page size`() = runTest {
        val api =
            KtorEuropePMCApi(
                httpClient =
                    testClient {
                        error("request should not be made for invalid input")
                    },
            )

        assertFailsWith<EuropePMCError.InvalidInput> {
            api.search(EuropePMCSearchRequest(query = "p53", pageSize = 0))
        }
    }

    @Test
    fun `article maps http failures to typed error`() = runTest {
        val api =
            KtorEuropePMCApi(
                httpClient =
                    testClient {
                        respond(
                            content = "not found",
                            status = HttpStatusCode.NotFound,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
                        )
                    },
            )

        val error =
            assertFailsWith<EuropePMCError.Http> {
                api.article(EuropePMCArticleLookup(source = "MED", id = "missing"))
            }

        assertEquals(404, error.status)
    }

    private fun testClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient =
        HttpClient(MockEngine(handler)) {
            install(HttpTimeout)
        }

    private fun MockRequestHandleScope.respondJson(content: String): HttpResponseData =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
}
