package com.inventorynewton;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.AssetViewHolder> {

    private Context context;
    private List<Asset> assetList;
    private List<Asset> fullList;

    public AssetAdapter(Context context, List<Asset> assetList) {
        this.context = context;
        this.assetList = assetList;
        this.fullList = new ArrayList<>(assetList);
    }


    static class AssetViewHolder extends RecyclerView.ViewHolder {
        TextView tvAssetNumber, tvDescription, tvLocation, tvRemarks, tvValidate, cbCreated;
        Button btnDelete, btnEdit;
        CheckBox cbValidate;

        AssetViewHolder(View itemView) {
            super(itemView);
            tvAssetNumber = itemView.findViewById(R.id.tvAssetNumber);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            tvValidate = itemView.findViewById(R.id.tvValidate);
            cbValidate = itemView.findViewById(R.id.cbValidate);
            cbCreated = itemView.findViewById(R.id.cbCreated);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    // ✅ REQUIRED METHOD
    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        Asset asset = assetList.get(position);
        // Parse DB timestamp and format nicely
        String created = asset.created_at;
        String updated = asset.updated_at;

// Optional: format to dd-MM-yyyy HH:mm:ss
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

            if (created != null) {
                created = displayFormat.format(dbFormat.parse(created));
            }
            if (updated != null) {
                updated = displayFormat.format(dbFormat.parse(updated));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.tvAssetNumber.setText("Asset #: " + asset.assetNumber);
        holder.tvDescription.setText("Description: " + asset.description);
        holder.tvLocation.setText("Location: " + asset.location);
        holder.tvRemarks.setText("Validate: " + asset.validate);
        holder.tvValidate.setText("Remarks: " + asset.remarks);
        holder.cbCreated.setText("Created: " + created + " Updated: " + updated);

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAssetActivity.class);
            intent.putExtra("asset_number", asset.assetNumber);
            context.startActivity(intent);
        });
        boolean isFound = "Found".equalsIgnoreCase(asset.validate);
        holder.cbValidate.setOnCheckedChangeListener(null);
        holder.cbValidate.setChecked(isFound);

        holder.tvRemarks.setBackgroundColor(
                ContextCompat.getColor(
                        context,
                        isFound ? android.R.color.holo_green_light
                                : android.R.color.holo_red_light
                )
        );
        holder.tvRemarks.setTextColor(
                ContextCompat.getColor(context, android.R.color.white)
        );


        holder.cbValidate.setOnCheckedChangeListener((buttonView, checked) -> {
            asset.validate = checked ? "Found" : "Not Found";
            holder.tvRemarks.setText("Validate: " + asset.validate);

            holder.tvRemarks.setBackgroundColor(
                    ContextCompat.getColor(
                            context,
                            checked ? android.R.color.holo_green_light
                                    : android.R.color.holo_red_light
                    )
            );

            AssetDao dao = new AssetDao(context);
            dao.updateAsset(asset);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Asset toDelete = assetList.get(pos);

            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Delete Asset")
                    .setMessage("Are you sure you want to delete asset " + toDelete.assetNumber + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        DatabaseHelper db = new DatabaseHelper(context);
                        db.deleteAsset(toDelete.assetNumber);


                        assetList.remove(pos);
                        fullList.remove(toDelete);


                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, assetList.size());

                        Toast.makeText(context, "Asset deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    // 🔍 Search
    public void filter(String query) {
        assetList.clear();

        if (query == null || query.trim().isEmpty()) {
            assetList.addAll(fullList);
        } else {
            query = query.toLowerCase();
            for (Asset asset : fullList) {
                if (asset.assetNumber.toLowerCase().contains(query) ||
                        asset.description.toLowerCase().contains(query)) {
                    assetList.add(asset);
                }
            }
        }
        notifyDataSetChanged();
    }
}