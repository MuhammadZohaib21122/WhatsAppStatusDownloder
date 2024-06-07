package com.example.whatsappstatusdownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final Context context;
    private final ArrayList<StoryModel> filesList;

    public StoryAdapter(Context context, ArrayList<StoryModel> filesList) {
        this.context = context;
        this.filesList = filesList;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.card_row, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final StoryModel file = filesList.get(position);

        if (file.getUri().toString().endsWith(".mp4")) {
            holder.playIcon.setVisibility(View.VISIBLE);
        } else {
            holder.playIcon.setVisibility(View.INVISIBLE);
        }

        Glide.with(context)
                .load(file.getUri())
                .into(holder.saveImage);

        holder.downloadID.setOnClickListener(v -> {
            checkFolder();

            final String path = file.getPath();
            final File sourceFile = new File(path);

            String destPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.SAVE_FOLDER_NAME;
            File destFile = new File(destPath);

            try {
                FileUtils.copyFileToDirectory(sourceFile, destFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaScannerConnection.scanFile(
                    context,
                    new String[]{destPath + file.getFilename()},
                    new String[]{"*/*"},
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    }
            );

            Toast.makeText(context, "Saved to:" + destPath + file.getFilename(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    private void checkFolder() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.SAVE_FOLDER_NAME;
        File dir = new File(path);

        if (!dir.exists()) {
            boolean isDirectoryCreated = dir.mkdirs();
            if (isDirectoryCreated) {
                Log.d("Folder", "Created Successfully");
            }
        } else {
            Log.d("Folder", "Already Exists");
        }
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView playIcon, downloadID, saveImage;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            saveImage = itemView.findViewById(R.id.mainImageView);
            playIcon = itemView.findViewById(R.id.playButtonImage);
            downloadID = itemView.findViewById(R.id.downloadID);
        }
    }
}
