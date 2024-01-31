package com.dream.jetpackmvvm.network.manager

/**
 *
 * 时间　: 2023/5/2
 * 描述　: 网络变化实体类
 */
class NetTypeState(
    var isSuccess: Boolean = true,
    /**
     * -1 无网络  2 WiFi 3 WiFi与蜂窝网络
     */
    var netType: Int = -1,
)