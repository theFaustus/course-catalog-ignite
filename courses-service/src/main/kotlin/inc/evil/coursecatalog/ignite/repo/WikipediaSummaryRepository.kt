package inc.evil.coursecatalog.ignite.repo

import inc.evil.coursecatalog.config.ignite.IgniteConfig.Companion.WIKIPEDIA_SUMMARIES
import inc.evil.coursecatalog.service.impl.WikipediaApiClientImpl.WikipediaSummary
import org.apache.ignite.springdata.repository.IgniteRepository
import org.apache.ignite.springdata.repository.config.Query
import org.apache.ignite.springdata.repository.config.RepositoryConfig
import org.springframework.stereotype.Repository
import javax.cache.Cache

@Repository
@RepositoryConfig(cacheName = WIKIPEDIA_SUMMARIES)
interface WikipediaSummaryRepository : IgniteRepository<WikipediaSummary, String> {

    fun findByTitle(title: String): List<WikipediaSummary>

    fun findByDescriptionContains(keyword: String): List<Cache.Entry<String, WikipediaSummary>>

    @Query(value = "select description, count(description) as \"count\" from WIKIPEDIA_SUMMARIES.WIKIPEDIASUMMARY group by description")
    fun countPerDescription(): List<CountPerProgrammingLanguageType>

    interface CountPerProgrammingLanguageType {
        fun getDescription(): String
        fun getCount(): Int
    }
}
