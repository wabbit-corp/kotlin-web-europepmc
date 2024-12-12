package web.europepmc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder

object EuropePMC {
    // https://github.com/maryletteroa/europmc-scripts/blob/master/scripts/search_europmc_api.py
    // https://github.com/lubianat/node-europmc
    // https://www.ebi.ac.uk/europepmc/webservices/rest
    // https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=COVID-19&resultType=core&format=json

    @Serializable data class Response(
        val version: String,
        val hitCount: Int,
        val nextCursorMark: String,
        val nextPageUrl: String,
        val request: Request,
        val resultList: ResultList
    )

    @Serializable data class ResultList(
        val result: List<Result>
    )

    @Serializable data class Request(
        val queryString: String,
        val resultType: String,
        val cursorMark: String,
        val pageSize: Int,
        val sort: String,
        val synonym: Boolean,
    )

    @Serializable data class Result(
        val id: String,
        val source: String,

        val pmid: String? = null,
        val pmcid: String? = null,
        val doi: String? = null,

        // "fullTextIdList": {
        //          "fullTextId": [
        //            "PMC11022993"
        //          ]
        //        },
        val fullTextIdList: FullTextIdList? = null,

        val title: String,
        val authorString: String? = null,
        val authorList: AuthorList? = null,
        // "authorIdList": {
        //          "authorId": [
        //            {
        //              "type": "ORCID",
        //              "value": "0000-0002-3097-0038"
        //            }
        //          ]
        //        },
        val authorIdList: AuthorIdList? = null,
        //        "dataLinksTagsList": {
        //          "dataLinkstag": [
        //            "altmetrics"
        //          ]
        //        },
        val dataLinksTagsList: DataLinksTagsList? = null,
        val journalInfo: JournalInfo? = null,
        val pubYear: String? = null,
        val pageInfo: String? = null,
        val abstractText: String? = null,
        // "affiliation": "Qaujigiartiit Health Research Centre, Nunavut, Canada.",
        val affiliation: String? = null,
        // "publicationStatus": "ppublish",
        val publicationStatus: String? = null,
        val language: String? = null,
        val pubModel: String? = null,
        val pubTypeList: PubTypeList,
        // "grantsList": {
        //          "grant": [
        //            {
        //              "grantId": "ZIA MD000019",
        //              "agency": "Intramural NIH HHS",
        //              "acronym": "ImNIH",
        //              "orderIn": 0
        //            }
        //          ]
        //        },
        val grantsList: GrantsList? = null,

        //        "meshHeadingList": {
        //          "meshHeading": [
        //            {
        //              "majorTopic_YN": "N",
        //              "descriptorName": "Humans"
        //            },
        //            {
        //              "majorTopic_YN": "N",
        //              "descriptorName": "Adult"
        //            },
        //            {
        //              "majorTopic_YN": "N",
        //              "descriptorName": "United States",
        //              "meshQualifierList": {
        //                "meshQualifier": [
        //                  {
        //                    "abbreviation": "EP",
        //                    "qualifierName": "epidemiology",
        //                    "majorTopic_YN": "N"
        //                  }
        //                ]
        //              }
        //            },
        //            {
        //              "majorTopic_YN": "Y",
        //              "descriptorName": "Healthcare Disparities"
        //            },
        //            {
        //              "majorTopic_YN": "Y",
        //              "descriptorName": "COVID-19",
        //              "meshQualifierList": {
        //                "meshQualifier": [
        //                  {
        //                    "abbreviation": "PC",
        //                    "qualifierName": "prevention & control",
        //                    "majorTopic_YN": "N"
        //                  }
        //                ]
        //              }
        //            },
        //            {
        //              "majorTopic_YN": "Y",
        //              "descriptorName": "COVID-19 Vaccines",
        //              "meshQualifierList": {
        //                "meshQualifier": [
        //                  {
        //                    "abbreviation": "TU",
        //                    "qualifierName": "therapeutic use",
        //                    "majorTopic_YN": "N"
        //                  }
        //                ]
        //              }
        //            },
        //            {
        //              "majorTopic_YN": "N",
        //              "descriptorName": "Ethnicity"
        //            },
        //            {
        //              "majorTopic_YN": "N",
        //              "descriptorName": "Racial Groups"
        //            }
        //          ]
        //        },
        val meshHeadingList: MeshHeadingList? = null,

        //        "chemicalList": {
        //          "chemical": [
        //            {
        //              "name": "COVID-19 Vaccines",
        //              "registryNumber": "0"
        //            }
        //          ]
        //        },
        val chemicalList: ChemicalList? = null,

        // "keywordList": {
        //          "keyword": [
        //            "Arctic",
        //            "Indigenous",
        //            "Pandemic",
        //            "Case Study",
        //            "Local Knowledge",
        //            "Community-based Research",
        //            "Inuit",
        //            "Sámi",
        //            "Collaborative Approach",
        //            "Covid-19"
        //          ]
        //        },
        val keywordList: KeywordList? = null,
        // "subsetList": {
        //          "subset": [
        //            {
        //              "code": "IM",
        //              "name": "Index Medicus"
        //            }
        //          ]
        //        },
        val subsetList: SubsetList? = null,

        // "bookOrReportDetails": {
        //          "publisher": "medRxiv",
        //          "yearOfPublication": 2024
        //        },
        val bookOrReportDetails: BookOrReportDetails? = null,

        // "fullTextUrlList": {
        //          "fullTextUrl": [
        //            {
        //              "availability": "Open access",
        //              "availabilityCode": "OA",
        //              "documentStyle": "pdf",
        //              "site": "Unpaywall",
        //              "url": "https://www.tandfonline.com/doi/pdf/10.1080/22423982.2024.2341990?needAccess=true"
        //            },
        //            {
        //              "availability": "Subscription required",
        //              "availabilityCode": "S",
        //              "documentStyle": "doi",
        //              "site": "DOI",
        //              "url": "https://doi.org/10.1080/22423982.2024.2341990"
        //            },
        //            {
        //              "availability": "Open access",
        //              "availabilityCode": "OA",
        //              "documentStyle": "html",
        //              "site": "Europe_PMC",
        //              "url": "https://europepmc.org/articles/PMC11057456"
        //            },
        //            {
        //              "availability": "Open access",
        //              "availabilityCode": "OA",
        //              "documentStyle": "pdf",
        //              "site": "Europe_PMC",
        //              "url": "https://europepmc.org/articles/PMC11057456?pdf=render"
        //            }
        //          ]
        //        },
        val fullTextUrlList: FullTextUrlList? = null,

        // "commentCorrectionList": {
        //          "commentCorrection": [
        //            {
        //              "id": "38605817",
        //              "source": "MED",
        //              "reference": "F1000Research. \n    2023 ;12:1007",
        //              "type": "Preprint of",
        //              "note": "Link created based on a title-first author match",
        //              "orderIn": 10001
        //            }
        //          ]
        //        },
        val commentCorrectionList: CommentCorrectionList? = null,

        // "license": "cc by-nc-nd",
        val license: String? = null,

        // "versionList": {
        //          "version": [
        //            {
        //              "id": "PPR707121",
        //              "source": "PPR",
        //              "firstPublishDate": "2023-08-21",
        //              "versionNumber": 1,
        //              "pubTypeList": {
        //                "pubType": [
        //                  "preprint"
        //                ]
        //              },
        //              "hasEvaluations": "N"
        //            },
        //            {
        //              "id": "PPR763329",
        //              "source": "PPR",
        //              "firstPublishDate": "2023-11-22",
        //              "versionNumber": 2,
        //              "pubTypeList": {
        //                "pubType": [
        //                  "preprint"
        //                ]
        //              },
        //              "hasEvaluations": "N"
        //            },
        //            {
        //              "id": "PPR818756",
        //              "source": "PPR",
        //              "firstPublishDate": "2024-03-12",
        //              "versionNumber": 3,
        //              "pubTypeList": {
        //                "pubType": [
        //                  "preprint"
        //                ]
        //              },
        //              "hasEvaluations": "N"
        //            },
        //            {
        //              "id": "PPR838108",
        //              "source": "PPR",
        //              "firstPublishDate": "2024-04-16",
        //              "versionNumber": 4,
        //              "pubTypeList": {
        //                "pubType": [
        //                  "preprint"
        //                ]
        //              },
        //              "hasEvaluations": "N"
        //            }
        //          ]
        //        },
        val versionList: VersionList? = null,

        // "versionNumber": 4,
        val versionNumber: Int? = null,

        val hasEvaluations: PMCBool,
        // "isOpenAccess": "Y",
        //        "inEPMC": "Y",
        //        "inPMC": "N",
        //        "hasPDF": "Y",
        //        "hasBook": "N",
        //        "hasSuppl": "N",
        //        "citedByCount": 0,
        //        "hasData": "N",
        //        "hasReferences": "Y",
        //        "hasTextMinedTerms": "Y",
        //        "hasDbCrossReferences": "N",
        val isOpenAccess: PMCBool,
        val inEPMC: PMCBool,
        val inPMC: PMCBool,
        val hasPDF: PMCBool,
        val hasBook: PMCBool,
        val hasSuppl: PMCBool,
        val citedByCount: Int,
        val hasData: PMCBool,
        val hasReferences: PMCBool,
        val hasTextMinedTerms: PMCBool,
        val hasDbCrossReferences: PMCBool,
        val hasLabsLinks: PMCBool,
        val authMan: PMCBool,
        val epmcAuthMan: PMCBool,
        val nihAuthMan: PMCBool,
        // "manuscriptId": "NIHMS1960696",
        val manuscriptId: String? = null,
        // "hasTMAccessionNumbers": "Y",
        val hasTMAccessionNumbers: PMCBool,
        //        "tmAccessionTypeList": {
        //          "accessionType": [
        //            "omim",
        //            "nct",
        //            "doi"
        //          ]
        //        },
        val tmAccessionTypeList: TMAccessionTypeList? = null,

        // "dbCrossReferenceList": {
        //                    "dbName": [
        //                        "EMBL"
        //                    ]
        //                },
        val dbCrossReferenceList: DbCrossReferenceList? = null,

        val embargoDate: PMCDate? = null,

        val dateOfCompletion: PMCDate? = null,
        val dateOfCreation: PMCDate,
        val firstIndexDate: PMCDate,
        // fullTextReceivedDate": "2024-04-26",
        val fullTextReceivedDate: PMCDate? = null,
        val dateOfRevision: PMCDate? = null,
        val firstPublicationDate: PMCDate,
        // "electronicPublicationDate": "2024-04-26",
        val electronicPublicationDate: PMCDate? = null
    )

    @Serializable(with= PMCDate.Serializer::class) data class PMCDate(val year: Int, val month: Int, val day: Int) {
        class Serializer : KSerializer<PMCDate> {
            override val descriptor = PrimitiveSerialDescriptor("PMCDate", PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): PMCDate {
                val parts = decoder.decodeString().split("-")
                return PMCDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            }
            override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: PMCDate) {
                encoder.encodeString("${value.year}-${value.month}-${value.day}")
            }
        }
    }

    @Serializable(with= PMCBool.Serializer::class) @JvmInline value class PMCBool(val value: Boolean) {
        class Serializer : KSerializer<PMCBool> {
            override val descriptor = PrimitiveSerialDescriptor("PMCBool", PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): PMCBool {
                val strValue = decoder.decodeString()
                check(strValue == "Y" || strValue == "N")
                return PMCBool(strValue == "Y")
            }
            override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: PMCBool) {
                encoder.encodeString(if (value.value) "Y" else "N")
            }
        }
    }

    @Serializable data class DbCrossReferenceList(
        val dbName: List<String>
    )

    @Serializable data class TMAccessionTypeList(
        val accessionType: List<String>
    )

    @Serializable data class VersionList(
        val version: List<Version>
    )

    @Serializable data class Version(
        val id: String,
        val source: String,
        val firstPublishDate: PMCDate,
        val versionNumber: Int,
        val pubTypeList: PubTypeList,
        val hasEvaluations: String
    )

    @Serializable data class CommentCorrectionList(
        val commentCorrection: List<CommentCorrection>
    )

    @Serializable data class CommentCorrection(
        val id: String,
        val source: String,
        val type: String,
        val reference: String? = null,
        val note: String? = null,
        val orderIn: Int
    )

    @Serializable data class BookOrReportDetails(
        val publisher: String,
        val yearOfPublication: Int
    )

    @Serializable data class GrantsList(
        val grant: List<Grant>
    )

    @Serializable data class Grant(
        val grantId: String? = null,
        val agency: String,
        val acronym: String? = null,
        val orderIn: Int
    )

    @Serializable data class MeshHeadingList(
        val meshHeading: List<MeshHeading>
    )

    @Serializable data class MeshHeading(
        val majorTopic_YN: String,
        val descriptorName: String,
        val meshQualifierList: MeshQualifierList? = null
    )

    @Serializable data class MeshQualifierList(
        val meshQualifier: List<MeshQualifier>
    )

    @Serializable data class MeshQualifier(
        val abbreviation: String,
        val qualifierName: String,
        val majorTopic_YN: String
    )

    @Serializable data class ChemicalList(
        val chemical: List<Chemical>
    )

    @Serializable data class Chemical(
        val name: String,
        val registryNumber: String
    )

    @Serializable data class DataLinksTagsList(
        val dataLinkstag: List<String>
    )

    @Serializable data class AuthorIdList(
        val authorId: List<AuthorId>
    )

    @Serializable data class FullTextIdList(
        val fullTextId: List<String>
    )

    @Serializable data class FullText(
        val availability: String,
        val availabilityCode: String,
        val documentStyle: String,
        val site: String,
        val url: String,
    )

    @Serializable data class FullTextUrlList(
        val fullTextUrl: List<FullTextUrl>
    )

    @Serializable data class FullTextUrl(
        val availability: String,
        val availabilityCode: String,
        val documentStyle: String,
        val site: String,
        val url: String,
    )

    @Serializable data class KeywordList(
        val keyword: List<String>
    )

    @Serializable data class AuthorList(
        val author: List<Author>
    )

    @Serializable data class SubsetList(
        val subset: List<Subset>
    )

    @Serializable data class Subset(
        val code: String,
        val name: String
    )

    @Serializable data class Author(
        val fullName: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val initials: String? = null,
        val authorAffiliationDetailsList: AuthorAffiliationDetailsList? = null,
        // "authorId": {
        //                "type": "ORCID",
        //                "value": "0000-0002-3535-2271"
        //              }
        val authorId: AuthorId? = null,

        // OR

        val collectiveName: String? = null
    )

    @Serializable data class AuthorId(
        val type: String,
        val value: String,
    )

    @Serializable data class AuthorAffiliationDetailsList(
        val authorAffiliation: List<AuthorAffiliation>
    )

    @Serializable data class AuthorAffiliation(
        val affiliation: String
    )

    @Serializable data class JournalInfo(
        val issue: String? = null,
        val volume: String? = null,
        val journalIssueId: Int,
        val dateOfPublication: String,
        val monthOfPublication: Int,
        val yearOfPublication: Int,
        val printPublicationDate: String? = null,
        val journal: Journal,
    )

    // "title": "PloS one",
    //            "medlineAbbreviation": "PLoS One",
    //            "essn": "1932-6203",
    //            "issn": "1932-6203",
    //            "isoabbreviation": "PLoS One",
    //            "nlmid": "101285081"
    @Serializable data class Journal(
        val title: String,
        val medlineAbbreviation: String,
        val issn: String? = null,
        val essn: String? = null,
        val isoabbreviation: String? = null,
        val nlmid: String? = null,
    )

    @Serializable data class PubTypeList(
        val pubType: List<String>
    )
}
