package kr.dove.neo4j.service

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

//  singleton
object KomoranInstance {

    private val komoran = Komoran(DEFAULT_MODEL.FULL)

    fun getKomoran(): Komoran = komoran
}