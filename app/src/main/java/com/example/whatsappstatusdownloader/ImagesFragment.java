package com.example.whatsappstatusdownloader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videodownloader.R;
import com.example.videodownloader.editor.WhatsappStatus.Adapter.ImageAdapter;
import com.example.videodownloader.editor.WhatsappStatus.Adapter.ImageAdapter30plus;
import com.example.videodownloader.editor.WhatsappStatus.Models.Status;
import com.example.videodownloader.editor.WhatsappStatus.Models.StatusDocFile;
import com.example.videodownloader.editor.WhatsappStatus.Utils.Common;

import java.util.ArrayList;
import java.util.List;

public class ImagesFragment extends Fragment {

    private static final int REQUEST_ACTION_OPEN_DOCUMENT_TREE = 5544;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private final List<Status> imagesList = new ArrayList<>();
    private final List<StatusDocFile> imagesList30plus = new ArrayList<>();
    private final Handler handler = new Handler();
    private ImageAdapter imageAdapter;
    private ImageAdapter30plus imageAdapter30plus;
    private ConstraintLayout container;
    private TextView messageTextView;
    private boolean notFromButtonnnn = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setUpRecyclerView();
        setDataInList();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        container = view.findViewById(R.id.whatsAppImageFragment);
        messageTextView = view.findViewById(R.id.messageTextView);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), Common.GRID_COUNT));
        recyclerView.setHasFixedSize(true);
        imageAdapter = new ImageAdapter(imagesList, container);
        imageAdapter30plus = new ImageAdapter30plus(imagesList30plus, container);
    }

    private void setDataInList() {
        handler.post(() -> {
            imagesList.clear();
            imagesList30plus.clear();
            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    loadLegacyImages();
                } else {
                    loadScopedStorageImages();
                }
            }).start();
        });
    }

    private void loadLegacyImages() {
        if (Common.STATUS_DIRECTORY.exists()) {
            File[] statusFiles = Common.STATUS_DIRECTORY.listFiles();
            if (statusFiles != null && statusFiles.length > 0) {
                for (File file : statusFiles) {
                    Status status = new Status(file, file.getName(), file.getAbsolutePath());
                    if (!status.isVideo()) {
                        imagesList.add(status);
                    }
                }
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setAdapter(imageAdapter);
                    imageAdapter.notifyDataSetChanged();
                });
            }
        } else {
            handler.post(() -> progressBar.setVisibility(View.GONE));
        }
    }

    private void loadScopedStorageImages() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("WATool", Context.MODE_PRIVATE);
        String treeUri = preferences.getString("uri", "");

        if (treeUri == null || treeUri.isEmpty()) {
            requestScopedStorageAccess();
        } else {
            readScopedStorageImages(Uri.parse(treeUri));
        }
    }

    private void requestScopedStorageAccess() {
        Toast.makeText(getActivity(), "Please select the WhatsApp Status directory", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_ACTION_OPEN_DOCUMENT_TREE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACTION_OPEN_DOCUMENT_TREE && data != null && data.getData() != null) {
            Uri treeUri = data.getData();
            requireActivity().getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            SharedPreferences preferences = requireActivity().getSharedPreferences("WATool", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uri", treeUri.toString());
            editor.apply();

            readScopedStorageImages(treeUri);
        }
    }

    private void readScopedStorageImages(Uri treeUri) {
        DocumentFile directory = DocumentFile.fromTreeUri(requireContext(), treeUri);

        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (DocumentFile file : directory.listFiles()) {
                if (!file.isDirectory() && file.getName() != null && file.getName().endsWith(".jpg")) {
                    StatusDocFile status = new StatusDocFile(file, file.getName(), file.getUri().toString());
                    if (!status.isVideo()) {
                        imagesList30plus.add(status);
                    }
                }
            }
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(imageAdapter30plus);
                imageAdapter30plus.notifyDataSetChanged();
            });
        } else {
            handler.post(() -> progressBar.setVisibility(View.GONE));
        }
    }
}
