package kr.dove.neo4j.entity.node.press

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository

interface PressRepository : ReactiveNeo4jRepository<PressEntity, String> {
}