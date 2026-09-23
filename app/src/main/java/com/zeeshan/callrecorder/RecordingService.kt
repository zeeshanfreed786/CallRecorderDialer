package com.zeeshan.callrecorder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Foreground service that owns the MediaRecorder instance for the lifetime
 * of a call. Recording is via AudioSource.MIC, not the call-audio stream --
 * see the project README for why (Android restricts direct call-audio
 * access on non-preloaded apps from Android 10 onward). For clean two-way
 * audio put the call on speakerphone; on the earpiece this will mostly
 * capture your own voice, with the other side coming through faintly.
 */
class RecordingService : Service() {

    private var recorder: MediaRecorder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopRecording()
            stopSelf()
            return START_NOT_STICKY
        }

        val number = intent?.getStringExtra(EXTRA_NUMBER) ?: "Unknown"
        val isIncoming = intent?.getBooleanExtra(EXTRA_IS_INCOMING, true) ?: true
        startForeground(NOTIFICATION_ID, buildNotification())
        startRecording(number, isIncoming)
        return START_STICKY
    }

    private fun startRecording(number: String, isIncoming: Boolean) {
        if (recorder != null) return // a recording is already in progress

        val dir = File(getExternalFilesDir(null), "recordings")
        if (!dir.exists()) dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val direction = if (isIncoming) "IN" else "OUT"
        val safeNumber = number.replace(Regex("[^0-9+]"), "").ifEmpty { "Unknown" }
        val file = File(dir, "${direction}_${safeNumber}_$timestamp.m4a")

        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder = mr.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            } catch (e: Exception) {
                release()
                recorder = null
            }
        }
    }

    private fun stopRecording() {
        recorder?.let {
            try {
                it.stop()
            } catch (e: Exception) {
                // Can throw if stop() is called within ~1s of start(); the
                // partial file is discarded in that case, which is fine.
            }
            it.release()
        }
        recorder = null
    }

    private fun buildNotification(): Notification {
        val channelId = "call_recording_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "Call recording",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.recording_notification_title))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val EXTRA_NUMBER = "extra_number"
        private const val EXTRA_IS_INCOMING = "extra_is_incoming"
        private const val ACTION_STOP = "action_stop"

        fun start(context: Context, number: String, isIncoming: Boolean) {
            val intent = Intent(context, RecordingService::class.java).apply {
                putExtra(EXTRA_NUMBER, number)
                putExtra(EXTRA_IS_INCOMING, isIncoming)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.startService(Intent(context, RecordingService::class.java).apply {
                action = ACTION_STOP
            })
        }
    }
}
