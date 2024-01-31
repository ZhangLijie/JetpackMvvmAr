package com.dream.jetpackmvvm.network

import com.dream.jetpackmvvm.util.LogUtils
import okhttp3.Dns
import java.net.InetAddress

/**
 *    Author : Zhang Lijie
 *    E-mail : lijiezhang521@gmail.com
 *    Date   : 2023/11/21 11:17
 *    Desc   :
 */
class MyDNS:Dns {
    override fun lookup(hostname: String): MutableList<InetAddress> {
        val result = mutableListOf<InetAddress>()
        var systemAddressList: MutableList<InetAddress>? = null
        //通过系统DNS解析
        kotlin.runCatching {
            systemAddressList = Dns.SYSTEM.lookup(hostname)
        }.onFailure {
            LogUtils.debugInfo("MyDNS", "lookup: $it")
        }

        if (systemAddressList != null && systemAddressList!!.isNotEmpty()) {
            result.addAll(systemAddressList!!)
        } else {
            //系统DNS解析失败，走自定义路由
            result.add(InetAddress.getByName("www.baidu.com"))
        }

        return result
    }
}