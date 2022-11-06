package kr.dove.neo4j.service

import kr.dove.neo4j.api.PressService
import kr.dove.neo4j.entity.node.press.PressEntity
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class PressServiceImpl(
    private val reactiveNeo4jTemplate: ReactiveNeo4jTemplate,
) : PressService {

    override fun getPresses(): Flux<PressEntity> {
        return reactiveNeo4jTemplate.findAll(PressEntity::class.java)
    }

    override fun getPressByName(name: String): Mono<PressEntity> {
        return reactiveNeo4jTemplate.findById(name, PressEntity::class.java)
    }

    override fun getPressesByMorph(morph: String): Flux<PressEntity> {
        return reactiveNeo4jTemplate.findAll(
            """
                match (k: Keyword where k.morph='$morph')-[w :WRITTEN]->(p :Press) return p, w, k;
            """.trimIndent(),
            PressEntity::class.java
        )
    }
}