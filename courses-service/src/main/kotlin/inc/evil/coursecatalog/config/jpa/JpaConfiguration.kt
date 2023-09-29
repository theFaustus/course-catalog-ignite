package inc.evil.coursecatalog.config.jpa

import org.apache.ignite.springdata.repository.config.RepositoryConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = ["inc.evil.coursecatalog.repo"],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [RepositoryConfig::class])]
)
class JpaConfiguration {
}
