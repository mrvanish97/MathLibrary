package com.uonagent.mathlibrary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReadersDB extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "readersDb";

    public static final String TABLE_NAME = "readers";
    public static final String USER_ID = "_id";
    public static final String PASSWORD = "password";
    public static final String SALT = "salt";
    public static final String USER_NAME = "userName";
    public static final String E_MAIL = "eMail";
    public static final String TAKEN_BOOKS = "takenBooks";
    //id:quant,

    private static final String CREATE_TABLE = "create table " + TABLE_NAME
            + " ( " + USER_ID + " integer primary key, " + PASSWORD
            + " TEXT NOT NULL, " + SALT + " TEXT NOT NULL, " + USER_NAME + " TEXT NOT NULL, " +
            E_MAIL + " TEXT NOT NULL unique, " + TAKEN_BOOKS + " TEXT)";


    public ReadersDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
