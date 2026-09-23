package com.zeeshan.callrecorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeeshan.callrecorder.databinding.ItemRecordingBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingsAdapter(
    private val files: List<File>,
    private val onPlay: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecordingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
            .format(Date(file.lastModified()))
        holder.binding.fileName.text = file.name
        holder.binding.fileDate.text = dateStr
        holder.binding.playButton.setOnClickListener { onPlay(file) }
        holder.binding.deleteButton.setOnClickListener { onDelete(file) }
    }

    override fun getItemCount(): Int = files.size
}
