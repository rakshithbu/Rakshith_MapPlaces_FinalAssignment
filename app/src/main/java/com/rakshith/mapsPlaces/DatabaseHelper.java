package com.rakshith.mapsPlaces;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "favourites";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Favourite.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Favourite.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertContact(Favourite contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Favourite.COLUMN_LATITUDE, contact.getLatitude());
        values.put(Favourite.COLUMN_LONGITUDE, contact.getLongitude());
        values.put(Favourite.COLUMN_ADDRESS,contact.getLocationAddress());

        System.out.println("insert into contact===>"+contact.toString());
        // insert row
        long id = db.insert(Favourite.TABLE_NAME, null, values);

        db.close();

        return id;
    }

    public Favourite getContact(String location) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Favourite.TABLE_NAME,
                new String[]{Favourite.COLUMN_ID, Favourite.COLUMN_LATITUDE,
                        Favourite.COLUMN_LONGITUDE, Favourite.COLUMN_ADDRESS},
                Favourite.COLUMN_ADDRESS + "=?",
                new String[]{location}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        Favourite contact = new Favourite();

        contact.setLatitude(cursor.getDouble(cursor.getColumnIndex(Favourite.COLUMN_LATITUDE)));
        contact.setLongitude(cursor.getDouble(cursor.getColumnIndex(Favourite.COLUMN_LONGITUDE)));
        contact.setId(cursor.getColumnIndex(Favourite.COLUMN_ID));

        // close the db connection
        cursor.close();

        return contact;
    }

    public ArrayList<Favourite> getAllContacts() {
        ArrayList<Favourite> contacts = new ArrayList<>();


        String selectQuery = "SELECT  * FROM " + Favourite.TABLE_NAME ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Favourite contact = new Favourite();
                contact.setLatitude(cursor.getDouble(cursor.getColumnIndex(Favourite.COLUMN_LATITUDE)));
                contact.setLongitude(cursor.getDouble(cursor.getColumnIndex(Favourite.COLUMN_LONGITUDE)));
                contact.setLocationAddress(cursor.getString(cursor.getColumnIndex(Favourite.COLUMN_ADDRESS)));
                contact.setId(cursor.getInt(cursor.getColumnIndex(Favourite.COLUMN_ID)));
                contacts.add(contact);
            } while (cursor.moveToNext());
        }

        db.close();

        return contacts;
    }





    public int updateContact(Favourite contact,String previousLocation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Favourite.COLUMN_LATITUDE, contact.getLatitude());
        values.put(Favourite.COLUMN_LONGITUDE, contact.getLongitude());
        values.put(Favourite.COLUMN_ADDRESS,contact.getLocationAddress());

        System.out.println("columnid ====>"+contact.getId());

        // updating row
        return db.update(Favourite.TABLE_NAME, values, Favourite.COLUMN_ADDRESS + " = ?",
                new String[]{previousLocation});
    }

    public void deleteContact(Favourite note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Favourite.TABLE_NAME, Favourite.COLUMN_ADDRESS + " = ?",
                new String[]{note.getLocationAddress()});
        db.close();
    }
}
