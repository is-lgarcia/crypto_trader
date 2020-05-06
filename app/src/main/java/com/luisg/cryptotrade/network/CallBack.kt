package com.luisg.cryptotrade.network

import java.lang.Exception

interface CallBack<T> {
    fun onSuccess(result: T?)
    fun onFailed(exception: Exception)
}