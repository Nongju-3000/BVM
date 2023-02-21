package com.credo.bvm.ui.BVM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.credo.bvm.databinding.FragmentBvmBinding;

public class BVMFragment extends Fragment {

    private FragmentBvmBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BVMViewModel BVMViewModel = new ViewModelProvider(this).get(BVMViewModel.class);

        binding = FragmentBvmBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}