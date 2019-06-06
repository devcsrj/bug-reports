package com.github.devcsrj

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.nio.charset.StandardCharsets

/**
 * An pre-filter that rewrites request as a remote procedure call, by rewriting
 * the entire request in the request body.
 *
 * The specification is generally written in the form of:
 * ```
 * VERSION
 * METHOD url
 * header1:value1
 * header2:value2
 *
 * Body
 * ```
 *
 * The `Version` is a metadata used for determining the parsing
 * version. This is arbitrary, which is agreed upon by the server
 * and the client. This is ended by `\n`:
 *
 * ```
 * 1.0\n
 * ```
 *
 * The next line is composed of the HTTP method and the path. The
 * first 8 bytes is allocated - for the HTTP method (7 bytes as max
 * for `OPTIONS`), followed by a space, then the actual path:
 *
 * ```
 * POST    /the/actual/path
 * ```
 *
 * The next lines correspond to key-value pairs representing the headers.
 * The header name and header value is separated by `:`, ending with
 * a newline `\n`:
 *
 * ```
 * header1:header1
 * header2:header2
 * ```
 *
 * The header group is followed by another newline `\n`, and finally
 * writes the body:
 * ```
 * the original body
 * ```
 *
 * The entire request is sent as a `POST` request.
 */
class RpcFilterFactory : AbstractGatewayFilterFactory<RpcFilterFactory.Config>(Config::class.java) {

    private val version = "1.0"

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            var size = 0L
            val buffer = exchange.response.bufferFactory().allocateBuffer()
            size += version(buffer)
            size += pathInfo(buffer, exchange.request)
            size += headers(buffer, exchange.request)

            val headers = HttpHeaders()
            headers.putAll(exchange.request.headers)
            // Content type will always be text/plain
            headers.contentType = MediaType.TEXT_PLAIN

            val body = DataBufferUtils.join(Flux.just(buffer).mergeWith(exchange.request.body))

            val request = object : ServerHttpRequestDecorator(exchange.request) {

                override fun getMethodValue(): String {
                    // https://github.com/spring-cloud/spring-cloud-gateway/issues/539#issuecomment-499437638
                    return "POST"
                }

                override fun getHeaders(): HttpHeaders {
                    val contentLength = headers.contentLength
                    val newHeaders = HttpHeaders()
                    newHeaders.putAll(super.getHeaders())
                    if (contentLength > 0) {
                        newHeaders.contentLength = size + contentLength
                    } else {
                        newHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    }
                    return newHeaders
                }

                override fun getBody() = body.toFlux()
            }
            chain.filter(exchange.mutate().request(request).build())
        }
    }

    private fun headers(buffer: DataBuffer,
                        request: ServerHttpRequest): Long {
        var size = 0L
        for (h in request.headers) {
            buffer.apply {
                for (v in h.value) {
                    val line = "${h.key}:$v\n"
                    write(line, StandardCharsets.UTF_8)
                    size += line.length
                }
            }
        }
        buffer.write('\n'.toByte())
        size++

        return size
    }

    private fun pathInfo(buffer: DataBuffer,
                         request: ServerHttpRequest): Long {
        var size = 0L
        buffer.apply {
            val method = request.methodValue

            write(method, StandardCharsets.UTF_8)
            size += method.length

            val remaining = 7 - method.length
            val space = ' '.toByte()
            for (i in 0..remaining) {
                write(space)
                size++
            }

            val path = UriComponentsBuilder
                    .fromHttpRequest(request)
                    .toUriString()
            write(path, StandardCharsets.UTF_8)
            size += path.length

            write("\n", StandardCharsets.UTF_8)
            size++
        }
        return size
    }

    private fun version(buffer: DataBuffer): Long {
        val content = "$version\n".toByteArray()
        buffer.write(content)
        return content.size.toLong()
    }

    class Config : AbstractGatewayFilterFactory.NameConfig() {

    }
}
