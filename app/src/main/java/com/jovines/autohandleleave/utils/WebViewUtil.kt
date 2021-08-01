package com.jovines.autohandleleave.utils

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * @param action 回调在JavaBridge的线程
 */
@SuppressLint("SetJavaScriptEnabled")
fun Context.getHtmlByWebView(uri: String, webView: WebView? = null, action: (String) -> Unit) {
    val mwWebView = webView ?: WebView(this)
    mwWebView.settings.javaScriptEnabled = true
    class JavaObjectJsInterface {
        @JavascriptInterface // 要加这个注解，不然调用不到
        fun onHtml(html: String?) {
            action.invoke(html ?: "")
        }
    }
    mwWebView.addJavascriptInterface(JavaObjectJsInterface(), "java_obj")
    mwWebView.webViewClient = WebViewClient()
    mwWebView.loadUrl(uri)
    mwWebView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            view.loadUrl("javascript:window.java_obj.onHtml('jhjhj');")
            super.onPageFinished(view, url)
        }
    }
}


///**
// * @param uri 根据具体的uri获得具体的招聘信息
// * @param action 回调，该回调不是在主线程中执行
// */
//fun parseWebPage(
//    context: Context,
//    id: String,
//    action: (data: RecruitmentBean) -> Unit
//) {
//    context.getHtmlByWebViewExpand(id) {
//        val bean = RecruitmentBean()
//        val titleList = mutableListOf<String>()
//        val parse = Jsoup.parse(it)
//        val doubleChoiceInfo = parse.getElementById("doubleChoiceInfo")
//        val articleKeyInfo = doubleChoiceInfo.getElementsByClass("articleKeyInfo")
//        // 基础信息
//        val articleKeyInfoTl = articleKeyInfo.getOrNull(0)?.getElementsByClass("tl")
//        val mutableMap = mutableMapOf<String, String>().apply {
//            articleKeyInfoTl?.forEach {
//                val split = it.getElementsByTag("td").getOrNull(0)?.text()?.split("：")
//                put(split?.getOrNull(0) ?: "", split?.getOrNull(1) ?: "")
//            }
//        }
//
//        // 填充数据类
//        bean.employerName = mutableMap.getOrElse(MainActivity.EMPLOYER_NAME, { "" })
//        bean.time = mutableMap.getOrElse(MainActivity.TIME, { "" })
//        bean.place = mutableMap.getOrElse(MainActivity.PLACE, { "" })
//        bean.teacherCharge = mutableMap.getOrElse(MainActivity.TEACHER, { "" })
//        // 招聘介绍
//        val contentInfo =
//            parse?.getElementById("contentInfo")?.getElementsByClass("articleContext")
//        bean.content = contentInfo?.text() ?: ""
//        val jobGrid = parse.getElementsByClass("page-table").firstOrNull()
//        // 标题
//        jobGrid?.getElementsByTag("tr")?.forEach {
//            val data = mutableListOf<String>()
//            it.children().forEach {
//                data.add(it.text())
//            }
//            bean.dataList.add(data)
//        }
//        // 内容
//        action(bean)
//    }
//}



