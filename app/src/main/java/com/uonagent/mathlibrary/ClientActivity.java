package com.uonagent.mathlibrary;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.FrameMetrics;
import android.view.MenuItem;
import android.widget.TextView;

public class ClientActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_library:
                setTitle("Библиотека");
                android.support.v4.app.FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                transaction1.replace(R.id.frame, LibraryClientFragment.newInstance(getIntent().getStringExtra("_id")));
                transaction1.commit();
                return true;
            case R.id.navigation_profile:
                setTitle("Профиль");
                android.support.v4.app.FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                transaction2.replace(R.id.frame, AccountFragment.newInstance(getIntent().getStringExtra("_id")));
                transaction2.commit();
                return true;
            case R.id.navigation_exit:

                onBackPressed();
        }
        return true;
    };

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выйти из системы?");
        builder.setNegativeButton("Нет", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("Да", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            super.onBackPressed();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_library);
    }

}
