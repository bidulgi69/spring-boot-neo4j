package kr.dove.neo4j.entity.node.keyword

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

//  기사 헤드라인에서 추출된 NNP(명사)
@Node("Keyword")
data class KeywordEntity(
    @Id val morph: String,
)
