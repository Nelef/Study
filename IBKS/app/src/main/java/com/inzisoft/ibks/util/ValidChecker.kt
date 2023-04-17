package com.inzisoft.ibks.util

import java.util.regex.Pattern

object ValidChecker {

    fun isValidVersion(value: String): Boolean {
        return Pattern.matches("(\\d{1,3}).(\\d{1,3}).(\\d{1,3})", value)
    }

    fun isValidCellphone(value: String): Boolean {
        return Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", value)
    }

    fun isNumber(value: String): Boolean {
        return Pattern.matches("^\\d*\\S", value)
    }

    fun check( regex: String, value: String): Boolean {
        return Pattern.matches(regex, value)
    }
}

object VersionChecker {
    fun compareVersion(current: String, new: String): Int {
        if (!ValidChecker.isValidVersion(current) || !ValidChecker.isValidVersion(new))
            throw IllegalArgumentException("invalid version (current : $current, new : $new)")

        if (current == new) return 0

        val currentVersions = current.split('.').map { it.toInt() }
        val newVersions = new.split('.').map { it.toInt() }

        if (currentVersions[0] < newVersions[0]) return 1

        if (currentVersions[1] < newVersions[1]) return 1

        if (currentVersions[2] < newVersions[2]) return 1

        return -1
    }
}