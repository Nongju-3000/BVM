package com.credo.bvm;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class BLEService extends Service {

    private static final String TAG = "BLEService";

    private BVMDataUpdateListener mBVMDataUpdateListener;
    private CPRBANDDataUpdateListener mCPRBANDDataUpdateListener;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt1, bluetoothGatt2;
    private ScanCallback scanCallback;

    private final IBinder mBinder = new BLEServiceBinder();


    public class BLEServiceBinder extends android.os.Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate: BleService" );
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startBLEscan() {
        Log.e(TAG, "startBLEscan: ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null) {
                    Log.e(TAG, "onScanResult: " + device.getName() + " " + device.getAddress());
                    if (device.getName().contains("BVM")) {
                        if (mBVMDataUpdateListener != null) {
                            onBVMDataReceived(device);
                        }
                    } else if (device.getName().equals("CPR-BAND")) {
                        if (mCPRBANDDataUpdateListener != null) {
                            Log.e(TAG, "onCPRScanResult: " + device.getName() + " " + device.getAddress());
                            // mCPRBANDDataUpdateListener.onCPRBANDDataUpdated(device);
                            onCPRBANDDataReceived(device);
                        }
                    }
                }
            }
        };
        bluetoothLeScanner.startScan(scanCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public interface CPRBANDDataUpdateListener {
        void onCPRBANDDataUpdated(String data, String label);
        void onCPRBANDDataUpdated(BluetoothDevice device);
    }

    public interface BVMDataUpdateListener {
        void onBVMDataUpdated(String data, String label);
        void onBVMDataUpdated(BluetoothDevice device);
    }

    public void setBVMDataUpdateListener(BVMDataUpdateListener listener) {
        this.mBVMDataUpdateListener = listener;

    }

    private void onBVMDataReceived(String data, String label) {
        if (mBVMDataUpdateListener != null) {
            mBVMDataUpdateListener.onBVMDataUpdated(data, label);
        }
    }

    @SuppressLint("MissingPermission")
    private void onBVMDataReceived(BluetoothDevice device) {
        if (mBVMDataUpdateListener != null) {
            mBVMDataUpdateListener.onBVMDataUpdated(device);
            Log.e(TAG, "onBVMDataReceived: " + device.getName() + " " + device.getAddress());
        }
    }

    public void setCPRBANDDataUpdateListener(CPRBANDDataUpdateListener listener) {
        this.mCPRBANDDataUpdateListener = listener;
    }

    private void onCPRBANDDataReceived(String data, String label) {
        if (mCPRBANDDataUpdateListener != null) {
            mCPRBANDDataUpdateListener.onCPRBANDDataUpdated(data, label);
        }
    }

    private void onCPRBANDDataReceived(BluetoothDevice device) {
        if (mCPRBANDDataUpdateListener != null) {
            Log.e(TAG, "onCPRBANDDataReceived: " + device.getName() + " " + device.getAddress());
            mCPRBANDDataUpdateListener.onCPRBANDDataUpdated(device);
        }
    }

}