package kr.dove.neo4j

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import kr.dove.neo4j.service.KomoranInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KomoranTests {

    @Test
    fun works_well() {
        val komoran = Komoran(DEFAULT_MODEL.FULL)
        val s = "안녕하세요. 반갑습니다\n저는 바보입니다."
        val result = komoran.analyze(s)

        result.tokenList
            .forEach { tkn ->
                println("${tkn.morph}, ${tkn.pos}")
                Assertions.assertNotNull(tkn.morph)
                Assertions.assertNotNull(tkn.pos)
            }
    }

    @Test
    fun is_singleton() {
        val komoran = KomoranInstance.getKomoran()
        Assertions.assertEquals(komoran, KomoranInstance.getKomoran())
    }

}