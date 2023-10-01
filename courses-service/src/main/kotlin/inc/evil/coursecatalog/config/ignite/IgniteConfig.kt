package inc.evil.coursecatalog.config.ignite

import inc.evil.coursecatalog.service.impl.WikipediaApiClientImpl
import inc.evil.courses.api.facade.CourseApiFacade
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteSpring
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheWriteSynchronizationMode
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.services.ServiceConfiguration
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder.DFLT_MCAST_GROUP
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration


@Configuration
@Profile("!test")
@EnableConfigurationProperties(value = [IgniteProperties::class])
@EnableIgniteRepositories(basePackages = ["inc.evil.coursecatalog.ignite"])
class IgniteConfig(
    val igniteProperties: IgniteProperties,
    val courseApiFacade: CourseApiFacade,
    val applicationContext: ApplicationContext
) {
    companion object {
        const val WIKIPEDIA_SUMMARIES = "WIKIPEDIA_SUMMARIES"
    }

    @Bean(name = ["igniteInstance"])
    fun igniteInstance(igniteConfiguration: IgniteConfiguration): Ignite {
        return IgniteSpring.start(igniteConfiguration, applicationContext)
    }

    @Bean
    fun igniteConfiguration(): IgniteConfiguration {
        val igniteConfiguration = IgniteConfiguration()
        igniteConfiguration.setIgniteInstanceName(igniteProperties.instanceName)
        igniteConfiguration.setPeerClassLoadingEnabled(true)
        igniteConfiguration.setMetricsLogFrequency(0) // no spam
        igniteConfiguration.setCommunicationSpi(configureTcpCommunicationSpi()) // avoid OOM due to message limit
        igniteConfiguration.setDiscoverySpi(configureDiscovery()) // allow possibility to switch to Kubernetes
        igniteConfiguration.setCacheConfiguration(wikipediaSummaryCacheConfiguration()) //vararg
        igniteConfiguration.setServiceConfiguration(courseApiFacadeConfiguration()) //vararg
        return igniteConfiguration
    }

    private fun courseApiFacadeConfiguration(): ServiceConfiguration {
        val serviceConfiguration = ServiceConfiguration()
        serviceConfiguration.service = courseApiFacade
        serviceConfiguration.name = CourseApiFacade.COURSE_API_FACADE_SERVICE_NAME
        serviceConfiguration.maxPerNodeCount = 1
        return serviceConfiguration
    }

    private fun configureTcpCommunicationSpi(): TcpCommunicationSpi {
        val tcpCommunicationSpi = TcpCommunicationSpi()
        tcpCommunicationSpi.setMessageQueueLimit(1024)
        return tcpCommunicationSpi
    }

    private fun configureDiscovery(): TcpDiscoverySpi {
        val spi = TcpDiscoverySpi()
        var ipFinder: TcpDiscoveryIpFinder? = null;
        if (igniteProperties.discovery.tcp.enabled) {
            ipFinder = TcpDiscoveryMulticastIpFinder()
            ipFinder.setMulticastGroup(DFLT_MCAST_GROUP)
        } else if (igniteProperties.discovery.kubernetes.enabled) {
            ipFinder = TcpDiscoveryKubernetesIpFinder()
            ipFinder.setNamespace(igniteProperties.discovery.kubernetes.namespace)
            ipFinder.setServiceName(igniteProperties.discovery.kubernetes.serviceName)
        }
        spi.setIpFinder(ipFinder)
        return spi
    }

    private fun wikipediaSummaryCacheConfiguration(): CacheConfiguration<String, WikipediaApiClientImpl.WikipediaSummary> {
        val wikipediaCache = CacheConfiguration<String, WikipediaApiClientImpl.WikipediaSummary>(WIKIPEDIA_SUMMARIES)
        wikipediaCache.setIndexedTypes(String::class.java, WikipediaApiClientImpl.WikipediaSummary::class.java)
        wikipediaCache.setEagerTtl(true)
        wikipediaCache.setCacheMode(CacheMode.REPLICATED)
        wikipediaCache.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_ASYNC)
        wikipediaCache.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
        wikipediaCache.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration(TimeUnit.MINUTES, 60)))
        return wikipediaCache
    }
}
