package kr.dove.neo4j.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping

interface JobLauncherService {

    //  batch job 실행 (trigger)
    @GetMapping(
        value = ["/job/launch"]
    )
    fun launch(): ResponseEntity<String>
}