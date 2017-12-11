package com.uonagent.mathlibrary;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.io.File;

public class AdminMainActivity extends AppCompatActivity {

    Button showList;
    Button addBook;
    Button wipe;
    Button exit;

    ReadersDB readersDB;
    LibraryDB libraryDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        showList = (Button) findViewById(R.id.button_view_list);
        addBook = (Button) findViewById(R.id.button_admin_add);
        wipe = (Button) findViewById(R.id.button_wipe);
        exit = (Button) findViewById(R.id.button_admin_exit);

        readersDB = new ReadersDB(this);
        libraryDB = new LibraryDB(this);

        showList.setOnClickListener(view -> {
            startActivity(new Intent("com.uonagent.mathlibrary.AdminListActivity"));
        });

        addBook.setOnClickListener(view -> {
            startActivity(new Intent("com.uonagent.mathlibrary.AdminAddActivity"));
        });

        wipe.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Удалить все данные?")
                    .setMessage("Все данные из БД, а так же изображения аватарок и обложки, будут удалены")
                    .setIcon(R.drawable.common_google_signin_btn_icon_light)
                    .setCancelable(false)
                    .setNegativeButton("Нет",
                            (dialog, id) -> dialog.cancel())
                    .setPositiveButton("Да",
                            (dialog, id) -> {
                                SQLiteDatabase databaseR = readersDB.getWritableDatabase();
                                SQLiteDatabase databaseL = libraryDB.getWritableDatabase();
                                databaseL.delete(libraryDB.TABLE_NAME, null, null);
                                databaseR.delete(readersDB.TABLE_NAME, null, null);
                                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                                File directoryAvatars = cw.getDir("users_images", Context.MODE_PRIVATE);
                                File directoryCovers = cw.getDir("covers", Context.MODE_PRIVATE);
                                for (File f : directoryAvatars.listFiles()) {
                                    f.delete();
                                }
                                for (File f : directoryCovers.listFiles()) {
                                    f.delete();
                                }
                                dialog.cancel();
                            });
            AlertDialog alert = builder.create();
            alert.show();
        });

        exit.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Выйти из системы?");
        builder.setNegativeButton("Нет", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("Да", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            super.onBackPressed();
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
