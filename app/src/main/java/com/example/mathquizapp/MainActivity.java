package com.example.mathquizapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.mathquizapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int RC_PIC_CONTACT = 100;
    private static final int RC_READ_CONTACTS = 200;
    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                RC_READ_CONTACTS);

        binding.getContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    requestContact();
                } else {
                    doIfNotGranted();
                }
            }
        });
    }

    private void requestContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RC_PIC_CONTACT);
    }

    private void doIfNotGranted() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            // Show an explanation to the user
            Toast.makeText(MainActivity.this, "Permission is needed to read contacts!", Toast.LENGTH_LONG).show();
        }
        if (sessionManager.isFirstTimeAsking(Manifest.permission.READ_CONTACTS)) {
            sessionManager.firstTimeAsking(Manifest.permission.READ_CONTACTS, false);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    RC_READ_CONTACTS);
        } else {
            //Permission disable by device policy or user denied permanently. Show proper error message
            Toast.makeText(MainActivity.this, "Permission is needed to read contacts, to avail this feature give permission from app setting.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PIC_CONTACT && resultCode == RESULT_OK) {
            Cursor cursor = null;
            try {
                String phoneNo = null;
                String name = null;
                Uri uri = data.getData();
                cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
                phoneNo = cursor.getString(phoneIndex);
                name = cursor.getString(nameIndex);
                binding.resultText.setText("Name: " + name + "\n");
                binding.resultText.append("Number: " + phoneNo + "\n");
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission was granted. Now you can call your method to open contacts, fetch contact or whatever
                   requestContact();
                } else {
                    // Permission was denied.......
                    // You can again ask for permission from here
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

}