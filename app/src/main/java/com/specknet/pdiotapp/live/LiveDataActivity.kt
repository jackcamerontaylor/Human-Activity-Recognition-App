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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.ThingyLiveData
import org.tensorflow.lite.Interpreter
import kotlin.collections.ArrayList
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.specknet.pdiotapp.ui.theme.YourAppTheme
import androidx.compose.material3.Text
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.specknet.pdiotapp.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

class PredictionViewModel : ViewModel() {
    private val _predictedActivity = MutableLiveData<String>("Waiting for prediction...")
    val predictedActivity: LiveData<String> get() = _predictedActivity

    fun setPredictedActivity(activity: String) {
        _predictedActivity.value = activity
    }
}


class LiveDataActivity : ComponentActivity() {

    private val predictionViewModel: PredictionViewModel by viewModels()

    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    var time = 0f
    lateinit var allRespeckData: LineData

    lateinit var allThingyData: LineData

    lateinit var respeckChart: LineChart
    lateinit var thingyChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    lateinit var looperThingy: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tflite = Interpreter(loadModelFile())
        setupRespeckReceiver()

        // Set up the broadcast receiver for Thingy
        setupThingyReceiver()

        setContent {
            YourAppTheme {
                    // Call your composable function here
                    MainContent(predictionViewModel) // Your main composable function
            }
        }

        // Initialise the TensorFlow Lite interpreter

    }
        // set up the broadcast receiver
    private fun setupRespeckReceiver() {
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    val gyro = liveData.gyro
                    val gyroX = gyro.x
                    val gyroY = gyro.y
                    val gyroZ = gyro.z

                    addSensorDataToBuffer(x, y, z, gyroX, gyroY, gyroZ)

                    time += 1

                }
            }
        }

        // register receiver on another thread
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

        // set up the broadcast receiver
    private fun setupThingyReceiver() {
        thingyLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_THINGY_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.THINGY_LIVE_DATA) as ThingyLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    time += 1

                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        this.registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)

    }

    @Composable
    fun MainContent(viewModel: PredictionViewModel){
        var backgroundColour by remember { mutableStateOf(Color.Magenta) }
        val predictedActivity by viewModel.predictedActivity.observeAsState("Waiting for prediction...")
        backgroundColour = when (predictedActivity) {
            "Ascending Stairs" -> Color(0xFF006400) // DarkGreen
            "Shuffle Walking" -> Color(0xFF00008B)  // DarkBlue
            "Sitting or Standing" -> Color(0xFFA9A9A9) // DarkGray
            "Misc Movement" -> Color(0xFF008080)    // Teal
            "Normal Walking" -> Color(0xFF556B2F)   // Olive
            "Lying Down" -> Color(0xFF708090)       // SlateGray
            "Descending Stairs" -> Color(0xFF800000) // Maroon
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
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val imageResId = when (predictedActivity) {
            "Ascending Stairs" -> R.drawable.ascending_stairs
            "Shuffle Walking" -> R.drawable.shuffle_walking
            "Sitting or Standing" -> R.drawable.sitting_or_standing
            "Normal Walking" -> R.drawable.normal_walking
            "Lying Down" -> R.drawable.lying_down
            "Descending Stairs" -> R.drawable.descending_stairs
            else -> null  // Default image
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()

        ) {
            imageResId?.let { Image(
                painter = painterResource(id = imageResId), // Replace with your image name
                contentDescription = "Description of the image",
                modifier = Modifier
                    .align(Alignment.Center) // Center the image in the Box
                    .offset(screenWidth * 0.1f)


            ) } ?: run {}
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

    // NEW CODE HERE

    // Function to load the model from assets folder
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("model.tflite")  // Make sure this matches your model file name
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    val windowSize = 50  // Number of time steps
    val featureSize = 6  // accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z
    val slidingWindowBuffer = ArrayList<FloatArray>(windowSize)

    fun addSensorDataToBuffer(accelX: Float, accelY: Float, accelZ: Float, gyroX: Float, gyroY: Float, gyroZ: Float) {
        slidingWindowBuffer.add(floatArrayOf(accelX, accelY, accelZ, gyroX, gyroY, gyroZ))

        // If the buffer is full, run inference
        if (slidingWindowBuffer.size >= windowSize) {
            runInference()  // Call this function when buffer is ready
            slidingWindowBuffer.clear()  // Clear the buffer for the next sliding window
        }
    }

    private fun convertToByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * windowSize * featureSize)  // 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())  // Use native byte order

        for (dataPoint in slidingWindowBuffer) {
            for (value in dataPoint) {
                byteBuffer.putFloat(value)
            }
        }

        return byteBuffer
    }

    fun runInference() {
        // Convert the buffer to ByteBuffer
        val inputBuffer = convertToByteBuffer()

        // Prepare output buffer to store the model’s predictions
        val outputBuffer = Array(1) { FloatArray(7) }  // Replace NUM_CLASSES with the actual number of classes your model predicts

        // Run the inference
        tflite.run(inputBuffer, outputBuffer)

        // Get the predicted class by taking the argmax of the output
        val predictedClass = outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: -1

        // Display the prediction in the UI
        displayPrediction(predictedClass)
    }

    fun displayPrediction(predictedClass: Int) {
        val activityLabels = arrayOf("Ascending Stairs", "Shuffle Walking", "Sitting or Standing", "Misc Movement", "Normal Walking", "Lying Down", "Descending Stairs")

        // Ensure you're on the main thread before updating the UI
        runOnUiThread {
            if (predictedClass in activityLabels.indices) {
                val predictedActivity = activityLabels[predictedClass]
                predictionViewModel.setPredictedActivity(predictedActivity)
            } else {
                Log.e("Prediction Error", "Invalid predicted class index: $predictedClass")
            }
        }
    }


    // NEW CODE ENDS HERE




    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        unregisterReceiver(thingyLiveUpdateReceiver)
        looperRespeck.quit()
        looperThingy.quit()
    }
}
