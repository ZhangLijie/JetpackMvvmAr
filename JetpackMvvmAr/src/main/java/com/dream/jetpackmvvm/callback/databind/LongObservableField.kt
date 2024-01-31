package com.dream.jetpackmvvm.callback.databind

import androidx.databinding.ObservableField

/**
 *    Author : Zhang Lijie
 *    E-mail : lijiezhang521@gmail.com
 *    Date   : 2022/9/7 21:26
 *    Desc   :
 */
class LongObservableField (value: Long = 0L) : ObservableField<Long>(value) {

    override fun get(): Long {
        return super.get()!!
    }
}