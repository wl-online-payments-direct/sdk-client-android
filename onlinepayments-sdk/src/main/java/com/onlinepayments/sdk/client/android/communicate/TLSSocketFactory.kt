/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.communicate

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

internal class TLSSocketFactory(
    private val internalSSLSocketFactory: SSLSocketFactory
): SSLSocketFactory() {

    @JvmSynthetic
    override fun getDefaultCipherSuites(): Array<String> {
        return internalSSLSocketFactory.defaultCipherSuites
    }

    @JvmSynthetic
    override fun getSupportedCipherSuites(): Array<String> {
        return internalSSLSocketFactory.supportedCipherSuites
    }

    @JvmSynthetic
    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket())
    }

    @JvmSynthetic
    @Throws(IOException::class)
    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))
    }

    @JvmSynthetic
    @Throws(IOException::class)
    override fun createSocket(host: String?, port: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @JvmSynthetic
    @Throws(IOException::class)
    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket {
        return enableTLSOnSocket(
            internalSSLSocketFactory.createSocket(
                host,
                port,
                localHost,
                localPort
            )
        )
    }

    @JvmSynthetic
    @Throws(IOException::class)
    override fun createSocket(host: InetAddress?, port: Int): Socket {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    @JvmSynthetic
    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket {
        return enableTLSOnSocket(
            internalSSLSocketFactory.createSocket(
                address,
                port,
                localAddress,
                localPort
            )
        )
    }

    private fun enableTLSOnSocket(socket: Socket): Socket {
        if (socket is SSLSocket) {
            socket.enabledProtocols =
                arrayOf("TLSv1.1", "TLSv1.2")
        }
        return socket
    }
}
