package inc.evil.coursecatalog.service.impl

import inc.evil.coursecatalog.service.IgniteCacheManager
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class IgniteCacheManagerImplTest {

    private val CACHE_NAME: String = "cacheName"
    private val igniteInstance: Ignite = Mockito.mock(Ignite::class.java)
    private val igniteCacheManager: IgniteCacheManager = IgniteCacheManagerImpl(igniteInstance)
    private val distributedMap = Mockito.mock(IgniteCache::class.java) as IgniteCache<Any, Any>

    @BeforeEach
    fun setUp() {
        `when`(igniteInstance.cache<Any, Any>(CACHE_NAME)).thenReturn(distributedMap)
    }

    @Test
    fun getDistributedMap_callsIgniteInstance() {
        igniteCacheManager.getDistributedCache(CACHE_NAME)

        verify(igniteInstance).cache<Any, Any>(CACHE_NAME)
    }

    @Test
    fun getDistributedMap_whenIgniteInstanceException_returnsEmptyMap() {
        `when`(igniteInstance.cache<Any, Any>(CACHE_NAME)).thenThrow(RuntimeException())

        Assertions.assertThat(igniteCacheManager.getDistributedCache(CACHE_NAME)).isEmpty()

        verify(igniteInstance).cache<Any, Any>(CACHE_NAME)
    }

    @Test
    fun clearDistributedMap_callsIgniteInstance() {
        Assertions.assertThat(igniteCacheManager.clearDistributedCache(CACHE_NAME).message).isEqualTo("IgniteCache[cacheName] cleared successfully")
        verify(igniteInstance).cache<Any, Any>(CACHE_NAME)
        verify(distributedMap).clear()
    }

    @Test
    fun clearDistributedMap_whenIgniteException_returnsErrorMessage() {
        `when`(igniteInstance.cache<Any, Any>(CACHE_NAME)).thenThrow(RuntimeException("Oops"))

        Assertions.assertThat(igniteCacheManager.clearDistributedCache(CACHE_NAME).message)
            .isEqualTo("Oops! Something went wrong during clearing IgniteCache[cacheName]... Oops")
        verify(igniteInstance).cache<Any, Any>(CACHE_NAME)
    }

    @Test
    fun clearDistributedMapForKey_clearsCacheForKey() {
        Assertions.assertThat(igniteCacheManager.clearDistributedCacheForKey("key", CACHE_NAME).message).isEqualTo("IgniteCache[cacheName] for key=key cleared successfully")
        verify(igniteInstance).cache<Any, Any>(CACHE_NAME)
        verify(distributedMap).remove("key")
    }

    @Test
    fun clearDistributedMapForKey_whenIgniteException_returnsErrorMessage() {
        `when`(igniteInstance.cache<Any, Any>(CACHE_NAME)).thenThrow(RuntimeException("Oops"))

        Assertions.assertThat(igniteCacheManager.clearDistributedCacheForKey("key", CACHE_NAME).message).isEqualTo("Oops! Something went wrong during clearing IgniteCache[cacheName]... Oops")
        verify(igniteInstance).cache<Any, Any>(CACHE_NAME)
    }
}
