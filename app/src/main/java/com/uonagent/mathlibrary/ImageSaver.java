package com.uonagent.mathlibrary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageSaver {
    public static String saveAvatarToInternalStorage(Bitmap bitmapImage, int id, Context appContext) {
        ContextWrapper cw = new ContextWrapper(appContext);
        File directory = cw.getDir("users_images", Context.MODE_PRIVATE);
        File mypath = new File(directory, "user_" + Integer.valueOf(id).toString() + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public static Bitmap readAvatarFromInternalStorage(int id, Context appContext,
                                                       ContentResolver contentResolver) throws IOException {
        ContextWrapper cw = new ContextWrapper(appContext);
        File directory = cw.getDir("users_images", Context.MODE_PRIVATE);
        File mypath = new File(directory, "user_" + Integer.valueOf(id).toString() + ".png");
        return MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(mypath));
    }

    public static String saveCoverToInternalStorage(Bitmap bitmapImage, int id, Context appContext) {
        ContextWrapper cw = new ContextWrapper(appContext);
        File directory = cw.getDir("covers", Context.MODE_PRIVATE);
        File mypath = new File(directory, "book_" + Integer.valueOf(id).toString() + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public static Bitmap readCoverFromInternalStorage(int id, Context appContext,
                                                      ContentResolver contentResolver) throws IOException {
        ContextWrapper cw = new ContextWrapper(appContext);
        File directory = cw.getDir("covers", Context.MODE_PRIVATE);
        File mypath = new File(directory, "book_" + Integer.valueOf(id).toString() + ".png");
        return MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(mypath));
    }
}
