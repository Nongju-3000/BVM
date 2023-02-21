package com.credo.bvm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BLEService extends Service {
    private BVMDataUpdateListener BVMDataUpdateListener;
    private CPRBANDDataUpdateListener CPRBANDDataUpdateListener;

    public BLEService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public interface CPRBANDDataUpdateListener {
        void onCPRBANDDataUpdated(String data, String label);
    }

    public interface BVMDataUpdateListener {
        void onBVMDataUpdated(String data, String label);
    }

    public void setBVMDataUpdateListener(BVMDataUpdateListener listener) {
        this.BVMDataUpdateListener = listener;
    }

    public void setCPRBANDDataUpdateListener(CPRBANDDataUpdateListener listener) {
        this.CPRBANDDataUpdateListener = listener;
    }

    private void onBVMDataReceived(String data, String label) {
        if (BVMDataUpdateListener != null) {
            BVMDataUpdateListener.onBVMDataUpdated(data, label);
        }
    }

    private void onCPRBANDDataReceived(String data, String label) {
        if (CPRBANDDataUpdateListener != null) {
            CPRBANDDataUpdateListener.onCPRBANDDataUpdated(data, label);
        }
    }
}