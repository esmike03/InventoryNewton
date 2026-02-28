package com.inventorynewton;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditAssetActivity extends AppCompatActivity {

    EditText etDescription, etLocation, etRemarks, etAssetNumber;
    CheckBox cbValidate;
    Button btnSave;

    AssetDao dao;
    Asset asset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_asset);

        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etAssetNumber = findViewById(R.id.etAssetNumber);
        etRemarks = findViewById(R.id.etRemarks);
        cbValidate = findViewById(R.id.cbValidate);
        btnSave = findViewById(R.id.btnSave);

        dao = new AssetDao(this);

        String assetNumber = getIntent().getStringExtra("asset_number");
        asset = dao.getAssetByNumber(assetNumber);

        // populate
        etAssetNumber.setText(asset.assetNumber);
        etDescription.setText(asset.description);
        etLocation.setText(asset.location);
        etRemarks.setText(asset.remarks);
        cbValidate.setChecked("Found".equals(asset.validate));

        btnSave.setOnClickListener(v -> saveAsset());
    }

    private void saveAsset() {
        asset.description = etDescription.getText().toString();
        asset.location = etLocation.getText().toString();
        asset.assetNumber = etAssetNumber.getText().toString();
        asset.remarks = etRemarks.getText().toString();
        asset.validate = cbValidate.isChecked() ? "Found" : "Not Found";

        dao.updateAsset(asset);
        finish();
    }
}