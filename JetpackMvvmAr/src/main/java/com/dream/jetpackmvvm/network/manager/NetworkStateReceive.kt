package com.dream.jetpackmvvm.network.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.dream.jetpackmvvm.util.LogUtils
import com.dream.jetpackmvvm.util.NetworkUtils.changeState

/**
 * 时间　: 2020/5/2
 * 描述　: 网络变化接收器
 */
class NetworkStateReceive : BroadcastReceiver() {

    var isInit = true
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            if (!isInit) {
                if (!com.dream.jetpackmvvm.network.NetworkUtil.isNetworkAvailable(context)) {
                    //收到没有网络时判断之前的值是不是有网络，如果有网络才提示通知 ，防止重复通知
                    NetworkStateManager.instance.mNetworkStateCallback.value?.let {
                        if (it.isSuccess) {
                            LogUtils.debugInfo("changeState 1::${changeState(context)}")
                            //没网
                            NetworkStateManager.instance.mNetworkStateCallback.value =
                                NetState(isSuccess = false)

                            NetworkStateManager.instance.mNetworkTypeStateCallback.value =
                                NetTypeState(isSuccess = false, netType = changeState(context))
                        } else {
                            LogUtils.debugInfo("changeState 2::${changeState(context)}")
                            NetworkStateManager.instance.mNetworkTypeStateCallback.value =
                                NetTypeState(isSuccess = false, netType = changeState(context))
                        }
                        return
                    }

                    LogUtils.debugInfo("changeState 3::${changeState(context)}")
                    NetworkStateManager.instance.mNetworkStateCallback.value =
                        NetState(isSuccess = false)
                    NetworkStateManager.instance.mNetworkTypeStateCallback.value =
                        NetTypeState(isSuccess = false, netType = changeState(context))
                } else {
                    //收到有网络时判断之前的值是不是没有网络，如果没有网络才提示通知 ，防止重复通知
                    NetworkStateManager.instance.mNetworkStateCallback.value?.let {
                        if (!it.isSuccess) {
                            //有网络了
                            LogUtils.debugInfo("changeState 4::${changeState(context)}")
                            NetworkStateManager.instance.mNetworkStateCallback.value =
                                NetState(isSuccess = true)
                            NetworkStateManager.instance.mNetworkTypeStateCallback.value =
                                NetTypeState(isSuccess = true, netType = changeState(context))
                        } else {
                            LogUtils.debugInfo("changeState 5::${changeState(context)}")
                            NetworkStateManager.instance.mNetworkTypeStateCallback.value =
                                NetTypeState(isSuccess = true, netType = changeState(context))
                        }
                        return
                    }
                    LogUtils.debugInfo("changeState 6::${changeState(context)}")
                    NetworkStateManager.instance.mNetworkStateCallback.value =
                        NetState(isSuccess = true)
                    NetworkStateManager.instance.mNetworkTypeStateCallback.value =
                        NetTypeState(isSuccess = true, netType = changeState(context))
                }
            }
            isInit = false
        }
    }

}