package no.digdir.catalogcomments.utils

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.postgresql.PostgreSQLContainer
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

abstract class ApiTestContext {
    @LocalServerPort
    var port: Int = 0

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues
                .of(
                    "spring.datasource.url=${postgresContainer.jdbcUrl}",
                    "spring.datasource.username=${DB_USER}",
                    "spring.datasource.password=${DB_PASSWORD}",
                ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApiTestContext::class.java)
        var postgresContainer: PostgreSQLContainer

        init {

            startMockServer()

            postgresContainer =
                PostgreSQLContainer("postgres:16")
                    .withDatabaseName(DB_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASSWORD)

            postgresContainer.start()

            populate()

            try {
                val con = URI("http://localhost:5050/ping").toURL().openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    logger.debug("Ping to mock server failed")
                    stopMockServer()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}
