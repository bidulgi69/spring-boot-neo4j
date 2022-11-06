package kr.dove.neo4j.service

import kr.dove.neo4j.api.KeywordService
import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class KeywordServiceImpl(
    private val reactiveNeo4jTemplate: ReactiveNeo4jTemplate,
) : KeywordService {

    override fun getKeywords(): Flux<KeywordEntity> {
        return reactiveNeo4jTemplate.findAll(KeywordEntity::class.java)
    }


}