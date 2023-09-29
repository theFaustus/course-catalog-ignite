package inc.evil.coursecatalog.facade.impl

import inc.evil.coursecatalog.service.impl.CourseServiceImpl
import inc.evil.courses.api.facade.CourseApiFacade
import inc.evil.courses.api.web.dto.CourseApiResponse
import inc.evil.courses.api.web.dto.InstructorApiResponse
import org.apache.ignite.Ignite
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.resources.SpringResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CourseApiFacadeImpl : CourseApiFacade {

    @Transient
    @SpringResource(resourceName = "courseService")
    lateinit var courseService: CourseServiceImpl

    @Transient
    @IgniteInstanceResource //spring constructor injection won't work since ignite is not ready
    lateinit var igniteInstance: Ignite

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun findAll(): List<CourseApiResponse> = courseService.findAll().map {
        CourseApiResponse(
            id = it.id,
            name = it.name,
            category = it.category.toString(),
            programmingLanguage = it.programmingLanguage,
            programmingLanguageDescription = it.programmingLanguageDescription,
            createdAt = it.createdAt.toString(),
            updatedAt = it.updatedAt.toString(),
            instructor = InstructorApiResponse(it.instructor?.id, it.instructor?.name, it.instructor?.summary, it.instructor?.description)
        )
    }

    override fun findById(id: Int): CourseApiResponse = courseService.findById(id).let {
        CourseApiResponse(
            id = it.id,
            name = it.name,
            category = it.category.toString(),
            programmingLanguage = it.programmingLanguage,
            programmingLanguageDescription = it.programmingLanguageDescription,
            createdAt = it.createdAt.toString(),
            updatedAt = it.updatedAt.toString(),
            instructor = InstructorApiResponse(it.instructor?.id, it.instructor?.name, it.instructor?.summary, it.instructor?.description)
        )
    }

    override fun cancel() {
        log.info("Canceling service")
    }

    override fun init() {
        log.info("Before deployment :: Pre-initializing service before execution on node {}", igniteInstance.cluster().forLocal().node())
    }

    override fun execute() {
        log.info("Deployment :: The service is deployed on grid node {}", igniteInstance.cluster().forLocal().node())
    }
}
