package inc.evil.coursecatalog.common

import inc.evil.coursecatalog.ignite.repo.WikipediaSummaryRepository
import org.apache.ignite.Ignite
import org.apache.ignite.configuration.IgniteConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean

@TestConfiguration
class IgniteTestConfiguration {
    @MockBean
    private lateinit var igniteInstance: Ignite
    @MockBean
    private lateinit var wikipediaSummaryRepository: WikipediaSummaryRepository
    @MockBean
    private lateinit var igniteConfiguration: IgniteConfiguration
}
