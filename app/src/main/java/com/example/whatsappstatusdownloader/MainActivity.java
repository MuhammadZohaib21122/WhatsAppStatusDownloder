package com.example.whatsappstatusdownloader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    StoryAdapter storyAdapter;
    File[] files;
    ArrayList<StoryModel> filesList = new ArrayList<>();
    private static final String PREFS_NAME = "MyAppPreferences";
    private static final String KEY_FIRST_LAUNCH = "firstLaunch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//  Remove Status bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);  // hide mobile key button

        initViews();
        if (isFirstLaunch()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkWhatsAppPermission();
            }
            markFirstLaunch();
        }

        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        setUpRefreshLayout();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void markFirstLaunch() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }

    private boolean isFirstLaunch() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipeRecyclerView);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            setUpRefreshLayout();
            new Handler().postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
            }, 2000);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUpRefreshLayout() {
        filesList.clear();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storyAdapter = new StoryAdapter(MainActivity.this, getData());
        recyclerView.setAdapter(storyAdapter);
        storyAdapter.notifyDataSetChanged();
    }

    private ArrayList<StoryModel> getData() {
        ArrayList<StoryModel> filesList = new ArrayList<>();

        String[] possiblePaths = {
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/.Statuses",
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
        };

        for (String targetPath : possiblePaths) {
            File targetDirector = new File(targetPath);

            if (targetDirector.exists() && targetDirector.isDirectory()) {
                Log.d("StatusDirectory", "Directory exists: " + targetPath);
                File[] files = targetDirector.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && !file.getName().endsWith(".nomedia")) {
                            StoryModel story = new StoryModel();
                            story.setUri(Uri.fromFile(file));
                            story.setPath(file.getAbsolutePath());
                            story.setFilename(file.getName());
                            filesList.add(story);
                        }
                    }
                } else {
                    Log.d("StatusDirectory", "No files found in: " + targetPath);
                }
            } else {
                Log.d("StatusDirectory", "Directory does not exist: " + targetPath);
            }
        }

        Toast.makeText(this, "Statuses found: " + filesList.size(), Toast.LENGTH_SHORT).show();
        return filesList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkWhatsAppPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request MANAGE_EXTERNAL_STORAGE permission for Android 11 and above
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 10001);
        } else {
            // For Android 10, use ACTION_OPEN_DOCUMENT_TREE
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            Uri waStatusUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, waStatusUri);
            startActivityForResult(intent, 10001);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Log.d("Permission", "Uri: " + uri.toString());
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Permission", "Permission not granted");
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
