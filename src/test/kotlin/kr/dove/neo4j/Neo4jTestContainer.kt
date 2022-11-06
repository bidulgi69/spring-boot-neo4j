package kr.dove.neo4j

import org.springframework.stereotype.Component
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import javax.annotation.PreDestroy

@Component
abstract class Neo4jTestContainer {
    companion object {
        @PreDestroy
        fun destroy() {
            CONTAINER.stop()
        }

        @Container
        @JvmStatic
        val CONTAINER = Neo4jContainer("neo4j:latest")
            .apply {
                addExposedPort(7474)
                addExposedPort(7687)
                addEnv("NEO4J_dbms_logs_debug_level", "DEBUG")
                addEnv("NEO4J_dbms_memory_pagecache_size", "1G")
                addEnv("NEO4J_dbms.memory.heap.initial_size", "1G")
                addEnv("NEO4J_dbms_memory_heap_max__size", "1G")
                withAdminPassword(null)
            }
            .apply { start() }
    }
}