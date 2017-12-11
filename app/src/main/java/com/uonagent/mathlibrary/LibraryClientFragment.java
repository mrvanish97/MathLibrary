package com.uonagent.mathlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class LibraryClientFragment extends Fragment {

    private static final String ARG_PARAM = "_id";
    private String mParam;

    ReadersDB readersDB;
    LibraryDB libraryDB;

    ListView listView;

    SimpleCursorAdapter adapter;
    public Cursor cursor;

    public static LibraryClientFragment newInstance(String param) {
        LibraryClientFragment fragment = new LibraryClientFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getString(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_client, container, false);

        libraryDB = new LibraryDB(getContext());
        readersDB = new ReadersDB(getContext());
        SQLiteDatabase databaseL = libraryDB.getWritableDatabase();
        SQLiteDatabase databaseR = readersDB.getWritableDatabase();
        cursor = databaseL.query(libraryDB.TABLE_NAME, null, null,
                null, null, null, null);

        String[] s = {libraryDB.TITLE, libraryDB.AUTHOR, libraryDB.ISBN, libraryDB.QUANTITY};
        int[] is = {R.id.title_common, R.id.author_common, R.id.isbn_common, R.id.quant_common};
        adapter = new CustomAdapter(getActivity().getApplicationContext(), R.layout.card_common, cursor, s, is);

        listView = (ListView) view.findViewById(R.id.recyclerClient);
        listView.setAdapter(adapter);
        Cursor c = ((CustomAdapter) listView.getAdapter()).getCursor();

        listView.setOnItemClickListener((parent, itemClicked, position, id) -> {
            c.moveToPosition(position);
            Log.i("ВЫБРАНА КНИГА ", c.getString(c.getColumnIndex(libraryDB.TITLE)));
            final String[] items = {"Взять", "Информация", "Отмена"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Выберите действие");
            builder.setItems(items, (dialogInterface, i) -> {
                if (i == 0) {
                    if (c.getInt(c.getColumnIndex(libraryDB.QUANTITY)) != 0) {
                        dialogInterface.dismiss();
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        NumberPicker picker = new NumberPicker(getActivity());
                        picker.setMinValue(1);
                        picker.setMaxValue(c.getInt(c.getColumnIndex(libraryDB.QUANTITY)));
                        picker.setValue(1);
                        picker.setWrapSelectorWheel(true);
                        NumberPicker.OnValueChangeListener listener = (numberPicker, i12, i1) -> {
                        };
                        picker.setOnValueChangedListener(listener);
                        builder1.setView(picker);
                        builder1.setTitle("Сколько книг хотите взять?");
                        builder1.setPositiveButton("Взять", ((dialogInterface1, i1) -> {
                            dialogInterface1.dismiss();
                            ContentValues cv = new ContentValues();

                            Cursor readersCursor = databaseR.query(readersDB.TABLE_NAME, null, readersDB.USER_ID +
                                    " = '" + mParam + "'", null, null, null, null);

                            if (readersCursor.moveToFirst()) {
                                String taken = readersCursor.getString(readersCursor.getColumnIndex(readersDB.TAKEN_BOOKS));
                                if (taken != null) {
                                    String[] arr = taken.split(",");
                                    int i2 = 0;
                                    //int currentQuant = 0;
                                    String[] o = new String[0];
                                    boolean flag = false;
                                    for (String it : arr) {
                                        o = it.split(":");
                                        if (o[0].equals(c.getString(c.getColumnIndex("_id")))) {
                                            flag = true;
                                            break;
                                        }
                                        ++i2;
                                    }
                                    if (flag) {
                                        String tmp = o[0];
                                        int quantNew = Integer.parseInt(o[1]) + picker.getValue();
                                        tmp += ":" + quantNew;
                                        arr[i2] = tmp;
                                        StringBuilder stringBuilder = new StringBuilder("");
                                        for (String it : arr) {
                                            String putter = it + ",";
                                            stringBuilder.append(putter);
                                        }
                                        cv.clear();
                                        cv.put(readersDB.TAKEN_BOOKS, stringBuilder.toString());
                                        databaseR.update(readersDB.TABLE_NAME, cv, "_id='" + mParam + "'", null);

                                    } else {
                                        cv.clear();
                                        cv.put(readersDB.TAKEN_BOOKS, taken + c.getString(c.getColumnIndex("_id")) + ":" +
                                                picker.getValue() + ",");
                                        databaseR.update(readersDB.TABLE_NAME, cv, "_id='" + mParam + "'", null);
                                    }
                                } else {
                                    cv.clear();
                                    cv.put(readersDB.TAKEN_BOOKS, c.getString(c.getColumnIndex("_id")) + ":" +
                                            picker.getValue() + ",");
                                    databaseR.update(readersDB.TABLE_NAME, cv, "_id='" + mParam + "'", null);
                                }
                                cv.clear();
                                String readers = c.getString(c.getColumnIndex(libraryDB.READERS));
                                if (readers != null) {
                                    String[] readArr = readers.split(",");
                                    String[] u = new String[0];
                                    int i3 = 0;
                                    boolean flag = false;
                                    for (String it : readArr) {
                                        u = it.split(":");
                                        if (u[0].equals(mParam)) {
                                            flag = true;
                                            break;
                                        }
                                        ++i3;
                                    }
                                    if (flag) {
                                        String tmp2 = mParam;
                                        int remNew = Integer.parseInt(u[1]) + picker.getValue();
                                        tmp2 += ":" + remNew;
                                        readArr[i3] = tmp2;
                                        StringBuilder stringBuilder1 = new StringBuilder("");
                                        for (String it : readArr) {
                                            String putter = it + ",";
                                            stringBuilder1.append(putter);
                                        }
                                        cv.put(libraryDB.READERS, stringBuilder1.toString());
                                        databaseL.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                                c.getInt(c.getColumnIndex("_id")) + "'", null);
                                    } else {
                                        cv.put(libraryDB.READERS, readers + mParam + ":" + picker.getValue() + ",");
                                        databaseL.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                                c.getInt(c.getColumnIndex("_id")) + "'", null);
                                    }
                                } else {
                                    cv.put(libraryDB.READERS, mParam + ":" + picker.getValue() + ",");
                                    databaseL.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                            c.getInt(c.getColumnIndex("_id")) + "'", null);
                                }
                            }
                            cv.clear();
                            int newQuant = c.getInt(c.getColumnIndex(libraryDB.QUANTITY)) - picker.getValue();
                            cv.put(libraryDB.QUANTITY, newQuant);
                            databaseL.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                    c.getInt(c.getColumnIndex("_id")) + "'", null);
                            adapter.swapCursor(databaseL.query(libraryDB.TABLE_NAME, null, null,
                                    null, null, null, null));
                            c.requery();
                            //Log.i("Выбрано", " " + picker.getValue());
                        }));
                        builder1.setNegativeButton("Отмена", (dialogInterface12, i13) -> {
                            dialogInterface12.dismiss();
                        });
                        AlertDialog np = builder1.create();
                        np.show();
                    } else {
                        dialogInterface.dismiss();
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        builder1.setTitle("Книг нет");
                        builder1.setNegativeButton("ОК", (dialogInterface13, i14) -> {
                            dialogInterface13.dismiss();
                        });
                        AlertDialog df = builder1.create();
                        df.show();
                    }
                } else if (i == 1) {
                    dialogInterface.dismiss();
                    startActivity(new Intent("com.uonagent.mathlibrary.WebActivity")
                            .putExtra("url", c.getString(c.getColumnIndex(libraryDB.DESCRIPTION))));
                } else if (i == 2) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        return view;
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
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File directory = cw.getDir("covers", Context.MODE_PRIVATE);
            File mypath = new File(directory, "book_" +
                    cursor.getInt(cursor.getColumnIndex("_id")) + ".png");
            if (mypath.exists()) {
                try {
                    Bitmap coverBmp;
                    coverBmp = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), Uri.fromFile(mypath));
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
