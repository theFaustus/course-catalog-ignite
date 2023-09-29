package inc.evil.coursecatalog.service

import inc.evil.coursecatalog.web.dto.OperationResponse
import inc.evil.coursecatalog.web.dto.ignite.CacheName
import javax.cache.Cache

interface IgniteCacheManager {
    fun clearDistributedCacheForKey(key: String, cacheName: String): OperationResponse
    fun clearDistributedCache(cacheName: String): OperationResponse
    fun getDistributedCache(cacheName: String): List<Cache.Entry<Any, Any>>
    fun getDistributedCacheNames(): List<CacheName>
}
