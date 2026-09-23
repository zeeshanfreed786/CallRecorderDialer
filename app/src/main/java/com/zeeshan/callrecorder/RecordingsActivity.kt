package com.zeeshan.callrecorder

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeeshan.callrecorder.databinding.ActivityRecordingsBinding
import java.io.File

class RecordingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingsBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recordingsList.layoutManager = LinearLayoutManager(this)
        loadRecordings()
    }

    private fun loadRecordings() {
        val dir = File(getExternalFilesDir(null), "recordings")
        val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()

        binding.emptyText.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE

        binding.recordingsList.adapter = RecordingsAdapter(
            files,
            onPlay = { file -> playRecording(file) },
            onDelete = { file ->
                file.delete()
                loadRecordings()
            }
        )
    }

    private fun playRecording(file: File) {
        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.playback_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }
}
