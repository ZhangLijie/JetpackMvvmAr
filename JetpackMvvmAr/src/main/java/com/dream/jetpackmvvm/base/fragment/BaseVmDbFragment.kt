package com.dream.jetpackmvvm.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dream.jetpackmvvm.base.viewmodel.BaseViewModel
import com.dream.jetpackmvvm.ext.inflateBindingWithGeneric
import com.dream.jetpackmvvm.util.LogUtils

/**
 * 作者　:
 * 描述　: ViewModelFragment基类，自动把ViewModel注入Fragment和Databind注入进来了
 * 需要使用Databind的清继承它
 */
abstract class BaseVmDbFragment<VM : BaseViewModel, DB : ViewDataBinding> : BaseVmFragment<VM>() {

    override fun layoutId() = 0

    //该类绑定的ViewDataBinding
    private var _binding: DB? = null
    var currentViewState = false
    val mDatabind: DB get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBindingWithGeneric(inflater, container, false)
        currentViewState = true
        return mDatabind?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LogUtils.debugInfo("BaseVmDbFragment", "BaseVmDbFragment onDestroyView ")
        currentViewState = false
        _binding = null
    }
}