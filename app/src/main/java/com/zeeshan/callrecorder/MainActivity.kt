package com.zeeshan.callrecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zeeshan.callrecorder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val enteredNumber = StringBuilder()

    private val requiredPermissions: Array<String>
        get() {
            val perms = mutableListOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            return perms.toTypedArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNeededPermissions()
        setupKeypad()

        binding.callButton.setOnClickListener { placeCall() }
        binding.backspaceButton.setOnClickListener { backspace() }
        binding.recordingsButton.setOnClickListener {
            startActivity(Intent(this, RecordingsActivity::class.java))
        }
    }

    private fun requestNeededPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupKeypad() {
        val buttons = listOf(
            binding.key0 to "0", binding.key1 to "1", binding.key2 to "2",
            binding.key3 to "3", binding.key4 to "4", binding.key5 to "5",
            binding.key6 to "6", binding.key7 to "7", binding.key8 to "8",
            binding.key9 to "9", binding.keyStar to "*", binding.keyHash to "#"
        )
        buttons.forEach { (button, digit) ->
            button.setOnClickListener {
                enteredNumber.append(digit)
                binding.numberDisplay.text = enteredNumber.toString()
            }
        }
    }

    private fun backspace() {
        if (enteredNumber.isNotEmpty()) {
            enteredNumber.deleteCharAt(enteredNumber.length - 1)
            binding.numberDisplay.text = enteredNumber.toString()
        }
    }

    private fun placeCall() {
        val number = enteredNumber.toString()
        if (number.isEmpty()) return

        // So CallStateReceiver can label the recording once the call connects.
        CallSession.pendingOutgoingNumber = number

        val hasCallPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        } else {
            // Falls back to the system dialer with the number pre-filled if
            // CALL_PHONE was never granted.
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        }
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
