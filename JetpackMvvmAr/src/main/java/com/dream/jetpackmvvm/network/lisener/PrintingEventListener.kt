package com.zlj.gsoncompatiblelib.lisener

import com.dream.jetpackmvvm.util.LogUtils
import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.atomic.AtomicLong


/**
 *    Author : Zhang Lijie
 *    E-mail : lijiezhang521@gmail.com
 *    Date   : 2023/10/10 15:26
 *    Desc   :
 */
class PrintingEventListener(callId: Long, callStartNanos: Long) :
    EventListener() {
    val callId: Long
    val callStartNanos: Long

    init {
        this.callId = callId
        this.callStartNanos = callStartNanos
    }

    private fun printEvent(name: String) {
        val elapsedNanos = System.nanoTime() - callStartNanos
        LogUtils.debugInfo("PrintingEventListener","${callId} ${elapsedNanos / 1000000000.0} ${name}" )
        //System.out.printf("%04d %.3f %s%n", callId, elapsedNanos / 1000000000.0, name)
    }

    override fun callStart(call: Call?) {
        printEvent("callStart")
    }

    override fun dnsStart(call: Call?, domainName: String?) {
        printEvent("dnsStart")
    }

    override fun dnsEnd(call: Call?, domainName: String?, inetAddressList: List<InetAddress?>?) {
        printEvent("dnsEnd")
    }

    override fun connectStart(
        call: Call?, inetSocketAddress: InetSocketAddress?, proxy: Proxy?
    ) {
        printEvent("connectStart")
    }

    override fun secureConnectStart(call: Call?) {
        printEvent("secureConnectStart")
    }

    override fun secureConnectEnd(call: Call?, handshake: Handshake?) {
        printEvent("secureConnectEnd")
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: okhttp3.Protocol?
    ) {
        printEvent("connectEnd")
    }

    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: okhttp3.Protocol?,
        ioe: IOException
    ) {
        printEvent("connectFailed")
    }

    override fun connectionAcquired(call: Call?, connection: Connection?) {
        printEvent("connectionAcquired")
    }

    override fun connectionReleased(call: Call?, connection: Connection?) {
        printEvent("connectionReleased")
    }

    override fun requestHeadersStart(call: Call?) {
        printEvent("requestHeadersStart")
    }

    override fun requestHeadersEnd(call: Call?, request: Request?) {
        printEvent("requestHeadersEnd")
    }

    override fun requestBodyStart(call: Call?) {
        printEvent("requestBodyStart")
    }

    override fun requestBodyEnd(call: Call?, byteCount: Long) {
        printEvent("requestBodyEnd")
    }

    override fun responseHeadersStart(call: Call?) {
        printEvent("responseHeadersStart")
    }

    override fun responseHeadersEnd(call: Call?, response: Response?) {
        printEvent("responseHeadersEnd")
    }

    override fun responseBodyStart(call: Call?) {
        printEvent("responseBodyStart")
    }

    override fun responseBodyEnd(call: Call?, byteCount: Long) {
        printEvent("responseBodyEnd")
    }

    override fun callEnd(call: Call?) {
        printEvent("callEnd")
    }

    override fun callFailed(call: Call?, ioe: IOException?) {
        printEvent("callFailed")
    }

    companion object {
         val FACTORY: Factory = object : Factory {
            val nextCallId = AtomicLong(1L)
            override fun create(call: Call): EventListener {
                val callId = nextCallId.getAndIncrement()
                System.out.printf("%04d %s%n", callId, call.request().url())
                return PrintingEventListener(callId, System.nanoTime())
            }
        }
    }
}