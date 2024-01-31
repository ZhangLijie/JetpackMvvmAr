package com.dream.jetpackmvvm.ext

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*
import com.dream.jetpackmvvm.base.activity.BaseVmActivity
import com.dream.jetpackmvvm.base.fragment.BaseVmFragment
import com.dream.jetpackmvvm.base.viewmodel.BaseViewModel
import com.dream.jetpackmvvm.ext.util.loge
import com.dream.jetpackmvvm.network.AppException
import com.dream.jetpackmvvm.network.BaseResponse
import com.dream.jetpackmvvm.network.ExceptionHandle
import com.dream.jetpackmvvm.network.manager.NetworkStateManager
import com.dream.jetpackmvvm.state.ResultState
import com.dream.jetpackmvvm.state.paresException
import com.dream.jetpackmvvm.state.paresResult
import com.dream.jetpackmvvm.util.LogUtils

/**
 * 显示页面状态，这里有个技巧，成功回调在第一个，其后两个带默认值的回调可省
 * @param resultState 接口返回值
 * @param onLoading 加载中
 * @param onSuccess 成功回调
 * @param onError 失败回调
 *
 */
fun <T> BaseVmActivity<*>.parseState(
    resultState: ResultState<T>,
    onSuccess: (T) -> Unit,
    onError: ((AppException) -> Unit)? = null,
    onLoading: (() -> Unit)? = null,
) {
    when (resultState) {
        is ResultState.Loading -> {
            showLoading(resultState.loadingMessage)
            onLoading?.run { this }
        }
        is ResultState.Success -> {
            dismissLoading()
            onSuccess(resultState.data)
        }
        is ResultState.Error -> {
            dismissLoading()
            onError?.run { this(resultState.error) }
        }
    }
}

/**
 * 显示页面状态，这里有个技巧，成功回调在第一个，其后两个带默认值的回调可省
 * @param resultState 接口返回值
 * @param onLoading 加载中
 * @param onSuccess 成功回调
 * @param onError 失败回调
 *
 */
fun <T> BaseVmFragment<*>.parseState(
    resultState: ResultState<T>,
    onSuccess: (T) -> Unit,
    onError: ((AppException) -> Unit)? = null,
    onLoading: ((message: String) -> Unit)? = null,
) {
    when (resultState) {
        is ResultState.Loading -> {
            if (onLoading == null) {
                showLoading(resultState.loadingMessage)
            } else {
                onLoading.invoke(resultState.loadingMessage)
            }
        }
        is ResultState.Success -> {
            dismissLoading()
            onSuccess(resultState.data)
        }
        is ResultState.Error -> {
            dismissLoading()
            onError?.run { this(resultState.error) }
        }
    }
}


/**
 * net request 不校验请求结果数据是否是成功
 * @param block 请求体方法
 * @param resultState 请求回调的ResultState数据
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.request(
    block: suspend () -> BaseResponse<T>,
    resultState: MutableLiveData<ResultState<T>>,
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中...",
): Job {
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) resultState.value = ResultState.onAppLoading(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            resultState.paresResult(it)
        }.onFailure {
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            resultState.paresException(it)
        }
    }
}

/**
 * net request 不校验请求结果数据是否是成功
 * @param block 请求体方法
 * @param resultState 请求回调的ResultState数据
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.requestNoCheck(
    block: suspend () -> T,
    resultState: MutableLiveData<ResultState<T>>,
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中...",
): Job {
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) resultState.value = ResultState.onAppLoading(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            resultState.paresResult(it)
        }.onFailure {
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            resultState.paresException(it)
        }
    }
}

fun canRequestData(): Boolean {
    var type = NetworkStateManager.instance.mNetworkTypeStateCallback.value
    var typeState = type?.netType
    var current_net_state = MMKV.defaultMMKV().decodeString("current_net_state")
    LogUtils.debugInfo("NetworkStateManager::${type?.netType},,,current_net_state::${current_net_state}")

    if (current_net_state == "1") {
        return false
    } else if (current_net_state == "3") {
        return true
    } else if (current_net_state == "2") {
        return typeState == 2
    }
    return true
}


/**
 * 过滤服务器结果，失败抛异常
 * @param block 请求体方法，必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不传
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.request(
    block: suspend () -> BaseResponse<T>,
    success: (T) -> Unit,
    error: (AppException) -> Unit = {},
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中...",
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    return viewModelScope.launch {
        if (!canRequestData()) {
            LogUtils.debugInfo("NetworkStateManager::当前应用无网络权限")
          //  error(AppException(error=Error.APP_NOREQUEST_PREMISSION,Throwable()))
            return@launch
        }
        LogUtils.debugInfo("NetworkStateManager::当前应用有网络权限")
        runCatching {
            if (isShowDialog) loadingChange.showDialog.postValue(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it) { t ->
                    success(t)
                }
            }.onFailure { e ->
                //打印错误消息
                e.message?.loge()
                //打印错误栈信息
                e.printStackTrace()
                //失败回调
                error(ExceptionHandle.handleException(e))
            }
        }.onFailure {
            //网络请求异常 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            //打印错误消息
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            //失败回调
            error(ExceptionHandle.handleException(it))
        }
    }
}


fun <T> BaseViewModel.requestList(
    block: suspend () -> MutableList<T>,
    success: (MutableList<T>) -> Unit,
    error: (AppException) -> Unit = {},
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中...",
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) loadingChange.showDialog.postValue(loadingMessage)
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponseList(it) { t ->
                    success(t)
                }
            }.onFailure { e ->
                //打印错误消息
                e.message?.loge()
                //打印错误栈信息
                e.printStackTrace()
                //失败回调
                error(ExceptionHandle.handleException(e))
            }
        }.onFailure {
            //网络请求异常 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            //打印错误消息
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            //失败回调
            error(ExceptionHandle.handleException(it))
        }
    }
}

/**
 *  不过滤请求结果
 * @param block 请求体 必须要用suspend关键字修饰
 * @param success 成功回调
 * @param error 失败回调 可不给
 * @param isShowDialog 是否显示加载框
 * @param loadingMessage 加载框提示内容
 */
fun <T> BaseViewModel.requestNoCheck(
    block: suspend () -> T,
    success: (T) -> Unit,
    error: (AppException) -> Unit = {},
    isShowDialog: Boolean = false,
    loadingMessage: String = "请求网络中...",
): Job {
    //如果需要弹窗 通知Activity/fragment弹窗
    if (isShowDialog) loadingChange.showDialog.postValue(loadingMessage)
    return viewModelScope.launch {
        runCatching {
            //请求体
            block()
        }.onSuccess {
            //网络请求成功 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            //成功回调
            success(it)
        }.onFailure {
            //网络请求异常 关闭弹窗
            loadingChange.dismissDialog.postValue(false)
            //打印错误消息
            it.message?.loge()
            //打印错误栈信息
            it.printStackTrace()
            //失败回调
            error(ExceptionHandle.handleException(it))
        }
    }
}

/**
 * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则会抛出异常
 */
suspend fun <T> executeResponse(
    response: BaseResponse<T>,
    success: suspend CoroutineScope.(T) -> Unit,
) {
    coroutineScope {
        if (response.isSucces()) {

            if (response.getResponseData() != null) {
                success(response.getResponseData())
            } else {
                throw AppException(
                    response.getResponseCode(),
                    response.getResponseMsg(),
                    response.getResponseMsg()
                )
                success(response.getResponseData())
            }
        } else if ("000029".equals(response.getResponseCode()) ||
            "000031".equals(response.getResponseCode()) ||
            "000032".equals(response.getResponseCode()) ||
            "000034".equals(response.getResponseCode())
        ) {
            LogUtils.debugInfo("executeResponse :${response.getResponseCode()}")
        } else throw AppException(
            response.getResponseCode(),
            response.getResponseMsg(),
            response.getResponseMsg()
        )
    }
}

suspend fun <T> executeResponseList(
    response: MutableList<T>,
    success: suspend CoroutineScope.(MutableList<T>) -> Unit,
) {
    coroutineScope {
        when {

            response.size > -1 -> {
                success(response)
            }
            else -> {
                throw AppException(
                    "response.getResponseCode()",
                    "中台数据异常",
                    "response.getResponseMsg()"
                )
//                throw AppException(
//                    response.getResponseCode(),
//                    response.getResponseMsg(),
//                    response.getResponseMsg()
//                )
            }
        }
    }
}

/**
 *  调用携程
 * @param block 操作耗时操作任务
 * @param success 成功回调
 * @param error 失败回调 可不给
 */
fun <T> BaseViewModel.launch(
    block: () -> T,
    success: (T) -> Unit,
    error: (Throwable) -> Unit = {},
) {
    viewModelScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                block()
            }
        }.onSuccess {
            success(it)
        }.onFailure {
            error(it)
        }
    }
}
