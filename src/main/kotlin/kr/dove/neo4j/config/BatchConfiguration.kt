package kr.dove.neo4j.config

import com.google.gson.Gson
import kr.co.shineware.nlp.komoran.core.Komoran
import kr.dove.neo4j.api.Article
import kr.dove.neo4j.entity.node.keyword.KeywordEntity
import kr.dove.neo4j.entity.node.press.PressEntity
import kr.dove.neo4j.entity.relationship.WrittenRelationship
import kr.dove.neo4j.service.KomoranInstance
import kr.dove.neo4j.service.Spider
import org.openqa.selenium.WebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Configuration
@EnableBatchProcessing
class BatchConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val gson: Gson,

    private val neo4jTemplate: Neo4jTemplate,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    //  네이버 뉴스 정치, 경제, 사회 카테고리
    private val categories = arrayOf(
        "https://news.naver.com/main/main.naver?mode=LSD&mid=shm&sid1=100",
        "https://news.naver.com/main/main.naver?mode=LSD&mid=shm&sid1=101",
        "https://news.naver.com/main/main.naver?mode=LSD&mid=shm&sid1=102"
    )

    @Bean
    fun crawlAndProcess(driver: WebDriver, spider: Spider): Job {
        return jobBuilderFactory.get("crawlAndProcess")
            .start(crawl(driver, spider))
            .next(preprocess(driver, spider))
            .next(parse(KomoranInstance.getKomoran()))
            .next(updateDb())
            .next(cleanup(driver))
            .build()
    }

    fun crawl(driver: WebDriver, spider: Spider): Step {
        return stepBuilderFactory.get("crawl")
            .tasklet { _, chunkContext ->
                logger.info("Crawl articles...")
                val uris = spider.getUris(driver, categories)
                val executionContext = chunkContext.getExecutionContext()
                executionContext.put(
                    "uris",
                    gson.toJson(uris)
                )

                logger.info("Total crawled uris: {}", uris.size)
                RepeatStatus.FINISHED
            }
            .build()
    }

    fun preprocess(driver: WebDriver, spider: Spider): Step {
        return stepBuilderFactory.get("preprocess")
            .tasklet { _, chunkContext ->
                val executionContext = chunkContext.getExecutionContext()
                val uris = gson.fromJson(executionContext.get("uris").toString(), Array<String>::class.java)
                val articles = mutableListOf<Article>()
                for (uri: String in uris) {
                    logger.info("Preprocess article {}", uri)
                    spider.moveTo(driver, uri, 500L)
                    val headline = spider.getHeadline(driver)
                    val press = spider.getPress(driver)
                    val editor = spider.getEditor(driver)

                    if (!headline.isNullOrBlank() && !press.isNullOrBlank()) {
                        articles.add(Article(
                            uri, headline, press, editor
                        ))
                    }
                }

                executionContext.remove("uris")
                executionContext.put(
                    "articles",
                    gson.toJson(articles)
                )

                RepeatStatus.FINISHED
            }
            .build()
    }

    fun parse(komoran: Komoran): Step {
        return stepBuilderFactory.get("parse")
            .tasklet { _, chunkContext ->
                val executionContext = chunkContext.getExecutionContext()
                val articles = gson.fromJson(executionContext.get("articles").toString(), Array<Article>::class.java)
                val filtered = mutableListOf<Article>()
                for (article: Article in articles) {
                    logger.info("Parse headlines... {}", article.headline)
                    val tokens = komoran.analyze(article.headline).tokenList

                    article.apply {
                        this.morphs.addAll(
                            tokens
                                .filter { tkn -> "NNP" == tkn.pos }         //  명사 추출
                                .map { tkn -> tkn.morph }
                        )
                    }
                    if (article.morphs.size > 0) filtered.add(article)
                }

                executionContext.remove("articles")
                executionContext.put(
                    "parsed_articles",
                    gson.toJson(filtered)
                )

                RepeatStatus.FINISHED
            }
            .build()
    }

    fun updateDb(): Step {
        return stepBuilderFactory.get("updateDb")
            .tasklet { _, chunkContext ->
                logger.info("Update db...")
                val executionContext = chunkContext.getExecutionContext()
                val parsed = gson.fromJson(executionContext.get("parsed_articles").toString(), Array<Article>::class.java)
                parsed.forEach { doSql(it) }
                executionContext.remove("parsed_articles")
                RepeatStatus.FINISHED
            }
            .build()
    }

    fun cleanup(driver: WebDriver): Step {
        return stepBuilderFactory.get("cleanup")
            .tasklet { _, _ ->
                logger.info("Cleanup driver...")
                SeleniumConfiguration.closeDriver()
                RepeatStatus.FINISHED
            }
            .build()
    }

    //  데이터베이스 업데이트 함수
    //  새로운 transaction 생성 후 진행 (propagation x)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun doSql(article: Article) {
        //  get press node
        //  create if not exists using switchIfEmpty()
        val press: PressEntity = neo4jTemplate.findById(article.press, PressEntity::class.java)
            .orElseGet {
                neo4jTemplate.save(
                    PressEntity(
                        article.press
                    )
                )
            }

        //  create keyword node if not exists
        val keywords = article.morphs
            .map { morph ->
                neo4jTemplate.findById(morph, KeywordEntity::class.java)
                    .orElseGet {
                        neo4jTemplate.save(
                            KeywordEntity(
                                morph
                            )
                        )
                    }
            }

        //  set relationships
        /*
        neo4jClient.query(
            """
                match (:Keyword {morph:'$morph'})-[rel: WRITTEN]->(:Press {name:'$press'})
                set rel.uri = '${article.uri}'
                set rel.editor = '${article.editor ?: ""}'
                return rel;
            """.trimIndent()
        )
         */
        keywords
            .forEach { keyword ->
            press.articles.add(
                WrittenRelationship(
                    keyword,
                    article.uri,
                    article.editor,
                )
            )
        }

        //  commit
        neo4jTemplate.save(press)
        logger.info("Completes update an article {}", article.headline)
    }

    private fun ChunkContext.getExecutionContext() =
        this.stepContext.stepExecution.jobExecution.executionContext
}