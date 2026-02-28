package com.inventorynewton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class HomeFragment extends Fragment {

    private static final int PICK_FILE = 1;
    ArrayList<Asset> previewList = new ArrayList<>();
    AssetPreviewAdapter adapter;
    RecyclerView recyclerView;
    Button importBtn;
    TextView  txtResults;
    private static final int PICK_CSV_FILE = 100;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Button addAssetButton = rootView.findViewById(R.id.button3);
        EditText etNumber = rootView.findViewById(R.id.editTextName);
        EditText etDescription = rootView.findViewById(R.id.editTextCategory);
        EditText etLocation = rootView.findViewById(R.id.editTextLocation);
        EditText etOwner = rootView.findViewById(R.id.editTextOwner);
        RadioGroup statusGroup = rootView.findViewById(R.id.radioGroupStatus);
        txtResults = rootView.findViewById(R.id.textView10);
        importBtn = rootView.findViewById(R.id.button5);
        importBtn.setEnabled(false);
        importBtn.setOnClickListener(v -> importData());

        importBtn.setOnClickListener(v -> importData());

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AssetPreviewAdapter(previewList);
        recyclerView.setAdapter(adapter);

        Button chooseCSV = rootView.findViewById(R.id.button4);

        chooseCSV.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*");
            startActivityForResult(intent, PICK_CSV_FILE);
        });
        AssetDao assetDao = new AssetDao(requireContext());

        addAssetButton.setOnClickListener(v -> {
            String asset_number = etNumber.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String remarks = etOwner.getText().toString().trim();

            if (asset_number.isEmpty() || description.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (assetDao.isAssetExist(asset_number)) {
                Toast.makeText(requireContext(),
                        "Asset number already exists!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            int selectedId = statusGroup.getCheckedRadioButtonId();
            String validate = selectedId == R.id.radioButtonFound ? "Found" : "Not Found";

            boolean inserted = assetDao.addAsset(asset_number, description, location, remarks, validate);

            if (inserted) {
                Toast.makeText(requireContext(), "Asset added successfully!", Toast.LENGTH_SHORT).show();
                // Optionally clear fields after adding
                etNumber.setText("");
                etDescription.setText("");
                etLocation.setText("");
                etOwner.setText("");
                statusGroup.clearCheck();
            } else {
                Toast.makeText(requireContext(), "Failed to add asset.", Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            readCSV(uri);
        }
    }


    private void importData() {

        AssetDao dao = new AssetDao(requireContext());

        int total = previewList.size();
        int success = 0;
        int skipped = 0;

        for (Asset a : previewList) {

            if (!a.isValid) {
                skipped++;
                continue;
            }

            // skip duplicates in database
            if (dao.isAssetExist(a.assetNumber)) {
                skipped++;
                continue;
            }

            boolean inserted = dao.addAsset(
                    a.assetNumber,
                    a.description,
                    a.location,
                    a.remarks,
                    a.validate
            );

            if (inserted) success++;
            else skipped++;
        }

        txtResults.setText("Total: " + total +
                "\nImported: " + success +
                "\nSkipped: " + skipped);
        Toast.makeText(requireContext(),
                "Skp: " + skipped +
                        "\nI: " + success +
                        "\nS: " + skipped,
                Toast.LENGTH_LONG).show();
        previewList.clear();
        adapter.notifyDataSetChanged();
        importBtn.setEnabled(false);
    }
    private void readCSV(Uri uri) {
        previewList.clear();

        try {
            InputStream input = requireContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            importBtn = importBtn.findViewById(R.id.button5);
            adapter.notifyDataSetChanged();


                importBtn.setEnabled(true);

            String line;
            boolean first = true;
            HashSet<String> duplicates = new HashSet<>();

            while ((line = reader.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                String[] t = line.split(",");

                Asset a = new Asset(
                        t[0], t[1], t[2], t[3], t[4]
                );

                // validation
                if (a.assetNumber.isEmpty() || a.description.isEmpty()) {
                    a.isValid = false;
                    a.error = "Required fields missing";
                } else if (duplicates.contains(a.assetNumber)) {
                    a.isValid = false;
                    a.error = "Duplicate in CSV";
                } else {
                    a.isValid = true;
                    duplicates.add(a.assetNumber);
                }

                previewList.add(a);
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
