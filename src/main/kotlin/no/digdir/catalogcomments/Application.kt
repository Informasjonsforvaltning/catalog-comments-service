package no.digdir.catalogcomments

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableWebSecurity
open class CommentApplication

fun main(args: Array<String>) {
    runApplication<CommentApplication>(*args)
}
