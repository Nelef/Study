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