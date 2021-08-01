package com.jovines.autohandleleave.utils

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jovines.autohandleleave.bean.Element
import com.jovines.autohandleleave.bean.Version
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request

object UpdateHelper {

    fun checkoutUpdate(function: (t: Version) -> Unit) {
        Single.create<Version> {
            val build = OkHttpClient.Builder().build()
            val url = Request.Builder()
                .url("https://gitee.com/iamtree/AutoHandleLeave/raw/master/app/release/output-metadata.json")
                .get().build()
            val resJson = build.newCall(url).execute().body?.string() ?: ""
            val parseString = JsonParser.parseString(resJson)
            if (parseString.isJsonObject) {
                val fromJson = Gson().fromJson(resJson, Version::class.java)
                it.onSuccess(fromJson)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(function, {})
    }


    fun getUpdateUri(element: Element): String {
        return "https://gitee.com/iamtree/AutoHandleLeave/blob/master/app/release/${element.outputFile}"
    }
}