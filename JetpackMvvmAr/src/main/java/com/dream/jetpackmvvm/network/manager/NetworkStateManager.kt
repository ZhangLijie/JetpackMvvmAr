package com.dream.jetpackmvvm.network.manager

import com.dream.jetpackmvvm.callback.livedata.event.EventLiveData

/**

 * 时间　: 2020/5/2
 * 描述　: 网络变化管理者
 */
class NetworkStateManager private constructor() {

    val mNetworkStateCallback = EventLiveData<NetState>()
    val mNetworkTypeStateCallback = EventLiveData<NetTypeState>()

    companion object {
        val instance: NetworkStateManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkStateManager()
        }
    }

}