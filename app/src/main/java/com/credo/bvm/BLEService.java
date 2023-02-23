package com.credo.bvm;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BLEService extends Service {

    private static final String TAG = "BLEService";

    private BVMDataUpdateListener mBVMDataUpdateListener;
    private CPRBANDDataUpdateListener mCPRBANDDataUpdateListener;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private BluetoothGatt band_bluetoothGatt, bvm_bluetoothGatt;
    private int breath_count = 0;

    private static final UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID DEPTH_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static final UUID ANGLE_UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private static final UUID WRITE_UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");

    private final IBinder mBinder = new BLEServiceBinder();


    public class BLEServiceBinder extends android.os.Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBLEscan();
    }

    public void startBLEscan() {
        stopBLEscan();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

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
                    if (device.getName().contains("BVM")) {
                        if (mBVMDataUpdateListener != null) {
                            onBVMDataReceived(device);
                        }
                    } else if (device.getName().equals("CPR-BAND")) {
                        if (mCPRBANDDataUpdateListener != null) {
                            // mCPRBANDDataUpdateListener.onCPRBANDDataUpdated(device);
                            onCPRBANDDataReceived(device);
                        }
                    }
                }
            }
        };
        bluetoothLeScanner.startScan(scanCallback);
    }

    public void stopBLEscan() {
        Log.e(TAG, "stopBLEscan: ");
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    public void connectBAND(BluetoothDevice device) {
        Log.e(TAG, "connectBVM: " + device.getName() + " " + device.getAddress());
        band_bluetoothGatt = device.connectGatt(this, false, band_gattCallback);
    }

    private final BluetoothGattCallback band_gattCallback = new BluetoothGattCallback() {
        List<BluetoothGattCharacteristic> chars = new ArrayList<>();
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                onCPRBANDDataReceived("Connected", "Connection");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e(TAG, "onServicesDiscovered: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                BluetoothGattService mBluetoothGattService = gatt.getService(SERVICE_UUID);
                chars.add(mBluetoothGattService.getCharacteristic(DEPTH_UUID));
                chars.add(mBluetoothGattService.getCharacteristic(ANGLE_UUID));
                subscribeToCharacteristics(gatt);
                if (mBluetoothGattService != null) {
                    Log.i(TAG, "Service characteristic UUID found: " + mBluetoothGattService.getUuid().toString());
                    onCPRBANDDataReceived("GATT_SUCCESS", "Connection");
                } else {
                    Log.i(TAG, "Service characteristic not found for UUID: " + SERVICE_UUID);
                }
            }
        }

        private void subscribeToCharacteristics(BluetoothGatt gatt){
            if(chars.size() == 0) return;
            BluetoothGattCharacteristic characteristic = chars.get(0);
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            if(descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            chars.remove(0);
            subscribeToCharacteristics(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, android.bluetooth.BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(characteristic.getUuid().equals(DEPTH_UUID)){
                String received = HexString.bytesToHex(characteristic.getValue());
                int data = Integer.parseInt(received, 16);
                received = String.valueOf(data);
                onCPRBANDDataReceived(received, "Depth");
            }
            else if(characteristic.getUuid().equals(ANGLE_UUID)){
                if(breath_count >10) {
                    String received = HexString.bytesToHex(characteristic.getValue());
                    int data = Integer.parseInt(received, 16);
                    received = String.valueOf(data);
                    onCPRBANDDataReceived(received, "Angle");
                    breath_count = 0;
                }
                breath_count++;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public void writeCharacteristic(String data){
        byte[] sender = HexString.hexToBytes(data);
        BluetoothGattService mSVC = band_bluetoothGatt.getService(SERVICE_UUID);
        BluetoothGattCharacteristic mCH = mSVC.getCharacteristic(WRITE_UUID);
        Log.d("sender", String.valueOf(sender));
        mCH.setValue(sender);
        band_bluetoothGatt.writeCharacteristic(mCH);
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


    private void onBVMDataReceived(BluetoothDevice device) {
        if (mBVMDataUpdateListener != null) {
            mBVMDataUpdateListener.onBVMDataUpdated(device);
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
            mCPRBANDDataUpdateListener.onCPRBANDDataUpdated(device);
        }
    }

}