package com.jovines.autohandleleave.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jovines.autohandleleave.bean.WeCquptCard
import com.jovines.autohandleleave.bean.SchoolLeavingApproval
import com.jovines.autohandleleave.bean.StatusData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// 查询该学生是否打卡
fun checkCheckInToday(student_id: String): Boolean {
    val gson = Gson()
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .build()

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val key = JsonObject().apply {
        addProperty("xh", student_id)
        addProperty("timestamp", Date().time / 1000)
    }
    val encode = Base64.getEncoder().encodeToString(key.toString().toByteArray())
    val requestBody = JsonObject().apply {
        addProperty("key", encode)
    }.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://we.cqu.pt/api/mrdk/get_mrdk_list_test.php")
        .addHeader("Host", "we.cqupt.edu.cn")
        .addHeader("charset", "utf-8")
        .addHeader(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; MI 6 Build/RQ3A.210605.005; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.62 XWEB/2853 MMWEBSDK/20210601 Mobile Safari/537.36 MMWEBID/8301 MicroMessenger/8.0.7.1920(0x28000737) Process/appbrand0 WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64 MiniProgramEnv/android"
        )
        .addHeader("content-type", "application/json")
        .addHeader("Referer", "https://servicewechat.com/wx8227f55dc4490f45/116/page-frame.html")
        .post(requestBody)
        .build()
    val execute = okHttpClient.newCall(request).execute()
    val message = execute.body?.string() ?: ""
    try {
        val parseString = JsonParser.parseString(message)
        if (parseString.isJsonObject) {
            val weCquptCard = gson.fromJson(message, WeCquptCard::class.java)
            weCquptCard.data?.getOrNull(0)?.let { data ->
                if (!data.created_at.isNullOrBlank()) {
                    val build = Calendar.Builder().setInstant(simpleDateFormat.parse(data.created_at)).build()
                    if (build[Calendar.DAY_OF_YEAR] == Calendar.getInstance()[Calendar.DAY_OF_YEAR]) {
                        return true
                    }
                }
            }
        }
    } catch (e: Throwable) {
        val file = File("cache/err.txt")
        if (!file.exists()) file.createNewFile()
        file.appendText("$message\n")
        return false
    }
    return false
}

// 查询请假学生
fun checkAskForLeave(unifiedCode: String): MutableList<SchoolLeavingApproval.Result> {
    if (unifiedCode.isEmpty()) return mutableListOf()
    val gson = Gson()
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .build()

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val key = JsonObject().apply {
        addProperty("role", "fdy")
        addProperty("zgh", unifiedCode)
        addProperty("id", "0")
        addProperty("lcztdm", "1")
        addProperty("timestamp", Date().time / 1000)
    }
    val encode = Base64.getEncoder().encodeToString(key.toString().toByteArray())
    val requestBody = JsonObject().apply {
        addProperty("key", encode)
    }.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://we.cqu.pt/api/lxsp/get_lxsp_spxx_list_lsh.php")
        .addHeader("Host", "we.cqupt.edu.cn")
        .addHeader("charset", "utf-8")
        .addHeader(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; MI 6 Build/RQ2A.210505.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.62 XWEB/2791 MMWEBSDK/20210302 Mobile Safari/537.36 MMWEBID/2166 MicroMessenger/8.0.3.1880(0x28000339) Process/appbrand0 WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64 MiniProgramEnv/android"
        )
        .addHeader("content-type", "application/json")
        .addHeader("Referer", "https://servicewechat.com/wx8227f55dc4490f45/106/page-frame.html")
        .post(requestBody)
        .build()
    val mutableListOf = mutableListOf<SchoolLeavingApproval.Result>()
    try {
        val execute = okHttpClient.newCall(request).execute()
        val message = execute.body?.string() ?: ""
        val isResultJsonArray = ((JsonParser.parseString(message) as JsonObject)
            .get("data") as JsonObject)
            .get("result").isJsonArray
        if (isResultJsonArray) {
            val schoolLeavingApproval = gson.fromJson<SchoolLeavingApproval>(message, SchoolLeavingApproval::class.java)
            schoolLeavingApproval.data?.result?.forEach {
                mutableListOf.add(it)
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return mutableListOf
}

// 对请假进行审批
fun approvalOfLeave(
    unifiedCode: String,
    logId: String,
    agreeOrNot: Boolean,
    name: String
): Boolean {
    val gson = Gson()
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .build()

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date()
    val key = JsonObject().apply {
        addProperty("type", "fdysp")
        addProperty("spfdy", name)
        addProperty("fdyspsj", simpleDateFormat.format(date))
        addProperty("fdyspjg", if (agreeOrNot) "同意" else "驳回")
        addProperty("spfdygh", unifiedCode)
        addProperty("fdyyj", "")
        addProperty("approve", if (agreeOrNot) "agree" else "reject")
        addProperty("log_id", logId)
        addProperty("timestamp", Date().time / 1000)
    }
    val encode = Base64.getEncoder().encodeToString(key.toString().toByteArray())
    val requestBody = JsonObject().apply {
        addProperty("key", encode)
    }.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://we.cqupt.edu.cn/api/lxsp/post_lxsp_spxx_lsh_test0914.php")
        .addHeader("Host", "we.cqupt.edu.cn")
        .addHeader("charset", "utf-8")
        .addHeader(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; MI 6 Build/RQ2A.210505.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.62 XWEB/2791 MMWEBSDK/20210302 Mobile Safari/537.36 MMWEBID/2166 MicroMessenger/8.0.3.1880(0x28000339) Process/appbrand0 WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64 MiniProgramEnv/android"
        )
        .addHeader("content-type", "application/json")
        .addHeader("Referer", "https://servicewechat.com/wx8227f55dc4490f45/106/page-frame.html")
        .post(requestBody)
        .build()
    val execute = okHttpClient.newCall(request).execute()
    val message = execute.body?.string() ?: ""
    var whetherSucceed = false
    try {
        val schoolLeavingApproval = gson.fromJson<StatusData>(message, StatusData::class.java)
        if (schoolLeavingApproval.status == 200) whetherSucceed = true
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return whetherSucceed
}


