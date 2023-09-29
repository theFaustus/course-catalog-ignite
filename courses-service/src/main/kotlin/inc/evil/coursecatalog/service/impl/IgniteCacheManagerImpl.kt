package inc.evil.coursecatalog.service.impl

import inc.evil.coursecatalog.service.IgniteCacheManager
import inc.evil.coursecatalog.web.dto.OperationResponse
import inc.evil.coursecatalog.web.dto.ignite.CacheName
import org.apache.ignite.Ignite
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.cache.Cache


@Component
class IgniteCacheManagerImpl(val igniteInstance: Ignite) : IgniteCacheManager {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun getDistributedCacheNames(): List<CacheName> {
        return try {
            log.info("Retrieving IgniteCache names")
            igniteInstance.cacheNames().map { CacheName(it) }.toList()
        } catch (e: Exception) {
            log.warn("Oops! Something went wrong during IgniteCache names retrieval... ${e.message}")
            emptyList()
        }
    }

    override fun getDistributedCache(cacheName: String): List<Cache.Entry<Any, Any>> {
        return try {
            log.info("Retrieving IgniteCache[$cacheName]")
            igniteInstance.cache<Any?, Any?>(cacheName).toList()
        } catch (e: Exception) {
            log.warn("Oops! Something went wrong during IgniteCache[$cacheName] retrieval... ${e.message}")
            emptyList()
        }
    }

    override fun clearDistributedCacheForKey(key: String, cacheName: String): OperationResponse {
        return try {
            igniteInstance.cache<Any, Any>(cacheName).remove(key)
            OperationResponse("IgniteCache[$cacheName] for key=$key cleared successfully")
        } catch (e: Exception) {
            OperationResponse("Oops! Something went wrong during clearing IgniteCache[$cacheName]... ${e.message}")
        }
    }

    override fun clearDistributedCache(cacheName: String): OperationResponse {
        return try {
            igniteInstance.cache<Any, Any>(cacheName).clear()
            OperationResponse("IgniteCache[$cacheName] cleared successfully")
        } catch (e: Exception) {
            OperationResponse("Oops! Something went wrong during clearing IgniteCache[$cacheName]... ${e.message}")
        }
    }

}
