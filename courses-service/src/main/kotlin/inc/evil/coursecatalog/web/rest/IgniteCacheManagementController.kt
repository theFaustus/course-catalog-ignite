package inc.evil.coursecatalog.web.rest

import inc.evil.coursecatalog.service.IgniteCacheManager
import inc.evil.coursecatalog.web.dto.OperationResponse
import inc.evil.coursecatalog.web.dto.ignite.CacheName
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.cache.Cache

@RestController
@RequestMapping("/api/v1/ignite-cache")
class IgniteCacheManagementController(val igniteCacheManager: IgniteCacheManager) {
    @DeleteMapping("/{cacheName}/{key}")
    fun clearDistributedMapForKey(@PathVariable cacheName: String, @PathVariable key: String): ResponseEntity<OperationResponse> {
        return ResponseEntity.ok(igniteCacheManager.clearDistributedCacheForKey(key, cacheName))
    }

    @DeleteMapping("/{cacheName}")
    fun clearDistributedMap(@PathVariable cacheName: String): ResponseEntity<OperationResponse> {
        return ResponseEntity.ok(igniteCacheManager.clearDistributedCache(cacheName))
    }

    @GetMapping("/{cacheName}")
    fun getDistributedMap(@PathVariable cacheName: String): ResponseEntity<List<Cache.Entry<Any, Any>>> {
        return ResponseEntity.ok(igniteCacheManager.getDistributedCache(cacheName))
    }

    @GetMapping
    fun getDistributedCacheNames(): ResponseEntity<CollectionModel<CacheName>> {
        val distributedCacheNames = igniteCacheManager.getDistributedCacheNames()
        distributedCacheNames.forEach {
            it.add(linkTo(WebMvcLinkBuilder.methodOn(IgniteCacheManagementController::class.java).getDistributedMap(it.name)).withSelfRel())
        }
        return ResponseEntity.ok(CollectionModel.of(distributedCacheNames))
    }

}
