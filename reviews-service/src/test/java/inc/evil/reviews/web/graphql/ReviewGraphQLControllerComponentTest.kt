package inc.evil.reviews.web.graphql

import inc.evil.courses.api.facade.CourseApiFacade
import inc.evil.courses.api.web.dto.CourseApiResponse
import inc.evil.courses.api.web.dto.InstructorApiResponse
import inc.evil.reviews.common.AbstractTestcontainersIntegrationTest
import inc.evil.reviews.common.ComponentTest
import inc.evil.reviews.common.fixtures.ReviewResponseFixture
import inc.evil.reviews.web.dto.ReviewResponse
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCluster
import org.apache.ignite.IgniteServices
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.graphql.test.tester.GraphQlTester
import ro.orange.eshop.userordermanagement.common.RunSql

@ComponentTest
internal class ReviewGraphQLControllerComponentTest : AbstractTestcontainersIntegrationTest() {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockBean
    private lateinit var igniteInstance: Ignite

    @Test
    @RunSql(["schema.sql", "/data/reviews.sql"])
    fun getReviewById() {
        graphQlTester.documentName("getReviewById")
            .variable("id", -1)
            .execute()
            .path("getReviewById")
            .entity(ReviewResponse::class.java)
            .isEqualTo(ReviewResponseFixture.of())
    }

    @Test
    @RunSql(["schema.sql", "/data/reviews.sql"])
    fun getAllReviews() {
        graphQlTester.documentName("getAllReviews")
            .execute()
            .path("getAllReviews")
            .entityList(ReviewResponse::class.java)
            .hasSize(4)
            .contains(ReviewResponseFixture.of())
    }

    @Test
    @RunSql(["schema.sql", "/data/reviews.sql"])
    fun deleteReviewById() {
        graphQlTester.documentName("deleteReviewById")
            .variable("id", -1)
            .executeAndVerify()
        graphQlTester.documentName("getReviewById")
            .variable("id", -1)
            .execute()
            .errors()
            .expect { it.message == "Review with id equal to [-1] could not be found!" }
    }

    @Test
    @RunSql(["schema.sql", "/data/reviews.sql"])
    fun createReview() {
        val instructor = InstructorApiResponse(1, "name", "summary", "description")
        val courseApiResponse = CourseApiResponse(1, "name", "category", "java", "desc", "date", "date", instructor)
        val igniteServices = mock(IgniteServices::class.java)
        val courseApiFacade = mock(CourseApiFacade::class.java)
        val cluster = mock(IgniteCluster::class.java)
        whenever(igniteInstance.cluster()).thenReturn(cluster)
        whenever(igniteInstance.services(anyOrNull())).thenReturn(igniteServices)
        whenever(igniteServices.serviceProxy(CourseApiFacade.COURSE_API_FACADE_SERVICE_NAME, CourseApiFacade::class.java, false)).thenReturn(courseApiFacade)
        whenever(courseApiFacade.findById(4)).thenReturn(courseApiResponse)


        graphQlTester.documentName("createReview")
            .variable("request", mapOf("author" to "Squidward", "text" to "What a nice course", "courseId" to 4))
            .execute()
            .path("createReview")
            .entity(ReviewResponse::class.java)
            .matches { it.text == "What a nice course" && it.author == "Squidward" && it.courseId == 4 }

    }

}
