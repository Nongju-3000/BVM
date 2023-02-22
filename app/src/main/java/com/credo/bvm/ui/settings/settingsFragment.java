package com.credo.bvm.ui.settings;

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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.credo.bvm.BLEService;
import com.credo.bvm.BVM_Adapter;
import com.credo.bvm.CPRBAND_Adapter;
import com.credo.bvm.databinding.FragmentSettingBinding;

public class settingsFragment extends Fragment{

    private BLEService mBLEService;
    private FragmentSettingBinding binding;

    private BluetoothDevice BVM_device, CPRBAND_device;
    private BVM_Adapter bvm_adapter;
    private CPRBAND_Adapter cprband_adapter;


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
                    BVM_device = device;
                    bvm_adapter.addBVM_Data(device);
                }
            });
            mBLEService.setCPRBANDDataUpdateListener(new BLEService.CPRBANDDataUpdateListener() {
                @Override
                public void onCPRBANDDataUpdated(String data, String label) {

                }

                @Override
                public void onCPRBANDDataUpdated(BluetoothDevice device) {
                    CPRBAND_device = device;
                    Log.e("CPRBAND_device", "onServiceConnected: " + device.getName());
                    cprband_adapter.addCPRBAND_Data(device);
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
        settingsViewModel settingsViewModel = new ViewModelProvider(this).get(settingsViewModel.class);


        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Intent intent = new Intent(getActivity(), BLEService.class);
        getActivity().bindService(intent, serviceConnection, getActivity().BIND_AUTO_CREATE);

        bvm_adapter = new BVM_Adapter();
        cprband_adapter = new CPRBAND_Adapter();

        RecyclerView bvm_recyclerview = binding.bvmRecyclerview;
        RecyclerView cprband_recyclerview = binding.cprRecyclerview;
        TextView bvm_status = binding.bvmStatus;
        TextView cprband_status = binding.cprbandStatus;
        Button search_btn = binding.searchBtn;
        Button connect_btn = binding.connectBtn;

        bvm_recyclerview.setAdapter(bvm_adapter);
        cprband_recyclerview.setAdapter(cprband_adapter);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBLEService != null){
                    bvm_adapter.clearBVM_Data();
                    cprband_adapter.clearCPRBAND_Data();
                    mBLEService.startBLEscan();
                }
            }
        });

        /*connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBLEService != null){
                    bvm_adapter.clearBVM_Data();
                    cprband_adapter.clearCPRBAND_Data();
                    mBLEService.connectBVM(BVM_device);
                    mBLEService.connectCPRBAND(CPRBAND_device);
                }
            }
        });*/

        bvm_adapter.setOnItemClickListener(new BVM_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                BVM_device = bvm_adapter.getItem(pos);
                bvm_status.setText(BVM_device.getAddress());
            }
        });

        cprband_adapter.setOnItemClickListener(new CPRBAND_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                CPRBAND_device = cprband_adapter.getItem(pos);
                cprband_status.setText(CPRBAND_device.getAddress());
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}