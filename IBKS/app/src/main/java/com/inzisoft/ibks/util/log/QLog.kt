package com.inzisoft.ibks.util.log

import com.inzisoft.ibks.BuildConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object QLog {

    private const val TAG = "Q_LOG"

    fun init(dirPath: String) {
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
        Logger.t(tag).v(message)
    }

    fun d(message: String, tag:String? = TAG) {
        if (!BuildConfig.DEBUG) return

        Logger.t(tag).d(message)
    }

    fun i(message: String, tag:String? = TAG) {
        Logger.t(tag).i(message)
    }

    fun w(message: String, tag:String? = TAG) {
        Logger.t(tag).w(message)
    }

    fun w(message: String, tr: Throwable, tag:String? = TAG) {
        Logger.t(tag).w("${tr.message} : $message")
    }

    fun w(tr: Throwable, tag:String? = TAG) {
        Logger.t(tag).w("${tr.message}")
    }

    fun e(message: String, tag:String? = TAG) {
        Logger.t(tag).e(message)
    }

    fun e(message: String, tr: Throwable, tag:String? = TAG) {
        Logger.t(tag).e(tr, message)
    }

    fun e(tr: Throwable, tag:String? = TAG) {
        Logger.t(tag).e("${tr.message}")
    }

    fun json(json: String, tag:String? = TAG) {
        Logger.t(tag).json(json)
    }

    fun xml( xml: String, tag:String? = TAG) {
        Logger.t(tag).xml(xml)
    }

}