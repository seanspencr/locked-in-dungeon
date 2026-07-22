package com.example.lockedindungeon.feature

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.services.AppBlockService.Companion.tag
import com.example.lockedindungeon.utils.parseDomainFromUrl


enum class BrowserName(val packageName: String){
    CHROME("com.android.chrome"),
    CHROME_BETA("com.chrome.beta"),
    CHROME_DEV("com.chrome.dev"),
    CHROME_CANARY("com.chrome.canary"),
    FIREFOX("org.mozilla.firefox"),
    FIREFOX_BETA("org.mozilla.firefox_beta"),
    FIREFOX_NIGHTLY("org.mozilla.fenix"),
    FIREFOX_FOCUS("org.mozilla.focus"),
    SAMSUNG_INTERNET("com.sec.android.app.sbrowser"),
    OPERA("com.opera.browser"),
    OPERA_MINI("com.opera.mini.native"),
    OPERA_GX("com.opera.gx"),
    EDGE("com.microsoft.emmx"),
    BRAVE("com.brave.browser"),
    UC_BROWSER("com.UCMobile.intl"),
    DUCKDUCKGO("com.duckduckgo.mobile.android"),
    VIVALDI("com.vivaldi.browser"),
    KIWI("com.kiwibrowser.browser"),
    YANDEX("com.yandex.browser"),
    MI_BROWSER("com.mi.globalbrowser"),
    HUAWEI_BROWSER("com.huawei.browser"),
    ANDROID_WEBVIEW("com.google.android.webview"),
    ANDROID_STOCK_BROWSER("com.android.browser");

    companion object {
        fun fromPackageName(packageName: String): BrowserName? {
            return entries.find { it.packageName == packageName }
        }
    }
}

class UrlDetector {

    fun getUrl(node : AccessibilityNodeInfo, packageName: String) : String{
        val browserName : BrowserName = BrowserName.fromPackageName(packageName) ?: return ""

        var nodeTree = parseAccessibilityTree(node, mutableListOf<AccessibilityNodeInfo>())
        nodeTree.forEach { it ->

            if(it.viewIdResourceName != null && it.viewIdResourceName != "null" && it.text != null && it.text != "null"){
                Log.d(tag, "viewId : ${it.viewIdResourceName}, descriptin : ${it.contentDescription}, uniqueId = ${it.uniqueId}, className : ${it.className} text : ${it.text} hintText : ${it.hintText ?: "no hint"} packageName : ${it.packageName}")
                if(it.viewIdResourceName == getUrlBarViewId(browserName)){
                    return parseDomainFromUrl(it.text.toString())
                }
            }
        }

        return ""
    }

    private fun getUrlBarViewId(browserName: BrowserName) : String?{
        return when(browserName){
            BrowserName.CHROME -> "com.android.chrome:id/url_bar"
            BrowserName.CHROME_BETA -> "com.android.chrome:id/url_bar"
            BrowserName.CHROME_DEV -> "com.android.chrome:id/url_bar"
            BrowserName.CHROME_CANARY -> "com.android.chrome:id/url_bar"
            BrowserName.FIREFOX -> TODO()
            BrowserName.FIREFOX_BETA -> TODO()
            BrowserName.FIREFOX_NIGHTLY -> TODO()
            BrowserName.FIREFOX_FOCUS -> TODO()
            BrowserName.SAMSUNG_INTERNET -> TODO()
            BrowserName.OPERA -> TODO()
            BrowserName.OPERA_MINI -> TODO()
            BrowserName.OPERA_GX -> TODO()
            BrowserName.EDGE -> TODO()
            BrowserName.BRAVE -> TODO()
            BrowserName.UC_BROWSER -> TODO()
            BrowserName.DUCKDUCKGO -> TODO()
            BrowserName.VIVALDI -> TODO()
            BrowserName.KIWI -> TODO()
            BrowserName.YANDEX -> TODO()
            BrowserName.MI_BROWSER -> TODO()
            BrowserName.HUAWEI_BROWSER -> TODO()
            BrowserName.ANDROID_WEBVIEW -> TODO()
            BrowserName.ANDROID_STOCK_BROWSER -> TODO()
        }
    }

    private fun parseAccessibilityTree(
        root: AccessibilityNodeInfo?,
        parentList: MutableList<AccessibilityNodeInfo>
    ): MutableList<AccessibilityNodeInfo> {
        if (root == null) return parentList

        parentList.add(root)

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null) {
                parseAccessibilityTree(child, parentList)
            }
        }

        return parentList
    }
}