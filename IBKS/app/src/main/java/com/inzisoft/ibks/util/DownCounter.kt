package com.inzisoft.ibks.util

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DownCounter(val scope: CoroutineScope) {
    companion object {
        private const val TIME = 3L * 60
        private const val INTERVAL = 1000L
    }

    private var timerJob: Job? = null
    private val mutex = Mutex()
    private var t = 0L

    fun isRun(): Boolean {
        return timerJob?.let {
            it.isActive && !it.isCancelled && !it.isCompleted
        } ?: false
    }

    fun start(tick: (count: Long) -> Unit) {
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.IO) {
            mutex.lock()
            t = TIME
            mutex.unlock()

            while (true) {
                if (t < 0L) break

                delay(INTERVAL)
                tick(mutex.withLock {
                    t--
                })
            }
        }
    }

    fun stop() {
        scope.launch {
            mutex.withLock { t = 0L }
            timerJob?.cancel()
            timerJob = null
        }

    }
}