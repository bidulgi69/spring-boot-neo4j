package kr.dove.neo4j.entity.node.press

import kr.dove.neo4j.entity.relationship.WrittenRelationship
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction

//  언론사
@Node("Press")
data class PressEntity(
    @Id val name: String,
    //  KEYWORD(NNP)-[:WRITTEN]->PRESS
    @Relationship(type = "WRITTEN", direction = Direction.INCOMING) val articles: MutableSet<WrittenRelationship> = HashSet()
)