package inc.evil.coursecatalog.web.rest

import inc.evil.coursecatalog.service.impl.IgniteCacheManagerImpl
import inc.evil.coursecatalog.web.dto.OperationResponse
import org.apache.ignite.internal.processors.cache.CacheEntryImpl
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(IgniteCacheManagementController::class)
class IgniteCacheManagementControllerTest {

    private val KEY: String = "key"
    private val CACHE_NAME: String = "someCache"

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var igniteCacheManager: IgniteCacheManagerImpl

    @Test
    fun getDistributedMap_returnsMap() {
        var cacheEntry = CacheEntryImpl<Any, Any>(KEY, "value")
        `when`(igniteCacheManager.getDistributedCache(CACHE_NAME)).thenReturn(listOf(cacheEntry))

        val responseBody = webTestClient.get()
            .uri("/api/v1/ignite-cache/{cacheName}", CACHE_NAME)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(object : ParameterizedTypeReference<List<CacheEntryImpl<Any, Any>>>() {})
            .returnResult()
            .responseBody

        Assertions.assertThat(responseBody).anyMatch { it.key.equals(cacheEntry.key) }
    }

    @Test
    fun clearDistributedMap_returnsOperationResponse() {
        `when`(igniteCacheManager.clearDistributedCache(CACHE_NAME)).thenReturn(OperationResponse("Everything is nice"))

        val responseBody = webTestClient.delete()
            .uri("/api/v1/ignite-cache/{cacheName}", CACHE_NAME)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(OperationResponse::class.java)
            .returnResult()
            .responseBody

        Assertions.assertThat(responseBody?.message).isEqualTo("Everything is nice")
    }

    @Test
    fun clearDistributedMapForKey_returnsOperationResponse() {
        `when`(igniteCacheManager.clearDistributedCacheForKey(KEY, CACHE_NAME)).thenReturn(OperationResponse("Everything is nice"))

        val responseBody = webTestClient.delete()
            .uri("/api/v1/ignite-cache/{cacheName}/{key}", CACHE_NAME, KEY)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(OperationResponse::class.java)
            .returnResult()
            .responseBody

        Assertions.assertThat(responseBody?.message).isEqualTo("Everything is nice")

    }

}
