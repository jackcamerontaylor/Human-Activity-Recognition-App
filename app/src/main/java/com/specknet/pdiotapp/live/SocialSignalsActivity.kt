package com.specknet.pdiotapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.history.Activity
import com.specknet.pdiotapp.history.ActivityDao
import com.specknet.pdiotapp.history.ActivityDatabase
import com.specknet.pdiotapp.ui.theme.YourAppTheme
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PredictionBreathingViewModel : ViewModel() {
    private val _predictedActivity = MutableLiveData<String>("Waiting for prediction...")
    val predictedActivity: LiveData<String> get() = _predictedActivity

    fun setPredictedActivity(activity: String) {
        _predictedActivity.value = activity
    }
}

class SocialSignalsActivity : ComponentActivity() {
    private val predictionViewModel: PredictionBreathingViewModel by viewModels()
    private lateinit var tflite: Interpreter
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    private lateinit var looperRespeck: Looper
    private val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)

    val windowSize = 100  // Number of time steps
    val featureSize = 3  // accel_x, accel_y, accel_z
    val slidingWindowBuffer = ArrayList<FloatArray>(windowSize)

    lateinit var db: ActivityDatabase  // Initialize the database
    lateinit var ActivityDao: ActivityDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ActivityDatabase.getDatabase(this)
        ActivityDao = db.activityDao()
        // Initialise the TensorFlow Lite interpreter for social signals
        tflite = Interpreter(loadModelFile("social_signals.tflite"))
        setUpRespeckReceiver()

        setContent {
            YourAppTheme {
                MainContent(predictionViewModel)
            }
        }
        // Set up the broadcast receiver for Respeck data
    }

    @Composable
    fun MainContent(viewModel: PredictionBreathingViewModel){
        var backgroundColour by remember { mutableStateOf(Color.Magenta) }
        val predictedActivity by viewModel.predictedActivity.observeAsState("Waiting for prediction...")
        backgroundColour = when (predictedActivity) {
            "Normal Breathing" -> Color(0xFF006400) // DarkGreen
            "Hyperventilation" -> Color(0xFF00008B)  // DarkBlue
            "Coughing" -> Color(0xFFA9A9A9) // DarkGray
            "Other" -> Color(0xFF008080)    // Teal
            else -> Color(0xFF800080)               // Purple
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColour)
        ) {
            TopBox(predictedActivity)
        }
    }
    val customFontFamily = FontFamily(
        Font(R.font.delius, FontWeight.Normal)
    )
    @Composable
    fun TopBox(predictedActivity: String) {
        val context = LocalContext.current
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val imageResId = when (predictedActivity) {
            "Normal Breathing" -> R.drawable.logo
            "Hyperventilation" -> R.drawable.logo
            "Coughing" -> R.drawable.logo
            "Other" -> R.drawable.logo
            else -> null
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val intent = Intent(
                        context,
                        SocialSignalsActivity::class.java
                    ) // Create an Intent to start LiveDataActivity
                    context.startActivity(intent) // Start the activity
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Switch to Activities",
                    style = TextStyle(
                        fontSize = 20.sp// Adjust size as needed
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight()

            ) {

                imageResId?.let {
                    Image(
                        painter = painterResource(id = imageResId), // Replace with your image name
                        contentDescription = "Description of the image",
                        modifier = Modifier
                            .align(Alignment.Center) // Center the image in the Box
                            .offset(screenWidth * 0.1f)


                    )
                } ?: run {}
                Text(
                    predictedActivity,
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = customFontFamily,
                        fontSize = 40.sp// Adjust size as needed
                    ),
                    modifier = Modifier
                        .offset(x = screenWidth * 0.1f, y = -screenHeight * 0.1f)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    fun setUpRespeckReceiver(){
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
            if (predictedClass in socialSignalLabels.indices) {
                val predictedActivity = socialSignalLabels[predictedClass]
                predictionViewModel.setPredictedActivity(predictedActivity)
                addActivity(System.currentTimeMillis(), predictedActivity)

            } else {
                Log.e("Prediction Error", "Invalid predicted class index: $predictedClass")
            }
        }
    }

    fun addActivity(startTime: Long, description: String) {
        val newActivity = Activity(startTime, description, "Breathing")

        // Use the databaseWriteExecutor to insert the activity on a background thread
        ActivityDatabase.databaseWriteExecutor.execute {
            ActivityDao.insert(newActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()
    }
}
