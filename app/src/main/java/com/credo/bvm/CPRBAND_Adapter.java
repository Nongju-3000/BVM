package com.credo.bvm;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CPRBAND_Adapter extends RecyclerView.Adapter<CPRBAND_Adapter.ViewHolder> {

    private ArrayList<BluetoothDevice> mCPRBAND_device = new ArrayList<>();

    @NonNull
    @Override
    public CPRBAND_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cprband, parent, false);

        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    private static OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull CPRBAND_Adapter.ViewHolder holder, int position) {
        BluetoothDevice cprband_data = mCPRBAND_device.get(position);

        holder.setItem(cprband_data);
    }

    @Override
    public int getItemCount() {
        return mCPRBAND_device.size();
    }

    public void addCPRBAND_Data(BluetoothDevice cprband_data) {
        mCPRBAND_device.add(cprband_data);
        notifyDataSetChanged();
    }

    public void setCPRBAND_Data(ArrayList<BluetoothDevice> cprband_data) {
        mCPRBAND_device = cprband_data;
        notifyDataSetChanged();
    }

    public void clearCPRBAND_Data() {
        mCPRBAND_device.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout CPRBAND_container;
        TextView name;
        TextView address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            CPRBAND_container = itemView.findViewById(R.id.cprband_container);
            name = itemView.findViewById(R.id.cprband_name);
            address = itemView.findViewById(R.id.cprband_address);

            CPRBAND_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        if(onItemClickListener != null){
                            onItemClickListener.onItemClick(v, pos);
                        }
                    }
                }
            });
        }

        @SuppressLint("MissingPermission")
        public void setItem(BluetoothDevice CPRBAND_data){
            name.setText(CPRBAND_data.getName());
            address.setText(CPRBAND_data.getAddress());
        }
    }

    public BluetoothDevice getItem(int pos){
        return mCPRBAND_device.get(pos);
    }
}