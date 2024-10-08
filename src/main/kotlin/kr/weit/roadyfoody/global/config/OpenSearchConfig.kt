package kr.weit.roadyfoody.global.config

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.opensearch.client.RestClient
import org.opensearch.client.RestClientBuilder
import org.opensearch.client.RestHighLevelClient
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration(proxyBeanMethods = false)
@EnableElasticsearchRepositories
class OpenSearchConfig(
    private val properties: OpenSearchProperties,
) : AbstractOpenSearchConfiguration() {
    @Bean
    override fun opensearchClient(): RestHighLevelClient {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()

        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials(properties.username, properties.password),
        )
        val builder: RestClientBuilder =
            RestClient
                .builder(
                    HttpHost(
                        properties.uri,
                        properties.port,
                        properties.scheme,
                    ),
                ).setHttpClientConfigCallback { httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                }
        return RestHighLevelClient(builder)
    }
}
