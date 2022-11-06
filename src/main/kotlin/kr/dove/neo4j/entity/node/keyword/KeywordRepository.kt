package kr.dove.neo4j.entity.node.keyword

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository

interface KeywordRepository : ReactiveNeo4jRepository<KeywordEntity, String> {
}