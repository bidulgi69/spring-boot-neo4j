package kr.dove.neo4j

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
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
    DataSourceAutoConfiguration::class
])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeywordServiceTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val reactiveNeo4jTestTemplate: ReactiveNeo4jTemplate,
) {

    private val keywordEntity = KeywordEntity("Faker")

    @BeforeEach
    fun setup() {
        reactiveNeo4jTestTemplate
            .deleteAll(KeywordEntity::class.java)
            .block()

        reactiveNeo4jTestTemplate
            .save(keywordEntity)
            .block()
    }

    @Test
    fun api_findAll_test() {
        webTestClient
            .get()
            .uri("/keyword/")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(KeywordEntity::class.java)
            .value<WebTestClient.ListBodySpec<KeywordEntity>> { keywords ->
                Assertions.assertEquals(1, keywords.size)
                Assertions.assertEquals(
                    keywordEntity.morph, keywords[0].morph
                )
            }

    }
}