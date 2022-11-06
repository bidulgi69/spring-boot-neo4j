package kr.dove.neo4j.api

data class Article(
    val uri: String,
    val headline: String,
    val press: String,
    val editor: String? = null,
    var morphs: MutableSet<String> = mutableSetOf()
)