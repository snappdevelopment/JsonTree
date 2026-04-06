package com.sebastianneubauer.jsontree.util

import kotlinx.atomicfu.atomic

internal class IdGenerator {
    private val atomicLong = atomic(0L)
    fun incrementAndGet(): Long {
        return atomicLong.incrementAndGet()
    }
}
