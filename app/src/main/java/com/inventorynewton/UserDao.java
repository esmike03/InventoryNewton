package com.inventorynewton;

import android.content.Context;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class UserDao {

    DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean login(String username, String password) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM users WHERE username=? AND password=?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }
    public boolean register(String username, String password) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        long result = db.insert("users", null, values);

        return result != -1;
    }
}