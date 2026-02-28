package com.inventorynewton;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssetDao {

    private DatabaseHelper dbHelper;

    public AssetDao(Context context){
        dbHelper = new DatabaseHelper(context);
    }

    public boolean addAsset(String asset_number, String description, String location, String remarks, String validate){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("asset_number", asset_number);
        values.put("description", description);
        values.put("location", location);
        values.put("remarks", remarks);
        values.put("validate", validate);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put("created_at", timestamp);
        values.put("updated_at", timestamp);

        long id = db.insert("assets", null, values);
        db.close();
        return id != -1;
    }

    public boolean isAssetExist(String number) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id FROM assets WHERE asset_number=?",
                new String[]{number}
        );
        boolean exist = c.moveToFirst();
        c.close();
        return exist;
    }

    public List<Asset> getAllAssets() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM assets ORDER BY id DESC", null);

        List<Asset> list = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                String assetNumber = c.getString(c.getColumnIndexOrThrow("asset_number"));
                String description = c.getString(c.getColumnIndexOrThrow("description"));
                String location = c.getString(c.getColumnIndexOrThrow("location"));
                String remarks = c.getString(c.getColumnIndexOrThrow("remarks"));
                String validate = c.getString(c.getColumnIndexOrThrow("validate"));
                String createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
                String updatedAt = c.getString(c.getColumnIndexOrThrow("updated_at"));

                Asset asset = new Asset(assetNumber, description, location, remarks, validate);
                asset.created_at = createdAt;
                asset.updated_at = updatedAt;

                list.add(asset);
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

    public Asset getAssetByNumber(String assetNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        Cursor c = db.rawQuery(
                "SELECT asset_number, description, location, remarks, validate, created_at, updated_at FROM assets WHERE asset_number = ?",
                new String[]{assetNumber}
        );

        Asset asset = null;

        if (c.moveToFirst()) {
            asset = new Asset(
                    c.getString(c.getColumnIndexOrThrow("asset_number")),
                    c.getString(c.getColumnIndexOrThrow("description")),
                    c.getString(c.getColumnIndexOrThrow("location")),
                    c.getString(c.getColumnIndexOrThrow("remarks")),
                    c.getString(c.getColumnIndexOrThrow("validate"))
            );
            asset.created_at = c.getString(c.getColumnIndexOrThrow("created_at"));
            asset.updated_at = c.getString(c.getColumnIndexOrThrow("updated_at"));
        }

        c.close();
        db.close();
        return asset;
    }

    public boolean updateAsset(Asset asset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("description", asset.description);
        values.put("location", asset.location);
        values.put("remarks", asset.remarks);
        values.put("validate", asset.validate);


        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put("updated_at", timestamp);

        int rows = db.update(
                "assets",
                values,
                "asset_number = ?",
                new String[]{asset.assetNumber}
        );

        db.close();
        return rows > 0;
    }
}