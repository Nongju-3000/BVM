package com.credo.bvm.ui.CPR;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.credo.bvm.BLEService;
import com.credo.bvm.R;
import com.credo.bvm.databinding.FragmentCprBinding;

public class CPRFragment extends Fragment {

    private FragmentCprBinding binding;


    private BLEService mBLEService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BLEService.BLEServiceBinder binder = (BLEService.BLEServiceBinder) iBinder;
            mBLEService = binder.getService();
            mBLEService.setBVMDataUpdateListener(new BLEService.BVMDataUpdateListener() {
                @Override
                public void onBVMDataUpdated(String data, String label) {

                }

                @Override
                public void onBVMDataUpdated(BluetoothDevice device) {

                }
            });
            mBLEService.setCPRBANDDataUpdateListener(new BLEService.CPRBANDDataUpdateListener() {
                @Override
                public void onCPRBANDDataUpdated(String data, String label) {
                    if(label == "Angle") {
                        int angle = Integer.parseInt(data);
                        if(angle <= 30)
                            binding.angle.setImageResource(R.drawable.angle_green);
                        else if(angle <= 60)
                            binding.angle.setImageResource(R.drawable.angle_orange);
                        else
                            binding.angle.setImageResource(R.drawable.angle_red);
                    }
                }

                @Override
                public void onCPRBANDDataUpdated(BluetoothDevice device) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBLEService = null;
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CPRViewModel CPRViewModel = new ViewModelProvider(this).get(CPRViewModel.class);

        binding = FragmentCprBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Intent intent = new Intent(getActivity(), BLEService.class);
        getActivity().bindService(intent, serviceConnection, getActivity().BIND_AUTO_CREATE);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}