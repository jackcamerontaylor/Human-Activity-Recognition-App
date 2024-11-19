package com.specknet.pdiotapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService
import com.specknet.pdiotapp.bluetooth.ConnectingActivity
import com.specknet.pdiotapp.history.Activity
import com.specknet.pdiotapp.history.ActivityHistory
import com.specknet.pdiotapp.history.ActivityViewModel
import com.specknet.pdiotapp.live.LiveDataActivity
import com.specknet.pdiotapp.onboarding.OnBoardingActivity
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.Utils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.compose.runtime.Composable
import com.specknet.pdiotapp.ui.theme.YourAppTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import com.specknet.pdiotapp.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.specknet.pdiotapp.live.SocialSignalsActivity


class MainActivity : ComponentActivity() {


    // permissions
    lateinit var permissionAlertDialog: AlertDialog.Builder

    val permissionsForRequest = arrayListOf<String>()

    var blePermissionGranted = false
    var locationPermissionGranted = false
    var cameraPermissionGranted = false
    var readStoragePermissionGranted = false
    var writeStoragePermissionGranted = false

    // broadcast receiver
    val filter = IntentFilter()

    var isUserFirstTime = false
    private lateinit var coordinatorLayout: CoordinatorLayout


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAppTheme {
                // Call your composable function here
                MainContent() // Your main composable function
            }
        }

        // check whether the onboarding screen should be shown
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(Constants.PREF_USER_FIRST_TIME)) {
            isUserFirstTime = false
        }
        else {
            isUserFirstTime = true
            sharedPreferences.edit().putBoolean(Constants.PREF_USER_FIRST_TIME, false).apply()
            val introIntent = Intent(this, OnBoardingActivity::class.java)
            startActivity(introIntent)
        }


        permissionAlertDialog = AlertDialog.Builder(this)

        setupPermissions()

        setupBluetoothService()

        // register a broadcast receiver for respeck status
        filter.addAction(Constants.ACTION_RESPECK_CONNECTED)
        filter.addAction(Constants.ACTION_RESPECK_DISCONNECTED)
    }

    val delius = FontFamily(
        Font(R.font.delius, FontWeight.Normal)
    )

    @Composable
    fun MainContent() {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            // Column for the buttons centered horizontally
            Column(
                modifier = Modifier
                    .fillMaxWidth(), // Add bottom padding to avoid overlap with image
                horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally
                verticalArrangement = Arrangement.Top // Align items at the top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)// Adjust size as needed
                )
                Buttons()
                Spacer(modifier = Modifier.height(50.dp))
                Button(onClick = {
                    val intent = Intent(context, LiveDataActivity::class.java) // Create an Intent to start LiveDataActivity
                    context.startActivity(intent) // Start the activity
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    shape = RectangleShape
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.activity),
                        contentDescription = "Activity",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)// Adjust size as needed
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {
                    val intent = Intent(context, SocialSignalsActivity::class.java) // Create an Intent to start LiveDataActivity
                    context.startActivity(intent) // Start the activity
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    shape = RectangleShape
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.breathing),
                        contentDescription = "Breathing",
                        modifier = Modifier
                            .fillMaxWidth(1f)// Adjust size as needed
                    )
                }
                ///LiveDataActBut()
            }

            // Image at the bottom of the screen
        }
    }

    @Composable
    fun LiveDataActBut() {
        val context = LocalContext.current // Get the current context
        Button(onClick = {
            val intent = Intent(context, LiveDataActivity::class.java) // Create an Intent to start LiveDataActivity
            context.startActivity(intent) // Start the activity
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = RectangleShape
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_page),
                contentDescription = "Pair Sensors",
                modifier = Modifier.fillMaxWidth()// Adjust size as needed
            )
        }
    }

    @Composable
    fun Buttons() {
        val context = LocalContext.current
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center // optional spacing between buttons
        ) {
            Button(onClick = {
                val intent = Intent(context, ConnectingActivity::class.java) // Create an Intent to start LiveDataActivity
                context.startActivity(intent) // Start the activity
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier
                    .weight(1.2f)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.sensors),
                    contentDescription = "Pair Sensors",
                    modifier = Modifier.fillMaxWidth() // Adjust size as needed
                )
            }
            Button(onClick = {
                val intent = Intent(context, ActivityHistory::class.java) // Create an Intent to start LiveDataActivity
                context.startActivity(intent) // Start the activity
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier
                    .weight(1.2f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History",
                    modifier = Modifier.fillMaxWidth() // Adjust size as needed
                )
            }

        }
    }


    fun setupPermissions() {
        // BLE permissions
        Log.i("Permissions", "BLE permission = " + blePermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsForRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        else {
            blePermissionGranted = true
        }


        // location permission
        Log.i("Permissions", "Location permission = " + locationPermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsForRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        else {
            locationPermissionGranted = true
        }

        // camera permission
        Log.i("Permissions", "Camera permission = " + cameraPermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Camera permission = " + cameraPermissionGranted)
            permissionsForRequest.add(Manifest.permission.CAMERA)
        }
        else {
            cameraPermissionGranted = true
        }

        // read storage permission
        Log.i("Permissions", "Read st permission = " + readStoragePermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Read st permission = " + readStoragePermissionGranted)
            permissionsForRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else {
            readStoragePermissionGranted = true
        }

        // write storage permission
        Log.i("Permissions", "Write storage permission = " + writeStoragePermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Write storage permission = " + writeStoragePermissionGranted)
            permissionsForRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        else {
            writeStoragePermissionGranted = true
        }

        if (permissionsForRequest.size >= 1) {
            ActivityCompat.requestPermissions(this,
                permissionsForRequest.toTypedArray(),
                Constants.REQUEST_CODE_PERMISSIONS)
        }

    }

    fun setupBluetoothService() {
        val isServiceRunning = Utils.isServiceRunning(BluetoothSpeckService::class.java, applicationContext)
        Log.i("debug","isServiceRunning = " + isServiceRunning)

        // check sharedPreferences for an existing Respeck id
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(Constants.RESPECK_MAC_ADDRESS_PREF)) {
            Log.i("sharedpref", "Already saw a respeckID, starting service and attempting to reconnect")

            // launch service to reconnect
            // start the bluetooth service if it's not already running
            if(!isServiceRunning) {
                Log.i("service", "Starting BLT service")

                Toast.makeText(this, "Connecting to " + sharedPreferences.getString(
                    Constants.RESPECK_MAC_ADDRESS_PREF, "") + "...", Toast.LENGTH_SHORT).show()
                val simpleIntent = Intent(this, BluetoothSpeckService::class.java)
                this.startService(simpleIntent)
            }
        }
        else {
            Log.i("sharedpref", "No Respeck seen before, must pair first")
            // TODO then start the service from the connection activity
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val simpleIntent = Intent(this, BluetoothSpeckService::class.java)
        this.stopService(simpleIntent)
        System.exit(0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if(grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    when(permissionsForRequest[i]) {
                        Manifest.permission.BLUETOOTH_SCAN -> blePermissionGranted = true
                        Manifest.permission.BLUETOOTH_CONNECT -> blePermissionGranted = true
                        Manifest.permission.ACCESS_COARSE_LOCATION -> locationPermissionGranted = true
                        Manifest.permission.ACCESS_FINE_LOCATION -> locationPermissionGranted = true
                        Manifest.permission.CAMERA -> cameraPermissionGranted = true
                        Manifest.permission.READ_EXTERNAL_STORAGE -> readStoragePermissionGranted = true
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> writeStoragePermissionGranted = true
                    }

                }
            }
        }

        // count how many permissions need granting
        var numberOfPermissionsUngranted = 0
        if (!blePermissionGranted) numberOfPermissionsUngranted++
        if (!locationPermissionGranted) numberOfPermissionsUngranted++
        if (!cameraPermissionGranted) numberOfPermissionsUngranted++
        if (!readStoragePermissionGranted) numberOfPermissionsUngranted++
        if (!writeStoragePermissionGranted) numberOfPermissionsUngranted++

        // show a general message if we need multiple permissions
        if (numberOfPermissionsUngranted > 1) {
            val generalSnackbar = Snackbar
                .make(coordinatorLayout, "Several permissions are needed for correct app functioning", Snackbar.LENGTH_LONG)
                .setAction("SETTINGS") {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
                .show()
        }
        else if(numberOfPermissionsUngranted == 1) {
            var snackbar: Snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_LONG)
            if (!blePermissionGranted) {
                snackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        "BLE permission needed for Bluetooth to work.",
                        Snackbar.LENGTH_LONG
                    )
            }

            if (!locationPermissionGranted) {
                snackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        "Location permission needed for Bluetooth to work.",
                        Snackbar.LENGTH_LONG
                    )
            }

            if(!cameraPermissionGranted) {
                snackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        "Camera permission needed for QR code scanning to work.",
                        Snackbar.LENGTH_LONG
                    )
            }

            if(!readStoragePermissionGranted || !writeStoragePermissionGranted) {
                snackbar = Snackbar
                    .make(
                        coordinatorLayout,
                        "Storage permission needed to record sensor.",
                        Snackbar.LENGTH_LONG
                    )
            }

            snackbar.setAction("SETTINGS") {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
                .show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.show_tutorial) {
            val introIntent = Intent(this, OnBoardingActivity::class.java)
            startActivity(introIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}