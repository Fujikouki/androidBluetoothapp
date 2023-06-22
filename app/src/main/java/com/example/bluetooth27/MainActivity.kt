package com.example.bluetooth27

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLEBLUETOOTH: Int = 1
    private val MY_REQUEST_CODE: Int = 2

    private var bluetoothAdapter: BluetoothAdapter? = null



    private var gpsEnabled: Boolean = false



    var MacAddressSet = mutableSetOf<String?>()

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {

                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device == null) {
                        Log.d("nullDevice", "Device is null")
                        return
                    }


                    val deviceHardwareAddress = device?.address // MAC address

                    MacAddressSet.add(deviceHardwareAddress)
                    Log.d("MacA", MacAddressSet.toString())
                    return
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            finish()
            return
            // Device doesn't support Bluetooth
        }

        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        val listener = ScanListener()

        var scanButton = findViewById<Button>(R.id.ScanButton)
        scanButton.setOnClickListener(listener)

        var cancelButton = findViewById<Button>(R.id.CancelButton)
        cancelButton.setOnClickListener(listener)

        var canseeButton = findViewById<Button>(R.id.canSeeButton)
        canseeButton.setOnClickListener(listener)


    }

    private inner class ScanListener : View.OnClickListener{
        override fun onClick(view: View) {

            when(view.id){
                R.id.ScanButton -> {
                    bluetoothAdapter?.startDiscovery()

                }
                R.id.CancelButton ->{
                    bluetoothAdapter?.cancelDiscovery()
                    MacAddressSet = mutableSetOf<String?>()
                }
                R.id.canSeeButton ->{
                    val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                    startActivity(discoverableIntent)

                }
            }



        }
    }
    private fun requestLocationFeature() {
        if (gpsEnabled) {
            return
        }
        //startActivityForResult(Intent(LocationManager.PROVIDERS_CHANGED_ACTION), MY_REQUEST_CODE)
        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), MY_REQUEST_CODE)
    }

    private  fun requestBluetoothFeature(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLEBLUETOOTH)
        }
    }
    override fun onResume() {
        super.onResume()
        requestLocationFeature()
        requestBluetoothFeature()
    }
    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
        bluetoothAdapter?.cancelDiscovery()
    }
}