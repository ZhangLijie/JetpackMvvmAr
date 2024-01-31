package com.dream.jetpackmvvm.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager


object NetworkUtils {
    //没有网络连接
    const val NETWORK_NONE = 0

    //WIFI
    const val NETWORK_WIFI = 1

    //2G网络
    const val NETWORK_2G = 2

    //3G网络
    const val NETWORK_3G = 3

    //4G网络
    const val NETWORK_4G = 4

    //移动网络
    const val NETWORK_MOBILE = 5

    /** Current network is NR(New Radio) 5G.  */
//    val NETWORK_TYPE_NR: Int = TelephonyProtoEnums.NETWORK_TYPE_NR // 20.


    /**
     * 判断是否有网络
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivity = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            ?: return false
        val info = connectivity.activeNetworkInfo
        return info != null && info.state == NetworkInfo.State.CONNECTED
    }

    /**
     * 获取当前网络连接类型
     */
    fun getNetworkType(context: Context): Int {
        //获取系统的网络服务
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                ?: return NETWORK_NONE

        //如果当前没有网络

        //获取当前网络类型，如果为空，返回无网络
        val activeNetInfo = connManager.activeNetworkInfo
        if (activeNetInfo == null || !activeNetInfo.isAvailable) {
            return NETWORK_NONE
        }

        // 判断是不是连接的是不是wifi
        val wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (null != wifiInfo) {
            val state = wifiInfo.state
            if (null != state) if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                return NETWORK_WIFI
            }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (null != networkInfo) {
            val state = networkInfo.state
            val strSubTypeName = networkInfo.subtypeName
            if (null != state) if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                return when (activeNetInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> NETWORK_2G
                    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> NETWORK_3G
                    TelephonyManager.NETWORK_TYPE_LTE -> NETWORK_4G
                    else ->                             //中国移动 联通 电信 三种3G制式
                        if (strSubTypeName.equals(
                                "TD-SCDMA",
                                ignoreCase = true
                            ) || strSubTypeName.equals(
                                "WCDMA",
                                ignoreCase = true
                            ) || strSubTypeName.equals("CDMA2000", ignoreCase = true)
                        ) {
                            NETWORK_3G
                        } else {
                            NETWORK_MOBILE
                        }
                }
            }
        }
        return NETWORK_NONE
    }

    /**
     * 获取当前网络连接的名称
     */
    fun getNetworkTypeName(context: Context): String {
        val networkType = getNetworkType(context)
        LogUtils.debugInfo("getNetworkTypeName:$networkType")
        when (networkType) {
            NETWORK_WIFI -> return "wifi"
            NETWORK_2G -> return "2g"
            NETWORK_3G -> return "3g"
            NETWORK_4G -> return "4g"
            NETWORK_MOBILE -> return "mobile"
            NETWORK_NONE-> return "no_net"
        }
        return ""
    }

    fun changeState(context: Context): Int {

        var networkType = getNetworkTypeName(context)

        when (networkType) {
            "wifi" -> return 2
            "2g", "3g", "4g", "mobile" -> return 3
            "no_net" -> return -1
            else -> {
                return 3
            }
        }

    }
}