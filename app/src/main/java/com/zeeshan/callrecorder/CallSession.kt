package com.zeeshan.callrecorder

/**
 * Tiny in-memory bridge between MainActivity (which knows the number it just
 * dialed) and CallStateReceiver (which only sees phone state changes, not
 * which number was dialed on an outgoing call).
 */
object CallSession {
    @Volatile
    var pendingOutgoingNumber: String? = null
}
