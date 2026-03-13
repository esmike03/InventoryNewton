package com.inventorynewton;

import android.Manifest;
import android.app.AlertDialog;
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
import android.view.inputmethod.InputMethodManager;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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


        assetList = new ArrayList<>(assetDao.getAllAssets());
        adapter = new AssetAdapter(requireContext(), assetList);



        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        Button btnExport = view.findViewById(R.id.btnExport);

        btnExport.setOnClickListener(v -> exportAssetsToCSV());

        etSearch.post(() -> {
            etSearch.requestFocus();

            // Optional: show keyboard (not required for Zebra scanner)
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        // 🔍 Search Listener
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String scannedText = etSearch.getText().toString().trim();

            etSearch.requestFocus();
            // Optional: show soft keyboard
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
            if (!scannedText.isEmpty()) {
                // Filter adapter with the scanned barcode
                adapter.filter(scannedText);

                // Clear the EditText for the next scan
                etSearch.setText("");
            }

            return true; // consume the action
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

            shareFile(file);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void shareFile(File csvFile) {
        String[] formats = {"CSV", "Excel"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose export format")
                .setItems(formats, (dialog, which) -> {
                    File fileToShare;
                    String mimeType;

                    if (which == 0) { // CSV
                        fileToShare = csvFile;
                        mimeType = "text/csv";
                    } else { // Excel
                        try {
                            fileToShare = convertCSVToExcel(csvFile);
                            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to create Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    Uri uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".provider",
                            fileToShare
                    );

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(mimeType);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(intent, "Export Asset Report"));
                })
                .show();
    }

    private File convertCSVToExcel(File csvFile) throws Exception {
        File excelFile = new File(csvFile.getParent(), csvFile.getName().replace(".csv", ".xlsx"));
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Assets");

        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line;
        int rowNum = 0;
        while ((line = br.readLine()) != null) {
            String[] columns = line.split(",");
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < columns.length; i++) {
                row.createCell(i).setCellValue(columns[i]);
            }
        }
        br.close();

        FileOutputStream fos = new FileOutputStream(excelFile);
        workbook.write(fos);
        workbook.close();
        fos.close();

        return excelFile;
    }
}