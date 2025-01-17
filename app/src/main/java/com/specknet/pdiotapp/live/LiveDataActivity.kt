package com.specknet.pdiotapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.specknet.pdiotapp.R
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
import com.specknet.pdiotapp.history.Activity
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter


class PredictionViewModel : ViewModel() {
    private val _predictedActivity = MutableLiveData<String>("Waiting for Prediction...")
    val predictedActivity: LiveData<String> get() = _predictedActivity

    fun setPredictedActivity(activity: String) {
        _predictedActivity.value = activity
    }
}


class LiveDataActivity : ComponentActivity() {

    private val predictionViewModel: PredictionViewModel by viewModels()

    private lateinit var tflite: Interpreter
    private lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    private lateinit var looperRespeck: Looper
    private val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)

    val windowSize = 50  // Number of time steps
    val featureSize = 3  // accel_x, accel_y, accel_z
    val slidingWindowBuffer = ArrayList<FloatArray>(windowSize)

    lateinit var db: ActivityDatabase  // Initialize the database
    lateinit var ActivityDao: ActivityDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ActivityDatabase.getDatabase(this)
        ActivityDao = db.activityDao()
        tflite = Interpreter(loadModelFile())
        setupRespeckReceiver()

        setContent {
            YourAppTheme {
                    // Call your composable function here
                    MainContent(predictionViewModel) // Your main composable function
            }
        }



    }
        // set up the broadcast receiver
    private fun setupRespeckReceiver() {

        // Set up the broadcast receiver for respeck data

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

        // Register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(
            respeckLiveUpdateReceiver,
            filterTestRespeck,
            null,
            handlerRespeck
        )
    }

    @Composable
    fun MainContent(viewModel: PredictionViewModel){
        ///var backgroundColour by remember { mutableStateOf(Color.Magenta) }
        val predictedActivity by viewModel.predictedActivity.observeAsState("Waiting for prediction...")
        /*backgroundColour = when (predictedActivity) {
            "Ascending Stairs" -> Color(0xFF006400) // DarkGreen
            "Shuffle Walking" -> Color(0xFF00008B)  // DarkBlue
            "Sitting or Standing" -> Color(0xFFA9A9A9) // DarkGray
            "Misc Movement" -> Color(0xFF008080)    // Teal
            "Normal Walking" -> Color(0xFF556B2F)   // Olive
            "Lying Down" -> Color(0xFF708090)       // SlateGray
            "Descending Stairs" -> Color(0xFF800000) // Maroon
            else -> Color(0xFF800080)               // Purple
        } */
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
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
            "Ascending Stairs" -> R.drawable.ascending_stairs
            "Shuffle Walking" -> R.drawable.shuffle_walking
            "Sitting or Standing" -> R.drawable.sitting_or_standing
            "Normal Walking" -> R.drawable.normal_walking
            "Lying Down on Back" -> R.drawable.lying_down
            "Lying Down on Left" -> R.drawable.lying_down_left
            "Lying Down on Right" -> R.drawable.lying_down_right
            "Lying Down on Stomach" -> R.drawable.lying_down_stomach
            "Running" -> R.drawable.running
            "Descending Stairs" -> R.drawable.descending_stairs
            else -> R.drawable.placeholder // Default image
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.top_live), // Replace with your image name
                    contentDescription = "Description of the image",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)

                )
                imageResId?.let {
                    Image(
                        painter = painterResource(id = imageResId), // Replace with your image name
                        contentDescription = "Description of the image",
                        modifier = Modifier // Center the image in the Box
                            .fillMaxWidth()

                    )
                } ?: run {}
                Text(
                    predictedActivity,
                    color = Color.Black,
                    style = TextStyle(
                        fontFamily = customFontFamily,
                        fontSize = 40.sp// Adjust size as needed
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                )
            }
        }

    }

    // NEW CODE HERE

    // Function to load the model from assets folder
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("daily_physical_activity.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun addSensorDataToBuffer(accelX: Float, accelY: Float, accelZ: Float) {
        slidingWindowBuffer.add(floatArrayOf(accelX, accelY, accelZ))
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

    private fun logBatteryLevel(context: Context): Float {
        // Fetch battery information
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        // Calculate battery percentage
        val batteryPct = level * 100 / scale.toFloat()
        Log.d("Battery Level", "Current Battery Level: $batteryPct%")
        return batteryPct
    }

    private fun runInference() {
        val startTime = System.currentTimeMillis() // Log start time

        val inputBuffer = convertToByteBuffer()
        val outputBuffer = Array(1) { FloatArray(11) }
        tflite.run(inputBuffer, outputBuffer)
        val predictedClass = outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: -1

        val endTime = System.currentTimeMillis() // Log end time
        Log.d("Prediction Time", "Inference Time: ${endTime - startTime} ms") // Log time taken for inference
        displayPrediction(predictedClass)
    }

    private fun displayPrediction(predictedClass: Int) {
        val activityLabels = arrayOf("Sitting or Standing", "Lying Down on Back", "Lying Down on Left",
            "Lying Down on Right", "Lying Down on Stomach", "Ascending Stairs", "Shuffle Walking",
            "Misc Movement", "Normal Walking", "Descending Stairs", "Running")

        runOnUiThread {
            if (predictedClass in activityLabels.indices) {
                val predictedActivity = activityLabels[predictedClass]
                predictionViewModel.setPredictedActivity(predictedActivity)
                addActivity(System.currentTimeMillis(), predictedActivity)

            } else {
                Log.e("Prediction Error", "Invalid predicted class index: $predictedClass")
            }
        }
    }

    fun addActivity(startTime: Long, description: String) {
        val newActivity = Activity(startTime, description, "Activity")

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
