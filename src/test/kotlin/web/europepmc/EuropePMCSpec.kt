package web.europepmc

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

class EuropePMCSpec {
    //  data_search = {
    //        "query": f"{query}",
    //        "resultType" : "lite",
    //        "synonymn": "",
    //        "cursorMark": "*",
    //        "pageSize": "1000", #valid page size 1-1000 only
    //        "sort": "",
    //        "format": "json",
    //        "callback": "",
    //        "email": "",
    //    }

    fun getAllExistingIds(): List<Int> {
        return File("./src/test/resources/europepmc/").listFiles()!!.map {
            val name = it.name
            check(name.endsWith(".json"))
            val num = name.substring(0, name.length - 5).toInt()
            num
        }.sorted()
    }

    @Test fun `loading existing files`() {
        val savedRequestIds = getAllExistingIds()

        for (id in savedRequestIds) {
            val text = File("./src/test/resources/europepmc/$id.json").readText()

//            val r = Json.decodeFromString<Response>(text)
//            Json.encodeToString(r)

            try {
                val r = Json.decodeFromString<EuropePMC.Response>(text)
                Json.encodeToString(r)
            } catch (e: Exception) {
                println("Error at $id: $e")
            }
        }
    }

    @Ignore
    @Test fun test() {
        val client = HttpClient(CIO)
        val savedRequestIds = getAllExistingIds()
        val json = Json {
            prettyPrint = true
        }

        runBlocking {
            val response =
                client.get("https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=Pharmacogenomics&resultType=core&format=json&pageSize=100").bodyAsText()

            var i = 1
            while (i in savedRequestIds) i += 1
            val j = Json.decodeFromString<JsonElement>(response)
            File("./src/test/resources/europepmc/$i.json").writeText(json.encodeToString(j))
        }
    }
}
