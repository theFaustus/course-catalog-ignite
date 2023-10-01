package inc.evil.coursecatalog

import inc.evil.coursecatalog.ignite.repo.WikipediaSummaryRepository
import inc.evil.coursecatalog.service.WikipediaApiClient
import inc.evil.coursecatalog.service.impl.WikipediaApiClientImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(value = [WikipediaApiClientImpl.WikipediaApiConfig::class])
class CourseCatalogApplication {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Bean
    fun init(client: WikipediaApiClient, repo: WikipediaSummaryRepository): CommandLineRunner = CommandLineRunner {
        run {
            client.fetchSummaryFor("Java programming language")?.let { repo.save("Java", it) }
            client.fetchSummaryFor("Kotlin programming language")?.let { repo.save("Kotlin", it) }
            client.fetchSummaryFor("C++")?.let { repo.save("C++", it) }
            client.fetchSummaryFor("Python programming language")?.let { repo.save("C#", it) }
            client.fetchSummaryFor("Javascript")?.let { repo.save("Javascript", it) }

//            repo.findAll().forEach { log.info("Fetched {}", it) }
//            repo.findByTitle("Kotlin").forEach { log.info("Fetched by title {}", it) }
//            repo.findByDescriptionContains("programming language").forEach { log.info(" Fetched by description {}", it) }
//            repo.countPerDescription().forEach { log.info("Count per description {}", it) }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CourseCatalogApplication>(*args)
}
