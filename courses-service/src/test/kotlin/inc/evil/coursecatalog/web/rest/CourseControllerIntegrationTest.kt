package inc.evil.coursecatalog.web.rest

import com.github.tomakehurst.wiremock.WireMockServer
import inc.evil.coursecatalog.common.IO
import inc.evil.coursecatalog.common.IntegrationTest
import inc.evil.coursecatalog.common.WireMockContextInitializer
import inc.evil.coursecatalog.common.dto.ErrorResponse
import inc.evil.coursecatalog.common.fixtures.CourseResponseFixture
import inc.evil.coursecatalog.common.stubResponse
import inc.evil.coursecatalog.ignite.repo.WikipediaSummaryRepository
import inc.evil.coursecatalog.service.impl.WikipediaApiClientImpl
import inc.evil.coursecatalog.web.dto.CourseRequest
import inc.evil.coursecatalog.web.dto.CourseResponse
import inc.evil.coursecatalog.web.dto.InstructorRequest
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.IgniteLock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*


@IntegrationTest
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
internal class CourseControllerIntegrationTest {

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var igniteInstance: Ignite

    @Autowired
    lateinit var wikipediaSummaryRepository: WikipediaSummaryRepository

    @Test
    @Sql(scripts = ["/h2/courses.sql"])
    fun getCourseById() {
        val expectedCourse = CourseResponseFixture.of()

        val courseResponse = webTestClient.get()
            .uri("/api/v1/courses/{id}", expectedCourse.id)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(CourseResponse::class.java)
            .returnResult()
            .responseBody


        courseResponse?.let {
            assertThat(it.id).isEqualTo(expectedCourse.id)
            assertThat(it.name).isEqualTo(expectedCourse.name)
            assertThat(it.category).isEqualTo(expectedCourse.category)
        }
    }

    @Test
    @Sql(scripts = ["/h2/courses.sql"])
    fun getAllCourses() {
        val coursesResponse = webTestClient.get()
            .uri("/api/v1/courses")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBodyList(CourseResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(coursesResponse).hasSize(3)
    }

    @Test
    @Sql(scripts = ["/h2/courses.sql"])
    fun deleteCourseById() {
        val id = -1

        webTestClient.delete()
            .uri("/api/v1/courses/{id}", id)
            .exchange()
            .expectStatus().is2xxSuccessful

        val courseResponse = webTestClient.get()
            .uri("/api/v1/courses/{id}", id)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(courseResponse?.messages).isEqualTo(setOf("Course with id equal to [-1] could not be found!"))    }

    @Test
    fun createCourse() {
        val responseBody = IO.read("/json/wikipediea-instructor-response.json")
        wireMockServer.stubResponse("/page/summary/Bruce%20Eckel", responseBody)
        wireMockServer.stubResponse("/page/summary/java_(programming_language)", responseBody)
        val cache = mock(IgniteCache::class.java) as IgniteCache<String, WikipediaApiClientImpl.WikipediaSummary>
        val igniteLock = mock(IgniteLock::class.java)
        `when`(igniteInstance.reentrantLock("summaries-lock", true, true, true)).thenReturn(igniteLock)
        `when`(igniteLock.tryLock()).thenReturn(true)
        `when`(wikipediaSummaryRepository.findById(any())).thenReturn(Optional.empty())
        `when`(wikipediaSummaryRepository.cache()).thenReturn(cache)
        `when`(cache.size()).thenReturn(0)

        val courseResponse = webTestClient.post()
            .uri("/api/v1/courses")
            .bodyValue(CourseRequest("Kotlin Development", "java","TUTORIAL", InstructorRequest("Bruce Eckel")))
            .exchange()
            .expectStatus().isCreated
            .expectBody(CourseResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(courseResponse?.id).isNotNull
    }

}
