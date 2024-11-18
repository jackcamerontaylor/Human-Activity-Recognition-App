package com.specknet.pdiotapp.history

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.specknet.pdiotapp.R
import java.text.SimpleDateFormat
import java.util.*


val customFontFamily = FontFamily(
    Font(R.font.delius, FontWeight.Normal)
)
class ModelType : ViewModel() {
    // MutableState to hold the selected value
    var selectedValue by mutableStateOf("Activity")
        private set

    // Function to change the value
    fun updateSelectedValue(value: String) {
        selectedValue = value
    }
}

class ActivityRepo(context: Context) {
    private val activityDao: ActivityDao

    init {
        val db = ActivityDatabase.getDatabase(context)
        activityDao = db.activityDao()
    }

    // Function to get activities for a specific day
    fun getActivitiesForDay(date: Long, model: String, label: String): LiveData<List<Activity>> {
        val startOfDay = getStartOfDayTimestamp(date)
        val endOfDay = getEndOfDayTimestamp(date)
        return activityDao.getActivitiesForDay(startOfDay, endOfDay, model, label)
    }
}


class DatePickerViewModel : ViewModel() {
    // Get the current date using Calendar
    private val calendar = Calendar.getInstance()
    private val day = calendar.get(Calendar.DAY_OF_MONTH)
    private val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based
    private val year = calendar.get(Calendar.YEAR)

    // Mutable state for the selected date with today's date as the default value
    var selectedDate by mutableStateOf("$day/$month/$year")
        private set

    // Function to update the date
    fun updateSelectedDate(date: String) {
        selectedDate = date
    }
}

class ActivityHistory : ComponentActivity() {
    private lateinit var repository: ActivityRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ActivityRepo(this)
        setContent {
            MaterialTheme {
                // Using the viewModel function to provide the ViewModel to the composable
                Column (
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Spacer(Modifier.height(16.dp))
                    Title(model = viewModel())
                    DatePickerExample(viewModel = viewModel())
                    Data(repository, viewModel = viewModel(), model = viewModel())
                }
            }
        }
    }
}

@Composable
fun Title(model: ModelType) {
    val modeltype = model.selectedValue
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = "Activity",
            modifier = Modifier
                .clickable {model.updateSelectedValue("Activity")},
            style = TextStyle(fontFamily= customFontFamily, fontSize = 30.sp, fontWeight = if (modeltype == "Activity") FontWeight.Bold else FontWeight.Normal)
        )
        Text(
            text = "Breathing",
            modifier = Modifier
                .clickable {model.updateSelectedValue("Breathing")},
            style = TextStyle(fontFamily= customFontFamily, fontSize = 30.sp, fontWeight = if (modeltype == "Breathing") FontWeight.Bold else FontWeight.Normal)
        )

    }

}

fun getStartOfDayTimestamp(date: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun getEndOfDayTimestamp(date: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return calendar.timeInMillis
}

@Composable
fun Data(repository: ActivityRepo, viewModel: DatePickerViewModel, model: ModelType) {
    val selectedDate = viewModel.selectedDate
    val model = model.selectedValue
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = dateFormat.parse(selectedDate)
    val timestamp = date?.time ?: return
    Log.d("Timestamp", "Selected timestamp for $selectedDate: $timestamp")

    val activityLabels =  arrayOf("Sitting or Standing", "Lying Down on Back", "Lying Down on Left",
        "Lying Down on Right", "Lying Down on Stomach", "Ascending Stairs", "Shuffle Walking",
        "Misc Movement", "Normal Walking", "Descending Stairs", "Running")
    val breathingLabels = arrayOf("Normal Breathing", "Coughing", "Hyperventilation", "Other")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = selectedDate,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            style = TextStyle(fontFamily= customFontFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (model == "Activity") {
                activityLabels.forEach { label ->
                    item {
                        ActivityCard(label, repository, timestamp, model)
                    }
                }
            } else {
                breathingLabels.forEach { label ->
                    item {
                        ActivityCard(label, repository, timestamp, model)
                    }
                }
            }
        }
    }

}



@Composable
fun ActivityCard(label: String, repository: ActivityRepo, timestamp: Long, model: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val activities by repository.getActivitiesForDay(timestamp, model, label).observeAsState(emptyList())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label
            )
            Text(
                text = formatTime(activities.count() * 2)
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                BarChartView(perHour(activities, timestamp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

fun perHour(activities: List<Activity>, timestamp: Long): List<Float> {
    val hours = MutableList(24) { 0f }
    for (i in 0 until 24) {
        val start = getStartOfDayTimestamp(timestamp) + i * 3600000
        val end = start + 3600000
        hours[i] = ((filterActivitiesByTimestamp(activities, start, end).count()*2).toFloat())
    }
    return hours
}

fun filterActivitiesByTimestamp(activities: List<Activity>, startTime: Long, endTime: Long): List<Activity> {
    return activities.filter { activity ->
        activity.start in startTime..endTime
    }
}

fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return "${hours} hours ${minutes} minutes ${remainingSeconds} seconds"
}

@Composable
fun DatePickerExample(viewModel: DatePickerViewModel) {
    // State to hold the selected date
    val selectedDate = viewModel.selectedDate

    // Create a Calendar instance
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Use the LocalContext to get the context for DatePickerDialog
    val context = LocalContext.current

    // DatePickerDialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                viewModel.updateSelectedDate(date)
            },
            year, month, day
        )
    }

    // UI for the date picker
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { datePickerDialog.show() }) {
            Text(text = "Pick a Date")
        }
    }
}

@Composable
fun BarChartView(data: List<Float>) { // Example data

    // Create Bar Chart using MPAndroidChart
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), // Set desired height for the chart
        factory = { context ->
            BarChart(context).apply {
                // Create Bar Entries
                val entries = data.mapIndexed { index, value ->
                    BarEntry(index.toFloat(), value)
                }

                // Create DataSet
                val dataSet = BarDataSet(entries, "Sample Data").apply {
                    color = android.graphics.Color.MAGENTA
                    valueTextColor = android.graphics.Color.BLACK
                    setDrawValues(false)
                }

                // Create BarData
                val barData = BarData(dataSet)
                this.data = barData
                this.invalidate()  // Refresh the chart

                // Description
                description.isEnabled = false // Disable description label

                legend.isEnabled = false

                // Axis Configuration // Enable left axis labels
                axisRight.isEnabled = false  // Disable right axis
                axisLeft.isEnabled = false
                xAxis.setDrawLabels(true)
                // Position the X-axis labels at the bottom and rotate them
                xAxis.position = XAxis.XAxisPosition.BOTTOM // Position the labels at the bottom
                xAxis.setDrawGridLines(false)  // Disable grid lines for X-axis
                axisLeft.setDrawGridLines(false) // Disable grid lines for Y-axis
                axisRight.setDrawGridLines(false) // Disable grid lines for the right Y-axis (if enabled)

                // Enable touch and zoom
                isDragEnabled = true
                setTouchEnabled(true)
                setPinchZoom(true)
            }
        }
    )
}
