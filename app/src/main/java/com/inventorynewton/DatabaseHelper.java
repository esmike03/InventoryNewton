package com.inventorynewton;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "inventory.db";
    public static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION); // use DB_VERSION here
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        String users = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL)";
        db.execSQL(users);

        // Assets table
        String assets = "CREATE TABLE IF NOT EXISTS assets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "asset_number TEXT UNIQUE," +
                "description TEXT," +
                "location TEXT," +
                "remarks TEXT," +
                "validate TEXT," +
                "created_at INTEGER," +
                "updated_at INTEGER)";
        db.execSQL(assets);
    }
    public void deleteAsset(String assetNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("assets", "asset_number = ?", new String[]{assetNumber});
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}