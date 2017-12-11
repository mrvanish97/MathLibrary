package com.uonagent.mathlibrary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibraryDB extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "Library";

    public static final String TABLE_NAME = "books";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String ISBN = "isbn";
    public static final String DESCRIPTION = "description";
    public static final String QUANTITY = "quantity";
    public static final String READERS = "readers";
    //id:quant,

    private static final String CREATE_TABLE = "create table " + TABLE_NAME
            + " ( _id integer primary key, " + TITLE
            + " TEXT NOT NULL, " + AUTHOR + " TEXT NOT NULL, " + ISBN + " TEXT NOT NULL unique, " +
            DESCRIPTION + " TEXT, " + QUANTITY + " INTEGER, " + READERS + " TEXT)";

    public LibraryDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
