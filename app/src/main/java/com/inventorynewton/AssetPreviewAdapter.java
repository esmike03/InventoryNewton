package com.inventorynewton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AssetPreviewAdapter extends RecyclerView.Adapter<AssetPreviewAdapter.ViewHolder> {

    private ArrayList<Asset> list;

    public AssetPreviewAdapter(ArrayList<Asset> list) {
        this.list = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvValidate, tvError, tvDescription, tvLocation, tvRemarks;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            tvValidate = itemView.findViewById(R.id.tvValidate);
            tvError = itemView.findViewById(R.id.tvError);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset_preview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        Asset a = list.get(i);

        h.tvNumber.setText(a.assetNumber);
        h.tvDescription.setText(a.description);
        h.tvLocation.setText(a.location);
        h.tvRemarks.setText(a.remarks);
        h.tvValidate.setText(a.validate);
        if (!a.isValid) {
            h.tvError.setVisibility(View.VISIBLE);
            h.tvError.setText(a.error);
        } else {
            h.tvError.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}