package inc.evil.reviews.config.ignite

import org.apache.ignite.Ignite
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder.DFLT_MCAST_GROUP
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories
import org.apache.ignite.springframework.boot.autoconfigure.IgniteConfigurer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(value = [IgniteProperties::class])
@EnableIgniteRepositories(basePackages = ["inc.evil.reviews.ignite"])
class IgniteConfig(val igniteProperties: IgniteProperties) {

    @Bean(name = ["igniteInstance"])
    fun igniteInstance(ignite: Ignite): Ignite {
        return ignite
    }

    @Bean
    fun configurer(): IgniteConfigurer {
        return IgniteConfigurer { igniteConfiguration: IgniteConfiguration ->
            igniteConfiguration.setIgniteInstanceName(igniteProperties.instanceName)
            igniteConfiguration.setClientMode(true)
            igniteConfiguration.setPeerClassLoadingEnabled(true)
            igniteConfiguration.setMetricsLogFrequency(0) // no spam
            igniteConfiguration.setCommunicationSpi(configureTcpCommunicationSpi()) // avoid OOM due to message limit
            igniteConfiguration.setDiscoverySpi(configureDiscovery()) // allow possibility to switch to Kubernetes
        }
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
}
