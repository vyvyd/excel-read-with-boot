package com.demo.helloexcelboot.openlibrary

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class OpenLibraryConfiguration {

    @Bean
    fun openLibraryAPIClient(): OpenLibraryAPIClient {
        val webClient = WebClient.builder()
            .baseUrl("http://localhost:6557") //TODO: extract to application.properties
            .codecs { it.defaultCodecs().maxInMemorySize(1024 * 1024) }
            .build()

        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient))
            .build()
            .createClient(OpenLibraryAPIClient::class.java)
    }

}