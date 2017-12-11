package com.uonagent.mathlibrary;

import android.app.Activity;
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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class AccountFragment extends Fragment {
    private static final String ARG_PARAM = "_id";
    private String mParam;

    ImageButton avatar;
    TextView name;
    TextView quant;

    ReadersDB readersDB;
    LibraryDB libraryDB;

    ListView listView;
    CustomAdapter adapter;

    public static AccountFragment newInstance(String param) {
        AccountFragment fragment = new AccountFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        avatar = (ImageButton) view.findViewById(R.id.button_avatar);
        name = (TextView) view.findViewById(R.id.client_name);
        quant = (TextView) view.findViewById(R.id.client_quant);

        readersDB = new ReadersDB(getContext());
        SQLiteDatabase database = readersDB.getWritableDatabase();
        Cursor cursor = database.query(readersDB.TABLE_NAME, null, readersDB.USER_ID +
                " = '" + mParam + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            name.setText(cursor.getString(cursor.getColumnIndex(readersDB.USER_NAME)));
            int q = 0;
            String tmp = cursor.getString(cursor.getColumnIndex(readersDB.TAKEN_BOOKS));
            if (tmp != null) {
                if (!tmp.isEmpty()) {
                    String[] books = tmp.split(",");
                    for (String s : books) {
                        String[] t = s.split(":");
                        q += Integer.parseInt(t[1]);
                    }
                }
            }
            quant.setText("Взято книг: " + q);
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File directory = cw.getDir("users_images", Context.MODE_PRIVATE);
            File mypath = new File(directory, "user_" + mParam + ".png");
            if (mypath.exists()) {
                try {
                    avatar.setImageBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(mypath)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                avatar.setImageResource(R.drawable.default_avatar);
            }
        }

        String takenBooks = cursor.getString(cursor.getColumnIndex(readersDB.TAKEN_BOOKS));
        Cursor cur;
        if (takenBooks != null) {
            listView = (ListView) view.findViewById(R.id.list_user);
            String[] arrTemp = takenBooks.split(",");
            String[] ids = new String[arrTemp.length];
            for (int i = 0; i < arrTemp.length; ++i) {
                ids[i] = arrTemp[i].split(":")[0];
            }
            libraryDB = new LibraryDB(getContext());
            SQLiteDatabase libDB = libraryDB.getWritableDatabase();

            StringBuilder whereStatement = new StringBuilder("");
            for (int i = 0; i < ids.length; i++) {
                if (i != (ids.length - 1))
                    whereStatement.append("_id = ? OR ");
                else
                    whereStatement.append("_id = ?");
            }

            cur = libDB.query(true, libraryDB.TABLE_NAME, null, whereStatement.toString(),
                    ids, null, null, null, null);

            String[] s = {libraryDB.TITLE, libraryDB.AUTHOR, libraryDB.ISBN, libraryDB.QUANTITY};
            int[] is = {R.id.title_common, R.id.author_common, R.id.isbn_common, R.id.quant_common};
            adapter = new CustomAdapter(getActivity().getApplicationContext(), R.layout.card_common, cur, s, is);
            listView.setAdapter(adapter);

            Cursor c = ((AccountFragment.CustomAdapter) listView.getAdapter()).getCursor();

            listView.setOnItemClickListener((parent, itemClicked, position, id) -> {
                c.moveToPosition(position);

                Log.i("ВЫБРАНА КНИГА ", c.getString(c.getColumnIndex(libraryDB.TITLE)));
                final String[] items = {"Вернуть", "Информация", "Отмена"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Выберите действие");
                builder.setItems(items, (dialogInterface, i) -> {
                    if (i == 0) {
                        String temp = cur.getString(cur.getColumnIndex(libraryDB.READERS));
                        String p = "";
                        int cntr = 0;
                        String[] _arr;
                        if (temp != null) {
                            if (!temp.isEmpty()) {
                                _arr = temp.split(",");
                                for (String it : _arr) {
                                    String[] parr = it.split(":");
                                    if (parr[0].equals(mParam)) {
                                        p = parr[1];
                                        break;
                                    }
                                    ++cntr;
                                }
                            }
                        }

                        final int size = Integer.parseInt(p);

                        dialogInterface.dismiss();
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        NumberPicker picker = new NumberPicker(getActivity());
                        picker.setMinValue(1);
                        picker.setMaxValue(size);
                        picker.setValue(1);
                        picker.setWrapSelectorWheel(true);
                        NumberPicker.OnValueChangeListener listener = (numberPicker, i12, i1) -> {
                        };
                        picker.setOnValueChangedListener(listener);
                        builder1.setView(picker);
                        builder1.setTitle("Сколько книг хотите вернуть?");
                        builder1.setPositiveButton("Вернуть", ((dialogInterface1, i1) -> {
                            if (picker.getValue() != size) {
                                dialogInterface1.dismiss();
                                ContentValues cv = new ContentValues();

                                Cursor readersCursor = database.query(readersDB.TABLE_NAME, null, readersDB.USER_ID +
                                        " = '" + mParam + "'", null, null, null, null);

                                if (readersCursor.moveToFirst()) {
                                    String taken = readersCursor.getString(readersCursor.getColumnIndex(readersDB.TAKEN_BOOKS));
                                    String[] arr = taken.split(",");
                                    int i2 = 0;
                                    //int currentQuant = 0;
                                    String[] o = new String[0];
                                    for (String it : arr) {
                                        o = it.split(":");
                                        if (o[0].equals(c.getString(c.getColumnIndex("_id")))) {
                                            break;
                                        }
                                        ++i2;
                                    }

                                    String tmp = o[0];
                                    int quantNew = Integer.parseInt(o[1]) - picker.getValue();
                                    tmp += ":" + quantNew;
                                    arr[i2] = tmp;
                                    StringBuilder stringBuilder = new StringBuilder("");
                                    for (String it : arr) {
                                        String putter = it + ",";
                                        stringBuilder.append(putter);
                                    }
                                    cv.clear();
                                    cv.put(readersDB.TAKEN_BOOKS, stringBuilder.toString());
                                    database.update(readersDB.TABLE_NAME, cv, "_id='" + mParam + "'", null);

                                    //////////////

                                    cv.clear();
                                    String readers = c.getString(c.getColumnIndex(libraryDB.READERS));

                                    String[] readArr = readers.split(",");
                                    String[] u = new String[0];
                                    int i3 = 0;
                                    boolean flag = false;
                                    for (String it : readArr) {
                                        u = it.split(":");
                                        if (u[0].equals(mParam)) {
                                            break;
                                        }
                                        ++i3;
                                    }

                                    String tmp2 = mParam;
                                    int remNew = Integer.parseInt(u[1]) - picker.getValue();
                                    tmp2 += ":" + remNew;
                                    readArr[i3] = tmp2;
                                    StringBuilder stringBuilder1 = new StringBuilder("");
                                    for (String it : readArr) {
                                        String putter = it + ",";
                                        stringBuilder1.append(putter);
                                    }
                                    cv.put(libraryDB.READERS, stringBuilder1.toString());
                                    libDB.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                            c.getInt(c.getColumnIndex("_id")) + "'", null);


                                    cv.clear();
                                    int newQuant = c.getInt(c.getColumnIndex(libraryDB.QUANTITY)) + picker.getValue();
                                    cv.put(libraryDB.QUANTITY, newQuant);
                                    libDB.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                            c.getInt(c.getColumnIndex("_id")) + "'", null);

                                    String[] _arrTemp = taken.split(",");
                                    String[] _ids = new String[_arrTemp.length];
                                    for (int _i = 0; _i < _arrTemp.length; ++_i) {
                                        _ids[_i] = _arrTemp[_i].split(":")[0];
                                    }


                                    StringBuilder _whereStatement = new StringBuilder("");
                                    for (int _i = 0; _i < _ids.length; _i++) {
                                        if (_i != (_ids.length - 1))
                                            _whereStatement.append("_id = ? OR ");
                                        else
                                            _whereStatement.append("_id = ?");
                                    }


                                    adapter.swapCursor(libDB.query(libraryDB.TABLE_NAME, null, _whereStatement.toString(),
                                            _ids, null, null, null));
                                    c.requery();
                                }
                            } else {
                                //todo delete
                                Cursor readersCursor = database.query(readersDB.TABLE_NAME, null, readersDB.USER_ID +
                                        " = '" + mParam + "'", null, null, null, null);
                                if (readersCursor.moveToFirst()) {
                                    String taken = readersCursor.getString(readersCursor.getColumnIndex(readersDB.TAKEN_BOOKS));
                                    String[] arr = taken.split(",");
                                    int i2 = 0;
                                    //int currentQuant = 0;
                                    String[] o = new String[0];
                                    for (String it : arr) {
                                        o = it.split(":");
                                        if (o[0].equals(c.getString(c.getColumnIndex("_id")))) {
                                            break;
                                        }
                                        ++i2;
                                    }
                                    String tmp = o[0];

                                    StringBuilder stringBuilder = new StringBuilder("");
                                    int ii2 = 0;
                                    for (String it : arr) {
                                        if (ii2 != i2) {
                                            String putter = it + ",";
                                            stringBuilder.append(putter);
                                        }
                                        ++ii2;
                                    }

                                    ContentValues cv = new ContentValues();
                                    cv.put(readersDB.TAKEN_BOOKS, stringBuilder.toString());
                                    database.update(readersDB.TABLE_NAME, cv, "_id='" + mParam + "'", null);

                                    ///////////

                                    cv.clear();
                                    String readers = c.getString(c.getColumnIndex(libraryDB.READERS));

                                    String[] readArr = readers.split(",");
                                    String[] u = new String[0];
                                    int i3 = 0;
                                    boolean flag = false;
                                    for (String it : readArr) {
                                        u = it.split(":");
                                        if (u[0].equals(mParam)) {
                                            break;
                                        }
                                        ++i3;
                                    }

                                    String tmp2 = mParam;
                                    StringBuilder stringBuilder1 = new StringBuilder("");
                                    int ii3 = 0;
                                    for (String it : readArr) {
                                        if (ii3 != i3) {
                                            String putter = it + ",";
                                            stringBuilder1.append(putter);
                                        }
                                        ++ii3;
                                    }
                                    cv.put(libraryDB.READERS, stringBuilder1.toString());
                                    libDB.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                            c.getInt(c.getColumnIndex("_id")) + "'", null);


                                    cv.clear();
                                    int newQuant = c.getInt(c.getColumnIndex(libraryDB.QUANTITY)) + picker.getValue();
                                    cv.put(libraryDB.QUANTITY, newQuant);
                                    libDB.update(libraryDB.TABLE_NAME, cv, "_id='" +
                                            c.getInt(c.getColumnIndex("_id")) + "'", null);
                                    String[] _arrTemp = taken.split(",");
                                    String[] _ids = new String[_arrTemp.length];
                                    for (int _i = 0; _i < _arrTemp.length; ++_i) {
                                        _ids[_i] = _arrTemp[_i].split(":")[0];
                                    }


                                    StringBuilder _whereStatement = new StringBuilder("");
                                    for (int _i = 0; _i < _ids.length; _i++) {
                                        if (_i != (_ids.length - 1))
                                            _whereStatement.append("_id = ? OR ");
                                        else
                                            _whereStatement.append("_id = ?");
                                    }


                                    adapter.swapCursor(libDB.query(libraryDB.TABLE_NAME, null, _whereStatement.toString(),
                                            _ids, null, null, null));
                                    c.requery();
                                }
                            }
                            //Log.i("Выбрано", " " + picker.getValue());
                        }));
                        builder1.setNegativeButton("Отмена", (dialogInterface12, i13) -> {
                            dialogInterface12.dismiss();
                        });
                        AlertDialog np = builder1.create();
                        np.show();
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
        }
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
            String temp = cursor.getString(cursor.getColumnIndex(libraryDB.READERS));
            String taken = "";
            if (temp != null) {
                if (!temp.isEmpty()) {
                    String[] arr = temp.split(",");
                    for (String it : arr) {
                        taken = it.split(":")[1];
                        if (taken.equals(mParam)) {
                            break;
                        }
                    }
                }
            }
            quant.setText("Взято книг: " + taken);

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
