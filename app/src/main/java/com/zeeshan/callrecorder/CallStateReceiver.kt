package com.zeeshan.callrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/**
 * Listens for system-wide call state changes (RINGING / OFFHOOK / IDLE).
 * This fires regardless of which app is set as the default dialer, so it
 * catches calls placed from this app's own keypad as well as calls handled
 * by the phone's normal dialer.
 *
 * OFFHOOK preceded by RINGING = an incoming call was answered.
 * OFFHOOK with no preceding RINGING = an outgoing call connected.
 */
class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                lastState = TelephonyManager.EXTRA_STATE_RINGING
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (lastState != TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    val isIncoming = lastState == TelephonyManager.EXTRA_STATE_RINGING
                    val number = if (isIncoming) {
                        incomingNumber ?: "Unknown"
                    } else {
                        CallSession.pendingOutgoingNumber ?: "Unknown"
                    }
                    RecordingService.start(context, number, isIncoming)
                }
                lastState = TelephonyManager.EXTRA_STATE_OFFHOOK
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                    RecordingService.stop(context)
                }
                lastState = TelephonyManager.EXTRA_STATE_IDLE
                CallSession.pendingOutgoingNumber = null
            }
        }
    }

    companion object {
        // BroadcastReceiver instances are short-lived, so this needs to be
        // static to persist across the RINGING -> OFFHOOK -> IDLE sequence.
        @Volatile
        private var lastState: String = TelephonyManager.EXTRA_STATE_IDLE
    }
}
