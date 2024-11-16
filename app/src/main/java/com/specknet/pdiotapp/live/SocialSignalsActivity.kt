package com.specknet.pdiotapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SocialSignalsActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    private lateinit var looperRespeck: Looper
    private val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)

    val windowSize = 100  // Number of time steps
    val featureSize = 3  // accel_x, accel_y, accel_z
    val slidingWindowBuffer = ArrayList<FloatArray>(windowSize)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_signals)  // Point to the social signals layout

        // Initialise the TensorFlow Lite interpreter for social signals
        tflite = Interpreter(loadModelFile("social_signals.tflite"))

        // Set up the broadcast receiver for Respeck data
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {
                    val liveData = intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ
                    addSensorDataToBuffer(x, y, z)
                }
            }
        }

        // Register the receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)
    }

    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun addSensorDataToBuffer(x: Float, y: Float, z: Float) {
        slidingWindowBuffer.add(floatArrayOf(x, y, z))
        if (slidingWindowBuffer.size >= windowSize) {
            runInference()
            slidingWindowBuffer.clear()
        }
    }

    private fun convertToByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * windowSize * featureSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        for (dataPoint in slidingWindowBuffer) {
            for (value in dataPoint) {
                byteBuffer.putFloat(value)
            }
        }
        return byteBuffer
    }

    private fun runInference() {
        val inputBuffer = convertToByteBuffer()
        val outputBuffer = Array(1) { FloatArray(4) } // Assuming 4 social signal classes
        tflite.run(inputBuffer, outputBuffer)
        val predictedClass = outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: -1
        displayPrediction(predictedClass)
    }

    private fun displayPrediction(predictedClass: Int) {
        val socialSignalLabels = arrayOf("Normal Breathing", "Coughing", "Hyperventilation", "Other")
        runOnUiThread {
            val predictionTextView = findViewById<TextView>(R.id.socialPredictionTextView)
            predictionTextView.text = if (predictedClass in socialSignalLabels.indices) {
                "Predicted Social Signal: ${socialSignalLabels[predictedClass]}"
            } else {
                "Invalid prediction"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()
    }
}
