package kr.dove.neo4j.service

import kr.dove.neo4j.api.JobLauncherService
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class JobLauncherServiceImpl(
    private val jobLauncher: JobLauncher,
    private val crawlAndProcess: Job,
) : JobLauncherService {

    override fun launch(): ResponseEntity<String> {
        jobLauncher
            .run(
                crawlAndProcess,
                JobParametersBuilder()
                    .addDate("start", java.util.Date(), true)   //  job identifier
                    .toJobParameters()
            )

        return ResponseEntity("Launched job successfully!", HttpStatus.OK)
    }
}