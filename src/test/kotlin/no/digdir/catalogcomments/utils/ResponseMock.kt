package no.digdir.catalogcomments.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.digdir.catalogcomments.utils.jwk.JwkStore

const val LOCAL_SERVER_PORT = 5050

private val mockserver = WireMockServer(LOCAL_SERVER_PORT)

fun startMockServer() {
    if (!mockserver.isRunning) {
        mockserver.stubFor(
            get(urlEqualTo("/ping"))
                .willReturn(aResponse().withStatus(200)),
        )
        mockserver.stubFor(
            get(urlEqualTo("/auth/realms/fdk/protocol/openid-connect/certs"))
                .willReturn(okJson(JwkStore.get())),
        )
        mockserver.start()
    }
}

fun stopMockServer() {
    if (mockserver.isRunning) mockserver.stop()
}
