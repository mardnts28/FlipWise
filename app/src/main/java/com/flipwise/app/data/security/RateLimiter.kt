package com.flipwise.app.data.security

object RateLimiter {
    private val timestamps = mutableMapOf<String, MutableList<Long>>()

    // Returns true if action is ALLOWED, false if rate limited
    fun isAllowed(action: String, maxCount: Int, windowMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val times = timestamps.getOrPut(action) { mutableListOf() }

        // Remove timestamps outside the window
        times.removeAll { now - it > windowMs }

        return if (times.size < maxCount) {
            times.add(now)
            true
        } else {
            false
        }
    }

    fun reset(action: String) {
        timestamps.remove(action)
    }
}