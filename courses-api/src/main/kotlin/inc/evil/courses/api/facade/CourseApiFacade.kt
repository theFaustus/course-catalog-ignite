package inc.evil.courses.api.facade

import inc.evil.courses.api.web.dto.CourseApiResponse
import org.apache.ignite.services.Service

interface CourseApiFacade: Service {
    companion object {
        const val COURSE_API_FACADE_SERVICE_NAME = "CourseApiFacade"
    }
    fun findAll(): List<CourseApiResponse>
    fun findById(id: Int): CourseApiResponse
}
