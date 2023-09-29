package inc.evil.coursecatalog.config.ignite

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "ignite")
data class IgniteProperties(
    val instanceName: String,
    val discovery: DiscoveryProperties = DiscoveryProperties()
)

@ConstructorBinding
data class DiscoveryProperties(
    val tcp: TcpProperties = TcpProperties(),
    val kubernetes: KubernetesProperties = KubernetesProperties()
)

@ConstructorBinding
data class TcpProperties(
    val enabled: Boolean = false,
    val host: String = "localhost"
)

data class KubernetesProperties(
    val enabled: Boolean = false,
    val namespace: String = "evil-inc",
    val serviceName: String = "course-service"
)
