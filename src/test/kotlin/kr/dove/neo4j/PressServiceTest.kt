package kr.dove.neo4j

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import kr.dove.neo4j.entity.node.press.PressEntity
import kr.dove.neo4j.entity.relationship.WrittenRelationship
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@ContextConfiguration(classes = [Neo4jTestConfiguration::class])
@EnableAutoConfiguration(exclude = [
    DataSourceAutoConfiguration::class  //  exclude spring batch autoconfig
])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PressServiceTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val reactiveNeo4jTemplate: ReactiveNeo4jTemplate,
) {

    private val pressEntity: PressEntity = PressEntity("CNN")

    @BeforeEach
    fun setup() {
        reactiveNeo4jTemplate.deleteAll(WrittenRelationship::class.java).block()
        reactiveNeo4jTemplate.deleteAll(PressEntity::class.java).block()
        reactiveNeo4jTemplate.deleteAll(KeywordEntity::class.java).block()

        val keywordEntity = reactiveNeo4jTemplate.save(KeywordEntity("Worlds")).block()!!
        val relationship = WrittenRelationship(
            keywordEntity,
            "https://www.cnn.joke",
            "bidulgi69"
        )
        reactiveNeo4jTemplate.save(pressEntity).block()
        pressEntity.apply {
            this.articles.add(relationship)
        }

        reactiveNeo4jTemplate.save(pressEntity).block()
    }

    @Test
    fun api_getPresses_test() {
        webTestClient
            .get()
            .uri("/press/")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(PressEntity::class.java)
            .value<WebTestClient.ListBodySpec<PressEntity>> { presses ->
                Assertions.assertEquals(1, presses.size)
                Assertions.assertEquals(pressEntity.name, presses[0].name)
            }
    }

    @Test
    fun api_getPressByName_test() {
        webTestClient
            .get()
            .uri("/press/notExists")
            .exchange()
            .expectStatus().isOk
            .expectBody().isEmpty

        webTestClient
            .get()
            .uri("/press/${pressEntity.name}")
            .exchange()
            .expectStatus().isOk
            .expectBody(PressEntity::class.java)
            .value { foundEntity ->
                Assertions.assertEquals(pressEntity.name, foundEntity.name)
            }
    }

    @Test
    fun api_getPressesByMorph_test() {
        webTestClient
            .get()
            .uri("/press/keyword/Worlds")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(PressEntity::class.java)
            .value<WebTestClient.ListBodySpec<PressEntity>> { presses ->
                Assertions.assertEquals(1, presses.size)
                Assertions.assertEquals(pressEntity.name, presses[0].name)
                Assertions.assertEquals(pressEntity.articles.toList()[0], presses[0].articles.toList()[0])
            }
    }
}