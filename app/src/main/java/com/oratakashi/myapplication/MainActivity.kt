package com.oratakashi.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.oratakashi.myapplication.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val speechRecognizer : SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
    }

    private val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }

        with(binding){
            speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, "true")

            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    etEntry.setText("")
                    etEntry.hint = "Waiting ..."
                }

                override fun onBeginningOfSpeech() {
                    etEntry.setText("")
                    etEntry.hint = "Listening ..."
                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {
                    etEntry.hint = "Ketik pesan"
                    recordAudio(false)
                }

                override fun onError(error: Int) {
                    Log.e("error", "Error $error")
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> {
                            Toast.makeText(applicationContext, "Tidak dikenali", Toast.LENGTH_SHORT).show()
                        }
                        SpeechRecognizer.ERROR_AUDIO -> {
                            Toast.makeText(applicationContext, "Gagal merekam audio", Toast.LENGTH_SHORT).show()
                        }
                        SpeechRecognizer.ERROR_CLIENT -> {
                            Toast.makeText(applicationContext, "Terdapat error di Recognition", Toast.LENGTH_SHORT).show()
                        }
                        SpeechRecognizer.ERROR_NETWORK  -> {
                            Toast.makeText(applicationContext, "Kesalahan jaringan", Toast.LENGTH_SHORT).show()
                        }
                        SpeechRecognizer.ERROR_SERVER    -> {
                            Toast.makeText(applicationContext, "Server sendang gangguan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val data: ArrayList<String> =
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                    Log.e("debug", "Result : $data")
                    if (data.isNotEmpty()) {
                        etEntry.setText(data[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val data: ArrayList<String> =
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

                    Log.e("debug", "Result : $data")
                    Log.e("debug", "Partitial : $partialResults")
                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }
            })

            fab.setOnClickListener {
                if(isRecording){
                    recordAudio(false)
                }else{
                    recordAudio(true)
                }
            }
        }
    }

    private fun recordAudio(record : Boolean){
        if(!record){
            binding.fab.setImageResource(R.drawable.ic_mic_off)
            isRecording = false
            speechRecognizer.stopListening()
            Toast.makeText(applicationContext, "Stop Listening", Toast.LENGTH_SHORT).show()
        }else{
            isRecording = true
            binding.fab.setImageResource(R.drawable.ic_mic_on)
//            Toast.makeText(applicationContext, "Start Listening", Toast.LENGTH_SHORT).show()
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(
                this,
                "Permission Granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}