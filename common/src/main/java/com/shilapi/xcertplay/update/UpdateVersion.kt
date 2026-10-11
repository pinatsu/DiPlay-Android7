package com.shilapi.xcertplay.update

internal object UpdateVersion {
    private val TRIPLE = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")
    private val ANDROID7_REVISION = Regex("android7-r(\\d+)", RegexOption.IGNORE_CASE)

    internal fun parse(value: String): Triple<Int, Int, Int>? =
        TRIPLE.find(value)?.let { match ->
            Triple(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())
        }

    internal fun isNewer(remote: String, installed: String): Boolean {
        val target = parse(remote) ?: return false
        val current = parse(installed) ?: return true
        if (target.first != current.first) return target.first > current.first
        if (target.second != current.second) return target.second > current.second
        if (target.third != current.third) return target.third > current.third

        val targetRevision = revision(remote) ?: return false
        val currentRevision = revision(installed) ?: return true
        return targetRevision > currentRevision
    }

    private fun revision(value: String): Int? =
        ANDROID7_REVISION.find(value)?.groupValues?.get(1)?.toInt()
}
