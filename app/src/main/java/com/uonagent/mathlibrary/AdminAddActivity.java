package com.uonagent.mathlibrary;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminAddActivity extends AppCompatActivity {

    ImageButton buttonCover;
    Button buttonAdd;

    TextView textTitle;
    TextView textAuthor;
    TextView textISBN;
    TextView textLink;
    TextView textQuant;
    private String link = "";

    LibraryDB libraryDB;

    Bitmap tmpCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add);

        buttonCover = (ImageButton) findViewById(R.id.buttonCover);
        buttonAdd = (Button) findViewById(R.id.buttonNewBook);

        textTitle = (TextView) findViewById(R.id.editTextAddTitle);
        textAuthor = (TextView) findViewById(R.id.editTextAddAuthor);
        textISBN = (TextView) findViewById(R.id.editTextAddISBN);
        textLink = (TextView) findViewById(R.id.editTextAddLink);
        textQuant = (TextView) findViewById(R.id.editTextAddQuantity);

        libraryDB = new LibraryDB(this);

        buttonCover.setOnClickListener(view -> {
            final String[] items = {"Камера", "Галерея", "Cсылка", "Отмена"};
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddActivity.this);
            builder.setTitle("Прикрепите обложку");
            builder.setItems(items, (dialogInterface, i) -> {
                if (i == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                } else if (i == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);
                } else if (i == 2) {
                    dialogInterface.dismiss();
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                    builder2.setTitle("Введите ссылку");

                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder2.setView(input);
                    builder2.setPositiveButton("OK", (dialog, which) -> {
                        link = input.getText().toString();
                        ImageDownloader.Listener listener = new ImageDownloader.Listener() {
                            @Override
                            public void onImageLoaded(Bitmap bitmap) {
                                buttonCover.setImageBitmap(bitmap);
                                tmpCover = bitmap;
                            }
                            @Override
                            public void onError() {
                                Toast.makeText(getApplicationContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                            }
                        };
                        new ImageDownloader(listener).execute(link);
                    });
                    builder2.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder2.show();
                } else if (i == 3) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        buttonAdd.setOnClickListener(view -> {
            //Matcher matcherIsbn = Pattern.compile("^(?:ISBN(?:-1[03])?:? )-?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$").matcher(textISBN.getText().toString());
            //Matcher matcherLink = Pattern.compile("^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$").matcher(textLink.getText().toString());
            Matcher matcherNatural = Pattern.compile("^[+]{0,1}[0]*[1-9]+\\d*$").matcher(textQuant.getText().toString());
            if (textTitle.getText().toString().isEmpty() || textAuthor.getText().toString().isEmpty() ||
                    textISBN.getText().toString().isEmpty() || textLink.getText().toString().isEmpty()
                    || !matcherNatural.matches()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddActivity.this);
                builder.setTitle("Ошибка!")
                        .setMessage("Проверьте введённые данные")
                        .setIcon(R.drawable.common_google_signin_btn_icon_light)
                        .setCancelable(false)
                        .setNegativeButton("OK",
                                (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                SQLiteDatabase database = libraryDB.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(libraryDB.TITLE, textTitle.getText().toString());
                contentValues.put(libraryDB.AUTHOR, textAuthor.getText().toString());
                contentValues.put(libraryDB.ISBN, textISBN.getText().toString());
                contentValues.put(libraryDB.DESCRIPTION, textLink.getText().toString());
                contentValues.put(libraryDB.QUANTITY, textQuant.getText().toString());
                if (database.insert(libraryDB.TABLE_NAME, "readers", contentValues) != -1) {
                    Cursor cursor = database.query(libraryDB.TABLE_NAME, null, null,
                            null, null, null, null);
                    int bookId = -1;
                    if (cursor.moveToLast()) {
                        int idIndex = cursor.getColumnIndex("_id");
                        bookId = cursor.getInt(idIndex);
                    }
                    if (tmpCover != null) {
                        ImageSaver.saveCoverToInternalStorage(tmpCover, bookId, getApplicationContext());
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddActivity.this);
                    builder.setTitle("Продолжить добавление?")
                            .setMessage("id добавленной книги: " + bookId)
                            .setIcon(R.drawable.common_google_signin_btn_icon_light)
                            .setCancelable(false)
                            .setPositiveButton("Да",
                                    (dialog, id) -> {
                                        textQuant.setText("");
                                        textTitle.setText("");
                                        textAuthor.setText("");
                                        textISBN.setText("");
                                        textLink.setText("");
                                        buttonCover.setImageResource(R.drawable.add_book);
                                        dialog.cancel();
                                    }).setNegativeButton("Нет", (dialog, id) -> {
                        dialog.cancel();
                        super.onBackPressed();
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddActivity.this);
                    builder.setTitle("Ошибка!")
                            .setMessage("Книга с таким ISBN уже зарегистрирована")
                            .setIcon(R.drawable.common_google_signin_btn_icon_light)
                            .setCancelable(false)
                            .setNegativeButton("OK",
                                    (dialog, id) -> dialog.cancel());
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                tmpCover = crop(bmp);
                buttonCover.setImageBitmap(tmpCover);
            } else if (requestCode == 1) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    tmpCover = crop(bmp);
                    buttonCover.setImageBitmap(tmpCover);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap crop(Bitmap bmp) {
        return bmp;
    }

    /*@Override
    public void onImageLoaded(Bitmap bitmap) {

        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error Loading Image !", Toast.LENGTH_SHORT).show();
    }*/
}
