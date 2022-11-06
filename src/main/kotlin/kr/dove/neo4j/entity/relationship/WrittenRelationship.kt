package kr.dove.neo4j.entity.relationship

import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.RelationshipId
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode
import java.util.Objects

@RelationshipProperties
data class WrittenRelationship(
    @TargetNode val keyword: KeywordEntity,     //  해당 relationship 이 OUTGOING 방향으로 사용되는 노드.
    @Property val uri: String,                  //  기사 uri
    @Property var editor: String? = null,       //  기자 (기사 작성자)
) {
    @RelationshipId var id: Long? = null

    override fun equals(other: Any?): Boolean {
        return other ?. let { o ->
            if (o is WrittenRelationship) {
                keyword.morph == o.keyword.morph
                        && editor == o.editor
                        && uri == o.uri
            } else false
        } ?: false
    }

    override fun hashCode(): Int {
        return Objects.hash(keyword.morph, editor, uri)
    }
}