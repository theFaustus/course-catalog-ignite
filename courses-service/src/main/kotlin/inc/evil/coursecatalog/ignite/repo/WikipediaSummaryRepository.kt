package inc.evil.coursecatalog.ignite.repo

import inc.evil.coursecatalog.config.ignite.IgniteConfig.Companion.WIKIPEDIA_SUMMARIES
import inc.evil.coursecatalog.service.impl.WikipediaApiClientImpl
import org.apache.ignite.springdata.repository.IgniteRepository
import org.apache.ignite.springdata.repository.config.RepositoryConfig
import org.springframework.stereotype.Repository

@Repository
@RepositoryConfig(cacheName = WIKIPEDIA_SUMMARIES)
interface WikipediaSummaryRepository : IgniteRepository<WikipediaApiClientImpl.WikipediaSummary, String>
