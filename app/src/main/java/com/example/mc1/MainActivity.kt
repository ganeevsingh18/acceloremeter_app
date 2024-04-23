//package com.example.mc1
package com.example.mc1

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.compose.foundation.layout.height
import androidx.compose.material.Scaffold
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException
import androidx.lifecycle.lifecycleScope


class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var appDatabase: AppDatabase

    // Orientation values
    private var lastUpdateTimestamp = 0L
    var updateInterval = mutableStateOf(1000L) // 5 seconds
//    var updateInterval= mutableStateOf(1000L)

    private var currentOrientation = mutableStateOf(Triple(0f, 0f, 0f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        appDatabase = AppDatabase.getDatabase(this)

        setContent {
            MaterialTheme {
                MainContent(currentOrientation,updateInterval)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER && System.currentTimeMillis() - lastUpdateTimestamp >= updateInterval.value) {
                lastUpdateTimestamp = System.currentTimeMillis()
                val newOrientation = Triple(it.values[0], it.values[1], it.values[2])
                currentOrientation.value = newOrientation
                lifecycleScope.launch {
                    appDatabase.orientationDao().insert(
                        OrientationEntity(
                            timestamp = lastUpdateTimestamp,
                            x = it.values[0],
                            y = it.values[1],
                            z = it.values[2]
                        )
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }

    fun exportData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataList = appDatabase.orientationDao().getAllOrientations()
            val dataString = dataList.joinToString(separator = "\n") { entity ->
                "${entity.timestamp}, ${entity.x}, ${entity.y}, ${entity.z}"
            }
            val file = File(getExternalFilesDir(null), "OrientationData.txt")
            try {
                file.writeText(dataString)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Data exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error exporting data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}



@Composable
fun OrientationGraphs() {
    val context = LocalContext.current
    val orientationData = remember { mutableStateListOf<OrientationEntity>() }

    LaunchedEffect(true) {
        context.getOrientationData {
            orientationData.addAll(it)
        }
    }

    if (orientationData.isNotEmpty()) {
        LineChartComposable(data = orientationData)
    }
}

suspend fun Context.getOrientationData(updateData: (List<OrientationEntity>) -> Unit) {
    val db = AppDatabase.getDatabase(this)
    withContext(Dispatchers.IO) {
        val data = db.orientationDao().getAllOrientations()
        updateData(data)
    }
}
@Composable
fun ShowGraphsWithButton(viewModel: OrientationViewModel) {
    var showGraphs by remember { mutableStateOf(false) }
    val data = viewModel.orientations.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                showGraphs = !showGraphs
                if (showGraphs) {
                    viewModel.fetchOrientations()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(text = if (showGraphs) "Hide Graphs" else "Show Graphs")
        }

        if (showGraphs && data.isNotEmpty()) {
            LineChartComposable(data = data)
        } else if (showGraphs && data.isEmpty()) {
            Text("No data available", style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun LineChartComposable(data: List<OrientationEntity>) {
    val entriesX = data.mapIndexed { index, orientation -> Entry(index.toFloat(), orientation.x) }
    val entriesY = data.mapIndexed { index, orientation -> Entry(index.toFloat(), orientation.y) }
    val entriesZ = data.mapIndexed { index, orientation -> Entry(index.toFloat(), orientation.z) }

    val dataSetX = LineDataSet(entriesX, "X Orientation").apply { color = android.graphics.Color.RED }
    val dataSetY = LineDataSet(entriesY, "Y Orientation").apply { color = android.graphics.Color.GREEN }
    val dataSetZ = LineDataSet(entriesZ, "Z Orientation").apply { color = android.graphics.Color.BLUE }

    val lineData = LineData(dataSetX, dataSetY, dataSetZ)

    AndroidView(
        modifier= Modifier.fillMaxWidth().height(300.dp),
        factory = { context ->
        LineChart(context).apply {
            this.data = lineData
            description.isEnabled = false
            invalidate() // Refresh the chart with new data
        }
    })
}


@Composable
fun OrientationDisplayWithDebug() {
    val viewModel: OrientationViewModel = viewModel()
    var showData by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                showData = !showData
                if (showData) {
                    viewModel.fetchOrientations()  // Trigger data load when button is clicked
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(text = if (showData) "Hide Data" else "Show Data")
        }

        if (showData) {
            DisplayOrientationData(viewModel)
        }
    }
}

@Composable
fun DisplayOrientationData(viewModel: OrientationViewModel) {
    val data = viewModel.orientations.collectAsState().value

    LazyColumn {
        items(data) { orientation ->
            Text(text = "Timestamp: ${orientation.timestamp}, X: ${orientation.x}, Y: ${orientation.y}, Z: ${orientation.z}")
        }
    }
}
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainContent(
    currentOrientationState: State<Triple<Float, Float, Float>>,
    updateInterval: MutableState<Long>
) {
    val context= LocalContext.current
    val viewModel: OrientationViewModel = viewModel()
    val currentOrientation = currentOrientationState.value
    Scaffold {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current X: ${currentOrientation.first}", style = MaterialTheme.typography.h6)
            Text("Current Y: ${currentOrientation.second}", style = MaterialTheme.typography.h6)
            Text("Current Z: ${currentOrientation.third}", style = MaterialTheme.typography.h6)

            Button(
                onClick = { updateInterval.value+=1000 }, // Assume viewModel can access and modify interval
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Increase Sensing Interval")
            }

            Button(
                onClick = {
                    if (context is MainActivity) {
                        context.exportData()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Export Data")
            }

            OrientationDisplayWithDebug()
            ShowGraphsWithButton(viewModel)
        }
    }
}



