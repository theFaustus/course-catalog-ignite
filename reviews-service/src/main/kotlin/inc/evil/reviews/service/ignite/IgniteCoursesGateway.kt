package inc.evil.reviews.service.ignite

import inc.evil.courses.api.facade.CourseApiFacade
import org.apache.ignite.Ignite
import org.springframework.stereotype.Component

@Component
class IgniteCoursesGateway(private val igniteInstance: Ignite) {

    fun findCourseById(id: Int) = courseApiFacade().findById(id)

    fun findAllCourses() = courseApiFacade().findAll()

    private fun courseApiFacade(): CourseApiFacade {
        return igniteInstance.services(igniteInstance.cluster().forServers())
            .serviceProxy(CourseApiFacade.COURSE_API_FACADE_SERVICE_NAME, CourseApiFacade::class.java, false)
    }
}
