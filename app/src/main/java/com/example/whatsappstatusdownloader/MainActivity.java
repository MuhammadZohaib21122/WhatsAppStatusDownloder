package com.example.whatsappstatusdownloader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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
        String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.FOLDER_NAME + "Media/.Statuses";
        File targetDirector = new File(targetPath);

        if (!targetDirector.exists()) {
            targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
            targetDirector = new File(targetPath);
        }

        if (targetDirector.exists() && targetDirector.isDirectory()) {
            files = targetDirector.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (!file.getName().endsWith(".nomedia")) {
                        StoryModel story = new StoryModel();
                        story.setUri(Uri.fromFile(file));
                        story.setPath(file.getAbsolutePath());
                        story.setFilename(file.getName());
                        filesList.add(story);
                    }
                }
            }
        }

        Toast.makeText(this, "Size = " + filesList.size(), Toast.LENGTH_SHORT).show();
        return filesList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkWhatsAppPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Uri wa_status_uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, wa_status_uri);
        startActivityForResult(intent, 10001);
    }
}
