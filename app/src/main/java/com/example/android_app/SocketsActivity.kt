package com.example.android_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SocketsActivity : AppCompatActivity(), LocationListener {

    private val logTag = "ZMQ_CLIENT"
    private val PREFS_NAME = "location_prefs"

    private lateinit var tvSockets: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvOfflineCount: TextView
    private lateinit var tvRSRP: TextView
    private lateinit var tvRSRQ: TextView
    private lateinit var tvRSSI: TextView
    private lateinit var tvFrequency: TextView
    private lateinit var tvIPAddress: TextView
    private lateinit var tvNetworkType: TextView

    private lateinit var handler: Handler
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager

    private val serverAddress = "tcp://172.20.10.14:5566"
    private val offlineFileName = "offline_data.json"

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var isSendingActive = true

    private var lastLat = 0.0
    private var lastLon = 0.0
    private var lastAlt = 0.0

    private var currentLat = 0.0
    private var currentLon = 0.0
    private var currentAlt = 0.0
    private var currentTimestamp: Long = 0
    private var currentTimeStr = ""

    private var currentRSRP = -140
    private var currentRSRQ = -20
    private var currentRSSI = -100
    private var currentFrequency = 0
    private var currentIPAddress = "0.0.0.0"
    private var currentNetworkType = "Unknown"

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val PERMISSION_REQUEST_PHONE_STATE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket)

        initViews()
        initServices()
        setupClickListeners()
        checkPermissions()
        updateOfflineCount()
        startAutoSending()
        updateIPAddress()
        loadSavedLocation()
        startNetworkUpdates()
    }
    private fun initViews() {
        tvSockets = findViewById(R.id.tvSockets)
        tvLat = findViewById(R.id.tvLatitude)
        tvLon = findViewById(R.id.tvLongitude)
        tvAlt = findViewById(R.id.tvAltitude)
        tvTime = findViewById(R.id.tvTime)
        tvOfflineCount = findViewById(R.id.tvOfflineCount)
        tvRSRP = findViewById(R.id.tvRSRP)
        tvRSRQ = findViewById(R.id.tvRSRQ)
        tvRSSI = findViewById(R.id.tvRSSI)
        tvFrequency = findViewById(R.id.tvFrequency)
        tvIPAddress = findViewById(R.id.tvIPAddress)

        val networkTypeView = findViewById<TextView?>(R.id.tvNetworkType)
        if (networkTypeView != null) {
            tvNetworkType = networkTypeView
        } else {
            Log.e(logTag, "tvNetworkType not found in layout")
        }
    }

    private fun initServices() {
        handler = Handler(Looper.getMainLooper())
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnRetryOffline).setOnClickListener {
            retryOfflineData()
        }

        findViewById<Button>(R.id.btnCheckPermissions).setOnClickListener {
            checkPermissions()
        }
    }

    private fun startNetworkUpdates() {
        scheduler.scheduleWithFixedDelay({
            if (isSendingActive) {
                updateNetworkParameters()
            }
        }, 1, 2, TimeUnit.SECONDS)
    }

    private fun checkPermissions() {
        val missingPermissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.READ_PHONE_STATE)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_ACCESS_LOCATION
            )
        } else {
            Toast.makeText(this, "Все разрешения предоставлены", Toast.LENGTH_SHORT).show()
            startLocationUpdates()
            updateNetworkParameters()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            var allGranted = true
            val deniedPermissions = mutableListOf<String>()

            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(logTag, "Разрешение предоставлено: ${permissions[i]}")
                } else {
                    allGranted = false
                    deniedPermissions.add(permissions[i])
                    Log.d(logTag, "Разрешение отклонено: ${permissions[i]}")
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Все разрешения предоставлены", Toast.LENGTH_SHORT).show()
                startLocationUpdates()
                updateNetworkParameters()
            } else {
                val message = "Отклонены разрешения: ${deniedPermissions.joinToString()}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                for (permission in deniedPermissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        showPermissionRationale()
                        break
                    }
                }
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Необходимы разрешения")
            .setMessage("Необходимы разрешения")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadSavedLocation() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lat = prefs.getFloat("latitude", 0f).toDouble()
        val lon = prefs.getFloat("longitude", 0f).toDouble()
        val alt = prefs.getFloat("altitude", 0f).toDouble()
        val timestamp = prefs.getLong("timestamp", 0)
        val timeStr = prefs.getString("time_string", "")

        if (lat != 0.0 && lon != 0.0 && !timeStr.isNullOrEmpty()) {
            currentLat = lat
            currentLon = lon
            currentAlt = alt
            currentTimestamp = timestamp
            currentTimeStr = timeStr

            handler.post {
                tvLat.text = "Latitude: $lat"
                tvLon.text = "Longitude: $lon"
                tvAlt.text = "Altitude: ${String.format("%.2f м", alt)}"
                tvTime.text = "Time: $timeStr"
                tvSockets.text = "Загружены данные из GpsActivity"
            }

            Log.d(logTag, "Loaded location from GpsActivity: $lat, $lon")

            handler.postDelayed({
                sendLocationData()
            }, 1000)
        } else {
            tvSockets.text = "Нет данных от GpsActivity. Используется автоматическое обновление GPS."
        }
    }

    private fun startLocationUpdates() {
        if (checkLocationPermissions()) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, this)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 1f, this)

                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    lastLocation?.let {
                        onLocationChanged(it)
                    }

                    handler.post {
                        tvSockets.text = "Поиск местоположения..."
                    }
                }
            } catch (e: Exception) {
                Log.e(logTag, "Error starting location updates: ${e.message}")
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        val alt = location.altitude
        val timestamp = location.time

        val sdf = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
        val timeStr = sdf.format(Date(timestamp))

        currentLat = lat
        currentLon = lon
        currentAlt = alt
        currentTimestamp = timestamp
        currentTimeStr = timeStr

        handler.post {
            tvLat.text = "Latitude: $lat"
            tvLon.text = "Longitude: $lon"
            tvAlt.text = "Altitude: ${String.format("%.2f м", alt)}"
            tvTime.text = "Time: $timeStr"
        }

        Log.d(logTag, "Location updated: $lat, $lon")
    }
    private fun updateNetworkParameters() {
        updateCellularParameters()
        updateWifiInfo()
        updateIPAddress()
        updateNetworkType()
    }

    private fun updateCellularParameters() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    handler.post {
                        tvRSRP.text = "RSRP: нет разрешения"
                        tvRSRQ.text = "RSRQ: нет разрешения"
                    }
                    return
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cellInfo = telephonyManager.allCellInfo
                if (cellInfo.isNullOrEmpty()) {
                    handler.post {
                        tvRSRP.text = "RSRP: нет данных"
                        tvRSRQ.text = "RSRQ: нет данных"
                    }
                    return
                }

                var found = false
                for (info in cellInfo) {
                    when (info) {
                        is android.telephony.CellInfoLte -> {
                            val signal = info.cellSignalStrength
                            val identity = info.cellIdentity
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                currentRSRP = signal.getRsrp()
                                currentRSRQ = signal.getRsrq()
                                currentRSSI = signal.getRssi()
                                currentFrequency = identity.getEarfcn()
                            } else {
                                currentRSRP = signal.dbm
                                currentRSRQ = signal.asuLevel
                                currentRSSI = signal.dbm
                                currentFrequency = identity.earfcn
                            }

                            handler.post {
                                tvRSRP.text = "RSRP: $currentRSRP dBm"
                                tvRSRQ.text = "RSRQ: $currentRSRQ dB"
                                tvFrequency.text = "Freq: $currentFrequency (LTE)"
                            }
                            found = true
                            break
                        }
                        is android.telephony.CellInfoWcdma -> {
                            val signal = info.cellSignalStrength
                            val identity = info.cellIdentity

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                currentRSSI = signal.getDbm()
                                currentFrequency = identity.getUarfcn()
                            } else {
                                currentRSSI = signal.dbm
                                currentFrequency = identity.uarfcn
                            }

                            handler.post {
                                tvRSRP.text = "RSRP: N/A (3G)"
                                tvRSRQ.text = "RSRQ: N/A (3G)"
                                tvRSSI.text = "RSSI: $currentRSSI dBm"
                                tvFrequency.text = "Freq: $currentFrequency (3G)"
                            }
                            found = true
                            break
                        }
                        is android.telephony.CellInfoGsm -> {
                            val signal = info.cellSignalStrength
                            val identity = info.cellIdentity

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                currentRSSI = signal.getDbm()
                                currentFrequency = identity.getArfcn()
                            } else {
                                currentRSSI = signal.dbm
                                currentFrequency = identity.arfcn
                            }

                            handler.post {
                                tvRSRP.text = "RSRP: N/A (GSM)"
                                tvRSRQ.text = "RSRQ: N/A (GSM)"
                                tvRSSI.text = "RSSI: $currentRSSI dBm"
                                tvFrequency.text = "Freq: $currentFrequency (GSM)"
                            }
                            found = true
                            break
                        }
                        is android.telephony.CellInfoNr -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val signal = info.cellSignalStrength
                                val identity = info.cellIdentity
                                handler.post {
                                    tvRSRP.text = "RSRP: $currentRSRP dBm (5G)"
                                    tvRSRQ.text = "RSRQ: $currentRSRQ dB (5G)"
                                    tvRSSI.text = "RSSI: $currentRSSI dBm"
                                    tvFrequency.text = "Freq: $currentFrequency (5G)"
                                }
                                found = true
                                break
                            }
                        }
                    }
                }

                if (!found) {
                    handler.post {
                        tvRSRP.text = "RSRP: тип сети не определен"
                        tvRSRQ.text = "RSRQ: тип сети не определен"
                        tvFrequency.text = "Freq: тип сети не определен"
                    }
                }
            } else {
                handler.post {
                    tvRSRP.text = "RSRP: API < 29"
                    tvRSRQ.text = "RSRQ: API < 29"
                    tvFrequency.text = "Freq: API < 29"
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error getting cellular info: ${e.message}")
            handler.post {
                tvRSRP.text = "RSRP: ошибка"
                tvRSRQ.text = "RSRQ: ошибка"
                tvFrequency.text = "Freq: ошибка"
            }
        }
    }

    private fun updateWifiInfo() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                val wifiInfo = wifiManager.connectionInfo
                val rssi = wifiInfo.rssi
                var ssid = wifiInfo.ssid
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length - 1)
                }

                if (rssi != -127) {
                    currentRSSI = rssi
                    handler.post {
                        tvRSSI.text = "WiFi RSSI: $rssi dBm"
                        if (ssid != "<unknown ssid>" && ssid.isNotBlank()) {
                            tvRSSI.text = "${tvRSSI.text} ($ssid)"
                        }
                    }
                } else {
                    handler.post {
                        tvRSSI.text = "RSSI: $currentRSSI dBm (сотовый)"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error getting WiFi info: ${e.message}")
        }
    }

    private fun updateNetworkType() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

                currentNetworkType = when {
                    networkCapabilities == null -> "No network"
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                    else -> "Unknown"
                }

                if (::tvNetworkType.isInitialized) {
                    handler.post {
                        tvNetworkType.text = "Сеть: $currentNetworkType"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error getting network type: ${e.message}")
        }
    }

    private fun updateIPAddress() {
        try {
            val ip = getIPAddress()
            currentIPAddress = ip
            handler.post {
                tvIPAddress.text = "IP: $ip"
            }
        } catch (e: Exception) {
            currentIPAddress = "0.0.0.0"
            handler.post {
                tvIPAddress.text = "IP: ошибка получения"
            }
        }
    }

    private fun getIPAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val hostAddress = address.hostAddress
                        if (hostAddress.indexOf(':') < 0) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error getting IP: ${e.message}")
        }
        return "0.0.0.0"
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(logTag, "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(logTag, "Provider disabled: $provider")
        Toast.makeText(this, "GPS выключен", Toast.LENGTH_SHORT).show()
    }

    private fun startAutoSending() {
        scheduler.scheduleWithFixedDelay({
            if (isSendingActive) {
                checkAndSendLocation()
            }
        }, 2, 3, TimeUnit.SECONDS)
    }

    private fun checkAndSendLocation() {
        val latChanged = Math.abs(currentLat - lastLat) > 0.000001
        val lonChanged = Math.abs(currentLon - lastLon) > 0.000001
        val altChanged = Math.abs(currentAlt - lastAlt) > 0.5

        if ((latChanged || lonChanged || altChanged) && currentLat != 0.0) {
            Log.d(logTag, "Location changed! Sending...")
            sendLocationData()
            lastLat = currentLat
            lastLon = currentLon
            lastAlt = currentAlt
        }
    }

    private fun sendLocationData() {
        Thread {
            try {
                Log.d(logTag, "Sending location data...")

                val context = ZContext()
                val socket = context.createSocket(SocketType.REQ)

                socket.setReceiveTimeOut(5000)
                socket.setSendTimeOut(5000)
                socket.setLinger(0)

                socket.connect(serverAddress)

                val jsonData = JSONObject().apply {
                    put("latitude", currentLat)
                    put("longitude", currentLon)
                    put("altitude", currentAlt)
                    put("time", currentTimestamp)
                    put("device_id", Build.MODEL)
                    put("provider", "gps")
                    put("rsrp", currentRSRP)
                    put("rsrq", currentRSRQ)
                    put("rssi", currentRSSI)
                    put("frequency", currentFrequency)
                    put("ip_address", currentIPAddress)
                    put("network_type", currentNetworkType)
                }

                val request = jsonData.toString()
                socket.send(request.toByteArray(ZMQ.CHARSET), 0)

                val replyBytes = socket.recv(0)

                if (replyBytes != null) {
                    val reply = String(replyBytes, ZMQ.CHARSET)
                    Log.d(logTag, "Received: $reply")

                    handler.post {
                        tvSockets.text = "Отправлено: $reply"
                    }

                    socket.close()
                    context.close()

                } else {
                    throw Exception("Нет ответа от сервера")
                }

            } catch (e: Exception) {
                Log.e(logTag, "Ошибка отправки: ${e.message}")
                saveOfflineData()

                handler.post {
                    tvSockets.text = "Оффлайн: данные сохранены"
                    updateOfflineCount()
                }
            }
        }.start()
    }

    private fun saveOfflineData() {
        try {
            val jsonData = JSONObject().apply {
                put("latitude", currentLat)
                put("longitude", currentLon)
                put("altitude", currentAlt)
                put("time", currentTimestamp)
                put("device_id", Build.MODEL)
                put("provider", "gps")
                put("saved_time", System.currentTimeMillis())
                put("rsrp", currentRSRP)
                put("rsrq", currentRSRQ)
                put("rssi", currentRSSI)
                put("frequency", currentFrequency)
                put("ip_address", currentIPAddress)
                put("network_type", currentNetworkType)
            }

            val file = File(filesDir, offlineFileName)
            FileWriter(file, true).use { it.write(jsonData.toString() + "\n") }

            Log.d(logTag, "Data saved offline")

        } catch (e: Exception) {
            Log.e(logTag, "Error saving offline: ${e.message}")
        }
    }

    private fun retryOfflineData() {
        Thread {
            try {
                val file = File(filesDir, offlineFileName)
                if (!file.exists()) {
                    handler.post {
                        Toast.makeText(this, "Нет оффлайн данных", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val offlineData = mutableListOf<String>()
                BufferedReader(FileReader(file)).use { reader ->
                    reader.forEachLine { line ->
                        if (line.isNotBlank()) offlineData.add(line)
                    }
                }

                if (offlineData.isEmpty()) {
                    file.delete()
                    return@Thread
                }

                var successCount = 0
                val failedData = mutableListOf<String>()

                for (data in offlineData) {
                    try {
                        if (sendSingleOfflineData(data)) {
                            successCount++
                        } else {
                            failedData.add(data)
                        }
                    } catch (e: Exception) {
                        failedData.add(data)
                    }
                }

                if (failedData.isEmpty()) {
                    file.delete()
                    handler.post {
                        tvSockets.text = "Все оффлайн данные отправлены!"
                        updateOfflineCount()
                        Toast.makeText(this, "Все данные отправлены", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    FileWriter(file).use { writer ->
                        failedData.forEach { writer.write(it + "\n") }
                    }
                    handler.post {
                        tvSockets.text = "Отправлено $successCount, осталось ${failedData.size}"
                        updateOfflineCount()
                        Toast.makeText(this, "Отправлено $successCount из ${offlineData.size}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(logTag, "Error: ${e.message}")
            }
        }.start()
    }

    private fun sendSingleOfflineData(data: String): Boolean {
        return try {
            val context = ZContext()
            val socket = context.createSocket(SocketType.REQ)

            socket.setReceiveTimeOut(5000)
            socket.setSendTimeOut(5000)
            socket.connect(serverAddress)
            socket.send(data.toByteArray(ZMQ.CHARSET), 0)

            val success = socket.recv(0) != null

            socket.close()
            context.close()
            success
        } catch (e: Exception) {
            false
        }
    }

    private fun updateOfflineCount() {
        try {
            val file = File(filesDir, offlineFileName)
            if (file.exists()) {
                var count = 0
                BufferedReader(FileReader(file)).use { reader ->
                    reader.forEachLine { line ->
                        if (line.isNotBlank()) count++
                    }
                }
                tvOfflineCount.text = "Оффлайн: $count"
            } else {
                tvOfflineCount.text = "Оффлайн: 0"
            }
        } catch (e: Exception) {
            tvOfflineCount.text = "Оффлайн: ошибка"
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermissions()) {
            startLocationUpdates()
        }
        updateNetworkParameters()
    }

    override fun onPause() {
        super.onPause()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isSendingActive = false
        scheduler.shutdown()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
        Log.d(logTag, "App destroyed")
    }
}