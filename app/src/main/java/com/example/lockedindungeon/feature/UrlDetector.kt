package com.example.lockedindungeon.feature

import android.os.Bundle
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
                if(it.viewIdResourceName == getUrlBarViewId(browserName)){
                    // If the URL bar is currently focused, the user is likely still typing or looking at suggestions.
                    // We skip detection in this state to avoid premature blocking from autocomplete.
                    if (it.isFocused) {
                        return@forEach
                    }
                    return parseDomainFromUrl(it.text.toString())
                }
            }
        }

        return ""
    }

    fun redirect(node: AccessibilityNodeInfo, packageName: String, newUrl: String) {
        val browserName = BrowserName.fromPackageName(packageName) ?: return
        val urlBarId = getUrlBarViewId(browserName) ?: return

        val nodes = node.findAccessibilityNodeInfosByViewId(urlBarId)
        if (nodes.isNotEmpty()) {
            val urlBar = nodes[0]
            urlBar.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val bundle = Bundle()
            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newUrl)
            urlBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
        }
    }

    private fun getUrlBarViewId(browserName: BrowserName) : String?{
        return when(browserName){
            BrowserName.CHROME -> "com.android.chrome:id/url_bar"
            BrowserName.CHROME_BETA -> "com.android.chrome:id/url_bar"
            BrowserName.CHROME_DEV -> "com.android.chrome:id/url_bar"
            BrowserName.CHROME_CANARY -> "com.android.chrome:id/url_bar"
            BrowserName.SAMSUNG_INTERNET -> "com.sec.android.app.sbrowser:id/location_bar_edit_text"
            BrowserName.BRAVE -> "com.brave.browser:id/url_bar"
            BrowserName.FIREFOX -> TODO()
            BrowserName.FIREFOX_BETA -> TODO()
            BrowserName.FIREFOX_NIGHTLY -> TODO()
            BrowserName.FIREFOX_FOCUS -> TODO()
            BrowserName.OPERA -> TODO()
            BrowserName.OPERA_MINI -> TODO()
            BrowserName.EDGE -> TODO()
            BrowserName.OPERA_GX -> TODO()
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