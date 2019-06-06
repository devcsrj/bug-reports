package com.github.devcsrj

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RpcFilterTest.Context::class)
class RpcFilterTest {

    class Context {

        @Bean
        fun mockWebServer(): MockWebServer {
            val server = MockWebServer()
            server.start()
            return server
        }

        @Bean
        fun rpcFilterFactory() = RpcFilterFactory()

        @Bean
        fun routes(mockWebServer: MockWebServer,
                   rpcFilterFactory: RpcFilterFactory,
                   builder: RouteLocatorBuilder): RouteLocator {
            return builder.routes().route { p ->
                p
                        .path("/**")
                        .filters { spec -> spec.filter(rpcFilterFactory.apply {}) }
                        .uri(mockWebServer.url("/").uri())
            }.build()
        }
    }


    @Autowired
    lateinit var server: MockWebServer

    @LocalServerPort
    var port: Int = 0

    @Test
    fun test() {
        server.enqueue(MockResponse().setResponseCode(200))
        val request = Request.Builder()
                .get()
                .url("http://localhost:$port/greet/RJ")
                .build()

        val response = httpClient().newCall(request).execute()
        // Not important, instead go to the logs.
        // You should see MockWebServer crashing with this stacktrace:
        /**
         * java.lang.StringIndexOutOfBoundsException: String index out of range: -1
        at java.lang.String.substring(String.java:1967) ~[na:1.8.0_112]
        at okhttp3.mockwebserver.MockWebServer.readRequest(MockWebServer.java:703) ~[mockwebserver-3.14.2.jar:na]
        at okhttp3.mockwebserver.MockWebServer.access$1500(MockWebServer.java:103) ~[mockwebserver-3.14.2.jar:na]
        at okhttp3.mockwebserver.MockWebServer$4.processOneRequest(MockWebServer.java:566) ~[mockwebserver-3.14.2.jar:na]
        at okhttp3.mockwebserver.MockWebServer$4.processConnection(MockWebServer.java:530) ~[mockwebserver-3.14.2.jar:na]
        at okhttp3.mockwebserver.MockWebServer$4.execute(MockWebServer.java:456) ~[mockwebserver-3.14.2.jar:na]
        at okhttp3.internal.NamedRunnable.run(NamedRunnable.java:32) [okhttp-3.14.2.jar:na]
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_112]
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_112]
        at java.lang.Thread.run(Thread.java:745) [na:1.8.0_112]
         */
    }

    private fun httpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }
}
