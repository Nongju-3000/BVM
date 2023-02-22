package com.credo.bvm.ui.CPR;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.credo.bvm.BLEService;
import com.credo.bvm.databinding.FragmentCprBinding;

public class CPRFragment extends Fragment implements BLEService.BVMDataUpdateListener, BLEService.CPRBANDDataUpdateListener{

    private FragmentCprBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CPRViewModel CPRViewModel = new ViewModelProvider(this).get(CPRViewModel.class);

        binding = FragmentCprBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onCPRBANDDataUpdated(String data, String label) {

    }

    @Override
    public void onCPRBANDDataUpdated(BluetoothDevice device) {

    }

    @Override
    public void onBVMDataUpdated(String data, String label) {

    }

    @Override
    public void onBVMDataUpdated(BluetoothDevice device) {

    }
}