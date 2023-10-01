package inc.evil.coursecatalog.service.impl

import inc.evil.coursecatalog.common.exceptions.LockAcquisitionException
import inc.evil.coursecatalog.common.exceptions.NotFoundException
import inc.evil.coursecatalog.config.ignite.IgniteConfig.Companion.WIKIPEDIA_SUMMARIES
import inc.evil.coursecatalog.ignite.repo.WikipediaSummaryRepository
import inc.evil.coursecatalog.model.Course
import inc.evil.coursecatalog.repo.CourseRepository
import inc.evil.coursecatalog.service.CourseService
import inc.evil.coursecatalog.service.WikipediaApiClient
import org.apache.ignite.Ignite
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val SUMMARIES_LOCK = "summaries-lock"

@Service("courseService")
class CourseServiceImpl(
    val courseRepository: CourseRepository,
    val wikipediaApiClient: WikipediaApiClient,
    val wikipediaSummaryRepository: WikipediaSummaryRepository,
    val igniteInstance: Ignite
) : CourseService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Course> = courseRepository.findAll()

    @Transactional(readOnly = true)
    override fun findById(id: Int): Course =
        courseRepository.findById(id).orElseThrow { NotFoundException(Course::class, "id", id.toString()) }

    @Transactional
    override fun save(course: Course): Course {
        enhanceWithProgrammingLanguageDescription(course)
        return courseRepository.save(course)
    }

    private fun enhanceWithProgrammingLanguageDescription(course: Course) {
        val lock = igniteInstance.reentrantLock(SUMMARIES_LOCK, true, true, true)
        if (!lock.tryLock()) throw LockAcquisitionException(SUMMARIES_LOCK, "enhanceWithProgrammingLanguageDescription")
        log.debug("Acquired lock {}", lock)
        Thread.sleep(2000)
        val summaries = wikipediaSummaryRepository.cache()
        log.debug("Fetched ignite cache [$WIKIPEDIA_SUMMARIES] = size(${summaries.size()})]")
        wikipediaSummaryRepository.findById(course.programmingLanguage).orElseGet {
            wikipediaApiClient.fetchSummaryFor("${course.programmingLanguage}_(programming_language)")?.let {
                log.debug("No cache value found, using wikipedia's response $it to update $course programming language description")
                wikipediaSummaryRepository.save(course.programmingLanguage, it)
                it
            }
        }?.let { course.programmingLanguageDescription = it.summary }
        lock.unlock()
    }

    @Transactional
    override fun deleteById(id: Int) = courseRepository.delete(findById(id))
}
