package com.example.lockedindungeon.utils

fun parseDomainFromUrl(url: String): String {
    return try {
        // Ensure there's a scheme so URI parsing works correctly
        val normalizedUrl = if (!url.contains("://")) "https://$url" else url

        val uri = java.net.URI(normalizedUrl)
        var host = uri.host ?: return ""

        // Strip common subdomain prefixes that don't change the "real" domain
        val prefixesToStrip = listOf("www.", "m.", "mobile.", "amp.")
        for (prefix in prefixesToStrip) {
            if (host.startsWith(prefix)) {
                host = host.removePrefix(prefix)
                break
            }
        }

        host
    } catch (e: Exception) {
        ""
    }
}