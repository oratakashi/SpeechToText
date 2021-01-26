package com.oratakashi.myapplication

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.oratakashi.myapplication.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val speechRecognizer : SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
    }

    private val adapter : MainAdapter by lazy {
        MainAdapter()
    }

    private lateinit var textSpeechListener : TextToSpeech.OnInitListener

    private val tts : TextToSpeech by lazy {
        TextToSpeech(applicationContext, textSpeechListener)
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
                        SpeechRecognizer.ERROR_NETWORK -> {
                            Toast.makeText(applicationContext, "Kesalahan jaringan", Toast.LENGTH_SHORT).show()
                        }
                        SpeechRecognizer.ERROR_SERVER -> {
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
                        adapter.addItem(data[0])
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

            textSpeechListener = TextToSpeech.OnInitListener {
                if(it == TextToSpeech.SUCCESS){
                    tts.language = Locale.forLanguageTag("ID-id")
                }else{
                    Toast.makeText(applicationContext, "TTS start failed", Toast.LENGTH_SHORT).show()
                }
            }

            cvText.setOnClickListener {
                tts.speak(tvText.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
            }

            fab.setOnClickListener {
                if(SpeechRecognizer.isRecognitionAvailable(applicationContext)){
                    if(isRecording){
                        recordAudio(false)
                    }else{
                        recordAudio(true)
                    }
                }else{
                    val appPackageName = "com.google.android.googlequicksearchbox"
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$appPackageName")))
                    } catch (anfe: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                    }
                }
            }

            rvMain.also {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(applicationContext)
            }
        }
    }

    private fun recordAudio(record: Boolean){
        if(!record){
            binding.fab.setImageResource(R.drawable.ic_mic_off)
            isRecording = false
            speechRecognizer.stopListening()
        }else{
            isRecording = true
            binding.fab.setImageResource(R.drawable.ic_mic_on)
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

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}