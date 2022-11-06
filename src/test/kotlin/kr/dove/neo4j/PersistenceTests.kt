package kr.dove.neo4j

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import kr.dove.neo4j.entity.node.press.PressEntity
import kr.dove.neo4j.entity.relationship.WrittenRelationship
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier

@ContextConfiguration(classes = [Neo4jTestConfiguration::class])
@DataNeo4jTest
class PersistenceTests(
    @Autowired private val neo4jTemplate: Neo4jTemplate,
    @Autowired private val reactiveNeo4jTestTemplate: ReactiveNeo4jTemplate,
) {

    private var pressEntity: PressEntity? = null
    private var keywordEntity: KeywordEntity? = null
    private var writtenRelationship: WrittenRelationship? = null

    @BeforeEach
    fun setup() {
        reactiveNeo4jTestTemplate
            .deleteAll(WrittenRelationship::class.java)
            .block()
        reactiveNeo4jTestTemplate
            .deleteAll(PressEntity::class.java)
            .block()
        reactiveNeo4jTestTemplate
            .deleteAll(KeywordEntity::class.java)
            .block()

        val press = PressEntity("CNN")
        val keyword = KeywordEntity("Biden")
        val relationship = WrittenRelationship(
            keyword,
            "https://edition.cnn.joke/",
            "bidulgi69"
        )

        //  save press
        StepVerifier
            .create(reactiveNeo4jTestTemplate.save(press))
            .expectNextMatches { createdEntity ->
                pressEntity = createdEntity
                press.isEqualTo(createdEntity)
            }
            .verifyComplete()

        //  save keyword
        StepVerifier
            .create(reactiveNeo4jTestTemplate.save(keyword))
            .expectNextMatches { createdEntity ->
                keywordEntity = createdEntity
                keyword.isEqualTo(createdEntity)
            }
            .verifyComplete()

        //  save relationship
        StepVerifier
            .create(reactiveNeo4jTestTemplate.save(press.apply {
                this.articles.add(relationship)
            }))
            .expectNextMatches { savedEntity ->
                writtenRelationship  = savedEntity.articles.toList()[0]
                relationship.isEqualTo(writtenRelationship!!)
            }
            .verifyComplete()
    }

    @Test
    fun create() {
        val press = "The Washington Post"
        val pressEntity = PressEntity(press)

        StepVerifier.create(reactiveNeo4jTestTemplate.save(pressEntity))
            .expectNextMatches { savedEntity ->
                pressEntity.isEqualTo(savedEntity)
            }
            .verifyComplete()

        StepVerifier.create(reactiveNeo4jTestTemplate.findById(press, PressEntity::class.java))
            .expectNextMatches { foundEntity ->
                pressEntity.isEqualTo(foundEntity)
            }
            .verifyComplete()

        StepVerifier.create(reactiveNeo4jTestTemplate.count(PressEntity::class.java))
            .expectNextMatches { count ->
                2L == count
            }
            .verifyComplete()
    }



    @Test
    fun press_findAll_test() {
        StepVerifier.create(reactiveNeo4jTestTemplate.findAll(PressEntity::class.java).count())
            .expectNextMatches { count ->
                1L == count
            }
            .verifyComplete()

        reactiveNeo4jTestTemplate.save(
            PressEntity("The Washington Post")
        ).block()
        StepVerifier.create(reactiveNeo4jTestTemplate.findAll(PressEntity::class.java).count())
            .expectNextMatches { count ->
                2L == count
            }
            .verifyComplete()
    }

    @Test
    fun keyword_findAll_test() {
        StepVerifier.create(reactiveNeo4jTestTemplate.findAll(KeywordEntity::class.java).count())
            .expectNextMatches { count ->
                1L == count
            }
            .verifyComplete()

        reactiveNeo4jTestTemplate.save(
            KeywordEntity("Interest")
        ).block()
        StepVerifier.create(reactiveNeo4jTestTemplate.findAll(KeywordEntity::class.java).count())
            .expectNextMatches { count ->
                2L == count
            }
            .verifyComplete()
    }


    @Test
    fun press_findById_test() {
        with(pressEntity!!) {
            StepVerifier.create(reactiveNeo4jTestTemplate.findById(name, PressEntity::class.java))
                .expectNextMatches { foundEntity ->
                    this.isEqualTo(foundEntity)
                }
                .verifyComplete()
        }
    }

    @Test
    fun press_findAll_with_cql_test() {
        with(keywordEntity!!) {
            val cypherQuery = "match (k: Keyword where k.morph='$morph')-[w :WRITTEN]->(p :Press) return p, w, k;"
            StepVerifier.create(reactiveNeo4jTestTemplate.findAll(
                cypherQuery,
                PressEntity::class.java
            ))
                .expectNextMatches { foundEntity ->
                    pressEntity!!.isEqualTo(foundEntity)
                }
                .verifyComplete()
        }
    }

    @Test
    fun press_findById_orElseGet() {
        val notExistsPress = "The Washington Post"
        Assertions.assertFalse(neo4jTemplate.findById(notExistsPress, PressEntity::class.java).isPresent)

        val entity = neo4jTemplate.findById(notExistsPress, PressEntity::class.java)
            .orElseGet {
                neo4jTemplate.save(
                    PressEntity(notExistsPress)
                )
            }
        Assertions.assertEquals(notExistsPress, entity.name)
    }

    @Test
    fun keyword_findById_orElseGet() {
        val notExistsMorph = "Ukraine"
        Assertions.assertFalse(neo4jTemplate.findById(notExistsMorph, KeywordEntity::class.java).isPresent)

        val entity = neo4jTemplate.findById(notExistsMorph, KeywordEntity::class.java)
            .orElseGet {
                neo4jTemplate.save(
                    KeywordEntity(notExistsMorph)
                )
            }
        Assertions.assertEquals(notExistsMorph, entity.morph)
    }

    @Test
    fun add_relationship_test() {
        val newMorph = KeywordEntity(
            "Iran"
        )
        neo4jTemplate.save(newMorph)
        Assertions.assertEquals(2L, neo4jTemplate.count(KeywordEntity::class.java))

        with(pressEntity!!) {
            val newRelationship = WrittenRelationship(
                newMorph,
                "https://www.washingtonpost.joke/",
                "bidulgi69"
            )
            articles.add(newRelationship)

            val savedEntity = neo4jTemplate.save(this)
            Assertions.assertEquals(this.name, savedEntity.name)
            Assertions.assertEquals(2, savedEntity.articles.size)
        }
    }

    private fun PressEntity.isEqualTo(o: PressEntity): Boolean = this.name == o.name
    private fun KeywordEntity.isEqualTo(o: KeywordEntity): Boolean = this.morph == o.morph
    private fun WrittenRelationship.isEqualTo(o: WrittenRelationship): Boolean = this.keyword.isEqualTo(o.keyword) && this.uri == o.uri && this.editor == o.editor
}