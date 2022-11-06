package kr.dove.neo4j.service

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.springframework.stereotype.Component

@Component
class Spider {

    @kotlin.jvm.Throws(InterruptedException::class)
    fun getUris(driver: WebDriver, categories: Array<String>): Set<String> {
        val pagePrefix = "#&date= 00:00:00&page"
        val uris = HashSet<String>()
        for (i: Int in categories.indices) {
            moveTo(driver, categories[i])
            //  헤드라인 뉴스 조회
            val headlines: List<WebElement> = driver.findElements(
                By.xpath("//a[contains(@class, \"cluster_text_headline\")]")
            )
            headlines
                .stream()
                .filter { elem -> elem.getAttribute("href") != null && elem.getAttribute("href").isNotBlank() }
                .forEach { elem -> uris.add(elem.getAttribute("href")) }

            //  섹션 1~10 페이지 조회
            for (j: Int in 1..10) {
                if (j != 1) {
                    //  1 페이지의 경우 categories[i] uri 로 접속해도 섹션이 보여진다.
                    moveTo(driver, "${categories[i]}$pagePrefix=$j", 2000L)
                }
                //  to avoid stale element exception
                //  html 이 로딩되기를 기다려준다.
                //  아래 id=section_body 를 갖는 태그가 페이지 이동 후 로딩될 때 까지 기다려줘야 한다.
                //  driver.manage().timeouts().implicitlyWait() 을 대안으로 사용할 수 있다.
                Thread.sleep(1000L)

                val sectionBody: WebElement = driver.findElement(
                    By.xpath("//div[@id=\"section_body\"]")
                )
                val sections: List<WebElement> = sectionBody.findElements(
                    By.xpath("//a[contains(@href, \"sid=\") and contains(@href, \"mnews/article\")]")
                )
                sections
                    .stream()
                    .filter { elem ->
                        elem.getAttribute("href") != null && elem.getAttribute("href").isNotBlank()
                    }
                    .forEach { elem -> uris.add(elem.getAttribute("href")) }
            }
        }
        return uris
    }

    fun getHeadline(driver: WebDriver): String? {
        val element: WebElement = driver.findElement(By.xpath("//h2[@class='media_end_head_headline']"))
        return element.text
    }

    fun getPress(driver: WebDriver): String? {
        val element: WebElement = driver.findElement(By.xpath("//img[contains(@class,'media_end_head_top_logo_img')]"))
        return element.getAttribute("alt")
    }

    fun getEditor(driver: WebDriver): String? {
        return try {
            val element: WebElement = driver.findElement(By.xpath("//em[@class='media_end_head_journalist_name']"))
            element.text
        } catch (e: org.openqa.selenium.NoSuchElementException) { null }    //  기사 작성자가 존재하지 않는 경우가 있기 때문에, 처리해줘야 한다.
    }

    @kotlin.jvm.Throws(InterruptedException::class)
    fun moveTo(driver: WebDriver, uri: String, timeouts: Long = 0L) {
        driver.get(uri)
        if (timeouts > 0) Thread.sleep(timeouts)
    }
}