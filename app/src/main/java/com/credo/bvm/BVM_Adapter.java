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

public class BVM_Adapter extends RecyclerView.Adapter<BVM_Adapter.ViewHolder> {

    private ArrayList<BluetoothDevice> bvm_decive = new ArrayList<>();

    @NonNull
    @Override
    public BVM_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bvm, parent, false);

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
    public void onBindViewHolder(@NonNull BVM_Adapter.ViewHolder holder, int position) {
        BluetoothDevice bvm_data = bvm_decive.get(position);

        holder.setItem(bvm_data);
    }

    @Override
    public int getItemCount() {
        return bvm_decive.size();
    }

    public void setBVM_Data(ArrayList<BluetoothDevice> bvm_data) {
        bvm_decive = bvm_data;
        notifyDataSetChanged();
    }

    public void addBVM_Data(BluetoothDevice bvm_data) {
        bvm_decive.add(bvm_data);
        notifyDataSetChanged();
    }

    public void clearBVM_Data() {
        bvm_decive.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout bvm_container;
        TextView name;
        TextView address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bvm_container = itemView.findViewById(R.id.bvm_container);
            name = itemView.findViewById(R.id.bvm_name);
            address = itemView.findViewById(R.id.bvm_address);

            bvm_container.setOnClickListener(new View.OnClickListener() {
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
        public void setItem(BluetoothDevice bvm_data){
            name.setText(bvm_data.getName());
            address.setText(bvm_data.getAddress());
        }
    }

    public BluetoothDevice getItem(int pos){
        return bvm_decive.get(pos);
    }
}
