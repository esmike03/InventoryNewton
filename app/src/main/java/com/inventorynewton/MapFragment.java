package com.inventorynewton;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {

    private AssetAdapter adapter;
    private AssetDao assetDao;

    private List<Asset> assetList;
    public MapFragment() {
        super(R.layout.fragment_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAssets);
        EditText etSearch = view.findViewById(R.id.etSearch);
        assetDao = new AssetDao(requireContext());
//        List<Asset> assetList = new ArrayList<>(assetDao.getAllAssets());

        assetList = new ArrayList<>(assetDao.getAllAssets());
        adapter = new AssetAdapter(requireContext(), assetList);



        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        Button btnExport = view.findViewById(R.id.btnExport);

        btnExport.setOnClickListener(v -> exportAssetsToCSV());

        // 🔍 Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        if (assetList == null) {
            assetList = new ArrayList<>();
        }

        if (assetDao == null) {
            assetDao = new AssetDao(requireContext());
        }


        assetList.clear();
        assetList.addAll(assetDao.getAllAssets());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    private void exportAssetsToCSV() {
        try {
            AssetDao dao = new AssetDao(requireContext());
            List<Asset> assets = dao.getAllAssets();

            if (assets.isEmpty()) {
                Toast.makeText(getContext(), "No assets to export", Toast.LENGTH_SHORT).show();
                return;
            }

            // 📅 File name
            String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            String fileName = "asset_report_" + date + ".csv";

            File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(dir, fileName);

            FileWriter writer = new FileWriter(file);

            // CSV Header
            writer.append("asset_number,description,location,remarks,validate\n");

            // CSV Rows
            for (Asset asset : assets) {
                writer.append(asset.assetNumber).append(",")
                        .append(asset.description).append(",")
                        .append(asset.location).append(",")
                        .append(asset.remarks).append(",")
                        .append(asset.validate).append("\n");
            }

            writer.flush();
            writer.close();

            shareCSV(file);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void shareCSV(File file) {
        Uri uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Export Asset Report"));
    }
}