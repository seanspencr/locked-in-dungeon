package com.example.lockedindungeon.utils

fun isBrowser(packageName : String) : Boolean{
    val browserPackageNames = listOf(
        // Standard & Popular
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.chrome.canary",
        "org.mozilla.firefox",
        "org.mozilla.focus",
        "org.mozilla.fenix",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.opera.gx",
        "com.duckduckgo.mobile.android",
        "com.vivaldi.browser",
        "org.torproject.torbrowser",
        "com.ecosia.android",

        // OEM System Browsers
        "com.sec.android.app.sbrowser",
        "com.sec.android.app.sbrowser.beta",
        "com.mi.globalbrowser",
        "com.android.browser",
        "com.vivo.browser",
        "com.heytap.browser",
        "com.nearme.browser",

        // Alternative & Lightweight
        "mark.via.gp",
        "com.kiwibrowser.browser",
        "com.cloudmosa.puffinFree",
        "com.UCMobile.intl",
        "com.uc.browser.en",
        "com.mycompany.app.soulbrowser"
    )

    return browserPackageNames.contains(packageName)
}