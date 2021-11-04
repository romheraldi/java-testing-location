package com.romheraldi.testinglocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private List<Data> dataList;
    private String dataIdStore;
    private String dataLatLong;

    private ActionData listener;

    interface ActionData {
        public void getLatLong(Double latitude, Double longitude);
    }

    public void setListener(ActionData mListener) {
        listener = mListener;
    }

    public Adapter(List<Data> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
        String nameStore = dataList.get(position).nameStore;
        String latLong = dataList.get(position).latLong;
        String dataId = dataList.get(position).dataId;

        holder.setData(nameStore, latLong, dataId);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView locationName;
        private TextView locationLatLong;
        private Button checkButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            locationName = itemView.findViewById(R.id.locationName);
            locationLatLong = itemView.findViewById(R.id.locationLatLong);
            checkButton = itemView.findViewById(R.id.checkButton);

        }

        public void setData(String nameStore, String latLong, String dataId) {
            locationName.setText(nameStore);
            locationLatLong.setText(latLong);

            checkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String[] dataCoordinate = latLong.split(",");
//
                    listener.getLatLong(Double.parseDouble(dataCoordinate[0]), Double.parseDouble(dataCoordinate[1]));
                }
            });
        }
    }
}
