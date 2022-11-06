package kr.dove.neo4j.config

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import kr.dove.neo4j.entity.node.press.PressEntity
import kr.dove.neo4j.entity.relationship.WrittenRelationship
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Config
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.*
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories
import org.springframework.transaction.ReactiveTransactionManager
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

@ConditionalOnProperty(value=["otpConfig"], havingValue="production")
@Configuration
@EnableReactiveNeo4jRepositories(basePackages = ["kr.dove.neo4j.entity"])
class Neo4jConfiguration {

    @Bean
    fun driver(properties: Neo4jProperties): Driver {
        val authentication = properties.authentication
        return GraphDatabase.driver(
            properties.uri,
            AuthTokens.basic(
                authentication.username,
                authentication.password),
            Config.builder()
                .withMaxConnectionPoolSize(properties.pool.maxConnectionPoolSize)
                .withMaxConnectionLifetime(1L, TimeUnit.MINUTES)
                .build()
        )
    }

    @Bean
    fun databaseSelectionProvider(): DatabaseSelectionProvider {
        return DatabaseSelectionProvider { getDatabase() }
    }

    @Bean
    fun reactiveDatabaseSelectionProvider(): ReactiveDatabaseSelectionProvider {
        return ReactiveDatabaseSelectionProvider { Mono.just(getDatabase()) }
    }

    @Bean
    fun reactiveTransactionManager(
        driver: Driver,
        reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider
    ): ReactiveTransactionManager {
        return ReactiveNeo4jTransactionManager(driver, reactiveDatabaseSelectionProvider)
    }

    @Bean
    fun reactiveNeo4jClient(
        driver: Driver,
        reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider
    ): ReactiveNeo4jClient {
        return ReactiveNeo4jClient.create(driver, reactiveDatabaseSelectionProvider)
    }

    @Bean
    fun neo4jClient(
        driver: Driver,
        databaseSelectionProvider: DatabaseSelectionProvider
    ): Neo4jClient {
        return Neo4jClient.create(driver, databaseSelectionProvider)
    }

    @Bean
    fun neo4jTemplate(
        neo4jClient: Neo4jClient
    ): Neo4jTemplate {
        return Neo4jTemplate(neo4jClient)
    }

    @Bean
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

    private fun getDatabase(): DatabaseSelection = DatabaseSelection.byName("neo4j")
}