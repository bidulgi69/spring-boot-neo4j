package kr.dove.neo4j

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.dove.neo4j.config.SeleniumConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.annotation.PreDestroy

@SpringBootApplication
class SpringNeo4jApplication {

	@Bean
	fun getGson(): Gson {
		return GsonBuilder()
			.setLenient()
			.create()
	}

	@PreDestroy
	fun destroy() {
		SeleniumConfiguration.closeDriver()
	}
}

fun main(args: Array<String>) {
	runApplication<SpringNeo4jApplication>(*args)
}
