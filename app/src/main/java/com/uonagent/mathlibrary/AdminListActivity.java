package com.uonagent.mathlibrary;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class AdminListActivity extends AppCompatActivity {

    //ReadersDB readersDB;
    LibraryDB libraryDB;

    ListView listView;

    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        libraryDB = new LibraryDB(this);
        SQLiteDatabase database = libraryDB.getWritableDatabase();
        Cursor cursor = database.query(libraryDB.TABLE_NAME, null, null,
                null, null, null, null);

        String[] s = {libraryDB.TITLE, libraryDB.AUTHOR, libraryDB.ISBN, libraryDB.QUANTITY};
        int[] is = {R.id.title_common, R.id.author_common, R.id.isbn_common, R.id.quant_common};
        adapter = new CustomAdapter(this, R.layout.card_common, cursor, s, is);

        listView = (ListView) findViewById(R.id.recyclerAdmin);
        listView.setAdapter(adapter);
        Cursor c = ((CustomAdapter) listView.getAdapter()).getCursor();

        listView.setOnItemClickListener((parent, itemClicked, position, id) -> {
            c.moveToPosition(position);
            final String[] items = {"Изменить количество", "Информация", "Удалить", "Отмена"};
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminListActivity.this);
            builder.setTitle("Выберите действие");
            builder.setItems(items, (dialogInterface, i) -> {
                if (i == 0) {
                    Toast.makeText(getApplicationContext(), Long.toString(id),
                            Toast.LENGTH_SHORT).show();
                } else if (i == 1) {
                    dialogInterface.dismiss();
                    startActivity(new Intent("com.uonagent.mathlibrary.WebActivity")
                            .putExtra("url", c.getString(c.getColumnIndex(libraryDB.DESCRIPTION))));
                } else if (i == 2) {
                    dialogInterface.dismiss();
                } else if (i == 3) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public class CustomAdapter extends SimpleCursorAdapter {

        private Context mContext;
        private Context appContext;
        private int layout;
        private Cursor cr;
        private final LayoutInflater inflater;

        public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to, 0);
            this.layout = layout;
            this.mContext = context;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            //TextView title = (TextView) view.findViewById(R.id.title_common);
            //TextView author = (TextView) view.findViewById(R.id.author_common);
            TextView isbn = (TextView) view.findViewById(R.id.isbn_common);
            TextView quant = (TextView) view.findViewById(R.id.quant_common);

            isbn.setText("ISBN: " + cursor.getString(cursor.getColumnIndex(libraryDB.ISBN)));
            quant.setText("Доступно книг: " + cursor.getString(cursor.getColumnIndex(libraryDB.QUANTITY)));

            ImageView cover = (ImageView) view.findViewById(R.id.cover_common);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("covers", Context.MODE_PRIVATE);
            File mypath = new File(directory, "book_" +
                    cursor.getInt(cursor.getColumnIndex("_id")) + ".png");
            if (mypath.exists()) {
                try {
                    Bitmap coverBmp;
                    coverBmp = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.fromFile(mypath));
                    cover.setImageBitmap(coverBmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                cover.setImageResource(R.drawable.ic_book);
            }
        }

    }
}
