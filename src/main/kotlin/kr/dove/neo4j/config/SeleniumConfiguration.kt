package kr.dove.neo4j.config

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class SeleniumConfiguration {
    companion object {
        private var webDriver: WebDriver? = null

        fun closeDriver() {
            webDriver?.close()
            webDriver = null
        }
    }

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("selenium/chromedriver")
        System.setProperty("webdriver.chrome.driver", resource.url.path)
    }

    @Bean
    fun getDriver(): WebDriver = webDriver ?: run {
        webDriver = ChromeDriver(ChromeOptions().setHeadless(true)) //  run in background
        webDriver!!
    }

    @PreDestroy
    fun destroy() {
        webDriver?.close()
    }
}