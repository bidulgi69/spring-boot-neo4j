package kr.dove.neo4j.api

import kr.dove.neo4j.entity.node.press.PressEntity
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PressService {

    @GetMapping(
        value = ["/press", "/press/"],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun getPresses(): Flux<PressEntity>

    @GetMapping(
        value = ["/press/{name}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getPressByName(@PathVariable(name = "name") name: String): Mono<PressEntity>

    @GetMapping(
        value = ["/press/keyword/{morph}"],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun getPressesByMorph(@PathVariable(name = "morph") morph: String): Flux<PressEntity>
}