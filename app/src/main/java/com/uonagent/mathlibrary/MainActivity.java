package com.uonagent.mathlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    Button buttonLogin, buttonReg;
    EditText login, pass;

    ReadersDB readersDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (EditText) findViewById(R.id.editTextLogin);
        pass = (EditText) findViewById(R.id.editTextPassword);

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonReg = (Button) findViewById(R.id.buttonReg);

        readersDB = new ReadersDB(this);

        buttonLogin.setOnClickListener(view -> {
            try {
                if (login.getText().toString().equals("-1") &&
                        MD5Hash.getHash(pass.getText().toString()).equals("e5418206ab66d0f3db3ba11c74af85e2")) {
                    login.setText("");
                    pass.setText("");
                    startActivity(new Intent("com.uonagent.mathlibrary.AdminMainActivity"));
                } else if (login.getText().toString().equals("-1") &&
                        !(MD5Hash.getHash(pass.getText().toString()).equals("e5418206ab66d0f3db3ba11c74af85e2"))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Ошибка!")
                            .setMessage("Проверьте введённые данные")
                            .setIcon(R.drawable.common_google_signin_btn_icon_light)
                            .setCancelable(false)
                            .setNegativeButton("OK",
                                    (dialog, id) -> dialog.cancel());
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    SQLiteDatabase database = readersDB.getWritableDatabase();
                    Cursor cursor = database.query(readersDB.TABLE_NAME, null, readersDB.USER_ID +
                            " = '" + login.getText().toString() + "'", null, null, null, null);
                    if (cursor.moveToFirst()) {
                        int hashIndex = cursor.getColumnIndex(readersDB.PASSWORD);
                        int saltIndex = cursor.getColumnIndex(readersDB.SALT);
                        String hashFromDB = cursor.getString(hashIndex);
                        String salt = cursor.getString(saltIndex);
                        if (MD5Hash.getHash(pass.getText().toString() + salt).equals(hashFromDB)) {
                            Bitmap userAvatar;
                            ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            File directory = cw.getDir("users_images", Context.MODE_PRIVATE);
                            File mypath = new File(directory, "user_" +
                                    Integer.valueOf(login.getText().toString()).toString() + ".png");
                            if (mypath.exists()) {
                                userAvatar = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(mypath));
                            } else {
                                Drawable myDrawable = getResources().getDrawable(R.drawable.default_avatar);
                                userAvatar = ((BitmapDrawable) myDrawable).getBitmap();
                            }
                            String l = login.getText().toString();
                            login.setText("");
                            pass.setText("");
                            startActivity(new Intent("com.uonagent.mathlibrary.ClientActivity").putExtra("_id", l));
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Ошибка!")
                                    .setMessage("Проверьте введённые данные")
                                    .setIcon(R.drawable.common_google_signin_btn_icon_light)
                                    .setCancelable(false)
                                    .setNegativeButton("OK",
                                            (dialog, id) -> dialog.cancel());
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Ошибка!")
                                .setMessage("Проверьте введённые данные")
                                .setIcon(R.drawable.common_google_signin_btn_icon_light)
                                .setCancelable(false)
                                .setNegativeButton("OK",
                                        (dialog, id) -> dialog.cancel());
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttonReg.setOnClickListener(view -> {
            startActivity(new Intent("com.uonagent.mathlibrary.RegistrationActivity"));
        });
    }
}
