package kr.dove.neo4j.api

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Flux

interface KeywordService {

    @GetMapping(
        value = ["/keyword", "/keyword/"],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun getKeywords(): Flux<KeywordEntity>
}