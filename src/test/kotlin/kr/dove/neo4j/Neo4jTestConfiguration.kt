package kr.dove.neo4j

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import kr.dove.neo4j.entity.node.press.PressEntity
import kr.dove.neo4j.entity.relationship.WrittenRelationship
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Config
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.DatabaseSelection
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider
import org.springframework.data.neo4j.core.ReactiveNeo4jClient
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

@TestConfiguration
class Neo4jTestConfiguration {

    @Bean
    fun driver(): Driver {
        return GraphDatabase.driver(
            Neo4jTestContainer.CONTAINER.boltUrl,
            Config.builder()
                .withMaxConnectionPoolSize(3)
                .withMaxConnectionLifetime(1L, TimeUnit.MINUTES)
                .build()
        )
    }

    @Bean
    fun databaseSelectionProvider(): DatabaseSelectionProvider {
        return DatabaseSelectionProvider { DatabaseSelection.byName("neo4j") }
    }

    @Bean
    fun reactiveDatabaseSelectionProvider(): ReactiveDatabaseSelectionProvider {
        return ReactiveDatabaseSelectionProvider { Mono.just(DatabaseSelection.byName("neo4j")) }
    }

    @Bean
    fun neo4jClient(
        driver: Driver,
        databaseSelectionProvider: DatabaseSelectionProvider
    ): Neo4jClient {
        return Neo4jClient.create(driver, databaseSelectionProvider)
    }

    @Bean
    fun reactiveNeo4jClient(
        driver: Driver,
        reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider
    ): ReactiveNeo4jClient {
        return ReactiveNeo4jClient.create(driver, reactiveDatabaseSelectionProvider)
    }

    @Bean("neo4jTestTemplate", "neo4jTemplate")
    fun neo4jTemplate(
        neo4jClient: Neo4jClient
    ): Neo4jTemplate {
        return Neo4jTemplate(neo4jClient)
    }

    @Bean("reactiveNeo4jTestTemplate", "reactiveNeo4jTemplate")
    fun reactiveNeo4jTemplate(
        reactiveNeo4jClient: ReactiveNeo4jClient
    ): ReactiveNeo4jTemplate {
        val context = Neo4jMappingContext()
        val entitySet = mutableSetOf<Class<*>>()
        entitySet.add(PressEntity::class.java)
        entitySet.add(KeywordEntity::class.java)
        entitySet.add(WrittenRelationship::class.java)
        context.setInitialEntitySet(entitySet)

        return ReactiveNeo4jTemplate(reactiveNeo4jClient, context)
    }
}