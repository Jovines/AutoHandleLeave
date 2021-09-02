package com.jovines.autohandleleave.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jovines.autohandleleave.bean.Element
import com.jovines.autohandleleave.bean.Version
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException


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


    fun Context.install(apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val contentUri: Uri =
            FileProvider.getUriForFile(this, "$packageName.FileProvider", apkFile)
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        startActivity(intent)
    }


    fun Context.download(things: String, name: String): File {
        val file = File(externalCacheDir?.absolutePath + File.separator + name)
        if (file.exists()) file.delete()
        file.createNewFile()
        val request: Request = Request.Builder().url(things).build()
        val response: Response = OkHttpClient.Builder().build().newCall(request).execute()
        if (!response.isSuccessful) throw IOException() else {
            val sink: BufferedSink = file.sink().buffer()
            response.body?.let { sink.writeAll(it.source()) }
            sink.close()
        }
        return file
    }

}