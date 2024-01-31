package com.dream.jetpackmvvm.base.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dream.jetpackmvvm.base.viewmodel.BaseViewModel
import com.dream.jetpackmvvm.ext.getVmClazz
import com.dream.jetpackmvvm.ext.util.notNull
import com.dream.jetpackmvvm.network.manager.NetState
import com.dream.jetpackmvvm.network.manager.NetworkStateManager
import com.dream.jetpackmvvm.util.StatusBarUtil


/**
 * 作者　:
 * 描述　: ViewModelActivity基类，把ViewModel注入进来了
 */
abstract class BaseVmActivity<VM : BaseViewModel> : AppCompatActivity() {

    lateinit var mViewModel: VM

    abstract fun layoutId(): Int

    /**
     * 最大的屏幕亮度
     */
    var maxLight = 0f

    /**
     * 当前的亮度
     */
    var currentLight = 0f

    /**
     * 用来控制屏幕亮度
     */
    var handler: Handler? = null

    /**
     * 延时时间
     */
    var DenyTime = 30 * 1000L


    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun showLoading(message: String = "请求网络中...")

    abstract fun dismissLoading()
    open fun initData() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        initDataBind().notNull({
            setContentView(it)
        }, {
            setContentView(layoutId())
        })
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mViewModel = createViewModel()
        registerUiChange()
        initView(savedInstanceState)
        initData()
        createObserver()
        NetworkStateManager.instance.mNetworkStateCallback.observeInActivity(this, Observer {
            onNetworkStateChanged(it)
        })
        initTimeData()
    }

    /**
     * 网络变化监听 子类重写
     */
    open fun onNetworkStateChanged(netState: NetState) {}

    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()

    /**
     * 注册UI 事件
     */
    private fun registerUiChange() {
        //显示弹窗
        mViewModel.loadingChange.showDialog.observeInActivity(this, Observer {
            showLoading(it)
        })
        //关闭弹窗
        mViewModel.loadingChange.dismissDialog.observeInActivity(this, Observer {
            dismissLoading()
        })
    }

    /**
     * 将非该Activity绑定的ViewModel添加 loading回调 防止出现请求时不显示 loading 弹窗bug
     * @param viewModels Array<out BaseViewModel>
     */
    protected fun addLoadingObserve(vararg viewModels: BaseViewModel) {
        viewModels.forEach { viewModel ->
            //显示弹窗
            viewModel.loadingChange.showDialog.observeInActivity(this, Observer {
                showLoading(it)
            })
            //关闭弹窗
            viewModel.loadingChange.dismissDialog.observeInActivity(this, Observer {
                dismissLoading()
            })
        }
    }

    /**
     * 供子类BaseVmDbActivity 初始化Databinding操作
     */
    open fun initDataBind(): View? {
        return null
    }

    protected open fun setStatusBar() {
        //这里做了两件事情，1.使状态栏透明并使contentView填充到状态栏 2.预留出状态栏的位置，防止界面上的控件离顶部靠的太近。这样就可以实现开头说的第二种情况的沉浸式状态栏了
//        StatusBarUtil.setTransparent(this)
//        StatusBarUtil.setColorNoTranslucent(this, 0xff000000.toInt())
        StatusBarUtil.transparencyBar(this, Color.WHITE)//Color.TRANSPARENT
        StatusBarUtil.StatusBarLightMode(this)
    }

    override fun onPause() {
        super.onPause()
        if(canStopSleep) {
            stopSleepTask()
        }
    }
    var canStopSleep = true

    override fun onResume() {
        super.onResume()
        canStopSleep= true
        startSleepTask()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentLight == 1f) {
            startSleepTask()
        }
        return super.dispatchTouchEvent(ev)
    }

    open fun initTimeData() {
        handler = Handler(Looper.getMainLooper())
        maxLight = GetLightness(this)
    }

    /**
     * 设置亮度
     *
     * @param context
     * @param light
     */
    open fun SetLight(context: Activity, light: Int) {
        currentLight = light.toFloat()
        val localLayoutParams: WindowManager.LayoutParams = context.getWindow().getAttributes()
        localLayoutParams.screenBrightness = light / 255.0f
        context.getWindow().setAttributes(localLayoutParams)
    }

    /**
     * 获取亮度
     *
     * @param context
     * @return
     */
    open fun GetLightness(context: Activity): Float {
        val localLayoutParams: WindowManager.LayoutParams = context.getWindow().getAttributes()
        return localLayoutParams.screenBrightness
    }


    /**
     * 开启休眠任务
     */
    open fun startSleepTask() {
        SetLight(this, maxLight.toInt())
        handler!!.removeCallbacks(sleepWindowTask)
        handler!!.postDelayed(sleepWindowTask, DenyTime)
    }

    /**
     * 结束休眠任务
     */
    open fun stopSleepTask() {
        handler!!.removeCallbacks(sleepWindowTask)
    }

    /**
     * 休眠任务
     */
    var sleepWindowTask = Runnable { SetLight(this@BaseVmActivity, 1) }

}