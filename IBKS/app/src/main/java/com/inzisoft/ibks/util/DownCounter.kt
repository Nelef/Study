package com.inzisoft.ibks.util

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DownCounter(val scope: CoroutineScope) {
    var time = 7L * 60
    var interval = 1000L

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
            t = time
            mutex.unlock()

            while (true) {
                if (t <= 0L) break

                delay(interval)
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