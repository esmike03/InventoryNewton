package com.inventorynewton;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
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

    private final Context context;
    private final List<Asset> assetList;
    private final List<Asset> fullList;

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
            tvLocation    = itemView.findViewById(R.id.tvLocation);
            tvRemarks     = itemView.findViewById(R.id.tvRemarks);
            tvValidate    = itemView.findViewById(R.id.tvValidate);   // status badge
            cbValidate    = itemView.findViewById(R.id.cbValidate);
            cbCreated     = itemView.findViewById(R.id.cbCreated);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
            btnEdit       = itemView.findViewById(R.id.btnEdit);
        }
    }

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

        String created = formatDate(asset.created_at);
        String updated = formatDate(asset.updated_at);

        holder.tvAssetNumber.setText("Asset #: " + nullSafe(asset.assetNumber));
        holder.tvDescription.setText("Description: " + nullSafe(asset.description));
        holder.tvLocation.setText("Location: " + nullSafe(asset.location));
        holder.tvRemarks.setText("Remarks: " + nullSafe(asset.remarks));
        holder.cbCreated.setText("Created: " + created + "  Updated: " + updated);

        // Validate badge — shows "Found" or "Not Found" with colored background
        boolean isFound = "Found".equalsIgnoreCase(asset.validate);
        holder.cbValidate.setOnCheckedChangeListener(null);
        holder.cbValidate.setChecked(isFound);
        applyBadge(holder.tvValidate, isFound);

        holder.cbValidate.setOnCheckedChangeListener((buttonView, checked) -> {
            asset.validate = checked ? "Found" : "Not Found";
            applyBadge(holder.tvValidate, checked);
            AssetDao dao = new AssetDao(context);
            dao.updateAsset(asset);
        });

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAssetActivity.class);
            intent.putExtra("asset_number", asset.assetNumber);
            context.startActivity(intent);
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

                        int currentPos = holder.getAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return;

                        assetList.remove(currentPos);
                        fullList.remove(toDelete);
                        notifyItemRemoved(currentPos);
                        notifyItemRangeChanged(currentPos, assetList.size());

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

    public void filter(String query) {
        assetList.clear();
        if (query == null || query.trim().isEmpty()) {
            assetList.addAll(fullList);
        } else {
            String q = query.toLowerCase();
            for (Asset asset : fullList) {
                boolean matchNumber = asset.assetNumber != null && asset.assetNumber.toLowerCase().contains(q);
                boolean matchDesc   = asset.description != null && asset.description.toLowerCase().contains(q);
                if (matchNumber || matchDesc) assetList.add(asset);
            }
        }
        notifyDataSetChanged();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "N/A";
        try {
            SimpleDateFormat dbFormat      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            return displayFormat.format(dbFormat.parse(raw));
        } catch (Exception e) {
            return raw;
        }
    }

    /** Updates tvValidate badge text + rounded background color */
    private void applyBadge(TextView badge, boolean isFound) {
        badge.setText(isFound ? "Found" : "Not Found");
        badge.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        // Mutate the drawable so each badge gets its own color instance
        GradientDrawable bg = (GradientDrawable) ContextCompat.getDrawable(
                context, R.drawable.badge_background).mutate();
        bg.setColor(ContextCompat.getColor(context,
                isFound ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        badge.setBackground(bg);
    }
}