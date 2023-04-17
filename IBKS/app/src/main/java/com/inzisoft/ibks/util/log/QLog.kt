package com.inzisoft.ibks.util.log

import android.util.Log
import com.inzisoft.ibks.BuildConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object QLog {

    private const val TAG = "Q_LOG"

    fun init(dirPath: String) {
        Logger.clearLogAdapters()
        Logger.addLogAdapter(
            AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                    .tag(TAG)
                    .methodCount(0)
                    .showThreadInfo(false)
                    .build()
            )
        )
        Logger.addLogAdapter(DiskLogAdapter(LogFileStrategy.newBuilder(dirPath).build()))
    }

    fun t(tag:String? = TAG): QLog {
        Logger.t(tag)
        return this
    }

    fun v(message: String, tag:String? = TAG) {
        if (!BuildConfig.DEBUG) return
        try {
            Logger.t(tag).v(message)
        } catch (e: Exception) {
            Log.v(tag, message)
        }
    }

    fun d(message: String, tag:String? = TAG) {
        if (!BuildConfig.DEBUG) return

        try {
            Logger.t(tag).d(message)
        } catch (e: Exception) {
            Log.d(tag, message)
        }
    }

    fun i(message: String, tag:String? = TAG) {
        try {
            Logger.t(tag).i(message)
        } catch (e: Exception) {
            Log.i(tag, message)
        }
    }

    fun w(message: String, tag:String? = TAG) {
        try {
            Logger.t(tag).w(message)
        } catch (e: Exception) {
            Log.w(tag, message)
        }
    }

    fun w(message: String, tr: Throwable, tag:String? = TAG) {
        try {
            Logger.t(tag).w("${tr.message} : $message")
        } catch (e: Exception) {
            Log.w(tag, message, tr)
        }
    }

    fun w(tr: Throwable, tag:String? = TAG) {
        try {
            Logger.t(tag).w("${tr.message}")
        } catch (e: Exception) {
            Log.w(tag, tr)
        }
    }

    fun e(message: String, tag:String? = TAG) {
        try {
            Logger.t(tag).e(message)
        } catch (e: Exception) {
            Log.e(tag, message)
        }
    }

    fun e(message: String, tr: Throwable, tag:String? = TAG) {
        try {
            Logger.t(tag).e(tr, message)
        } catch (e: Exception) {
            Log.e(tag, message, tr)
        }
    }

    fun e(tr: Throwable, tag:String? = TAG) {
        try {
            Logger.t(tag).e("${tr.message}")
        } catch (e: Exception) {
            Log.e(tag, tr.message ?: "unknown")
        }
    }

    fun json(json: String, tag:String? = TAG) {
        try {
            Logger.t(tag).json(json)
        } catch (e: Exception) {
            Log.e(tag, json)
        }
    }

    fun xml( xml: String, tag:String? = TAG) {
        try {
            Logger.t(tag).xml(xml)
        } catch (e: Exception) {
            Log.e(tag, xml)
        }
    }

}