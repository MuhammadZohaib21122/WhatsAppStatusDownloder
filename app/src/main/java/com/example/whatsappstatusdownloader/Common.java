package com.example.whatsappstatusdownloader;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.example.videodownloader.R;
import com.example.videodownloader.editor.WhatsappStatus.Models.Status;
import com.example.videodownloader.editor.WhatsappStatus.Models.StatusDocFile;
import com.example.videodownloader.editor.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Common {

    static final int MINI_KIND = 1;
    static final int MICRO_KIND = 3;

    public static final int GRID_COUNT = 2;

    private static final String CHANNEL_NAME = "VideoDownloader";

    public static final File STATUS_DIRECTORY = new File(Environment.getExternalStorageDirectory() +
            File.separator + "WhatsApp/Media/.Statuses");

    public static final File STATUS_DIRECTORY_NEW = new File(Environment.getExternalStorageDirectory() +
            File.separator + "Android/media/com.whatsapp/WhatsApp/Media/.Statuses");

    public static String APP_DIR = Utils.RootDirectoryWhatsappShow.getAbsolutePath();

    public static void copyFile(Status status, Context context, ConstraintLayout container) {

        if (getSavedFile(status.getTitle())) {
            Snackbar.make(container, "Already downloaded.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        File file = new File(Common.APP_DIR);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Snackbar.make(container, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                return;
            }
        }

        String fileName = status.getTitle();
        File destFile = new File(file + File.separator + fileName);

        try {
            FileUtils.copyFile(status.getFile(), destFile);
            destFile.setLastModified(System.currentTimeMillis());
            new SingleMediaScanner(context, file);
            showNotification(context, container, destFile, status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileDoc(StatusDocFile status, Context context, ConstraintLayout container) {

        if (getSavedFile(status.getTitle())) {
            Snackbar.make(container, "Already downloaded.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        File file = new File(Common.APP_DIR);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Snackbar.make(container, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                return;
            }
        }

        String fileName = status.getTitle();
        File destFile = new File(file + File.separator + fileName);

        try {
            // FileUtils.copyFile(status.getFile(), destFile); // Ensure this is implemented correctly
            destFile.setLastModified(System.currentTimeMillis());
            new SingleMediaScanner(context, file);
            showNotification(context, container, destFile, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showNotification(Context context, ConstraintLayout container, File destFile, Status status) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(context);
        }

        Uri data = FileProvider.getUriForFile(context, "com.example.videodownloader", new File(destFile.getAbsolutePath()));
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (status.isVideo()) {
            intent.setDataAndType(data, "video/*");
        } else {
            intent.setDataAndType(data, "image/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_NAME);

        notification.setSmallIcon(R.drawable.ic_download_ic)
                .setContentTitle(destFile.getName())
                .setContentText("File Saved to " + APP_DIR)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(new Random().nextInt(), notification.build());
        }

        Snackbar.make(container, "Saved successfully", Snackbar.LENGTH_LONG).show();
    }

    private static void showNotification(Context context, ConstraintLayout container, File destFile, StatusDocFile status) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(context);
        }

        Uri data = FileProvider.getUriForFile(context, "com.example.videodownloader", new File(destFile.getAbsolutePath()));
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (status.isVideo()) {
            intent.setDataAndType(data, "video/*");
        } else {
            intent.setDataAndType(data, "image/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_NAME);

        notification.setSmallIcon(R.drawable.ic_download_ic)
                .setContentTitle(destFile.getName())
                .setContentText("File Saved to " + APP_DIR)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(new Random().nextInt(), notification.build());
        }

        Snackbar.make(container, "Saved successfully", Snackbar.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void makeNotificationChannel(Context context) {

        NotificationChannel channel = new NotificationChannel(Common.CHANNEL_NAME, "Saved", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static boolean getSavedFile(String name) {

        final File app_dir = new File(Common.APP_DIR);

        Log.e("checkinggsaved", "Saved Time " + app_dir.getAbsolutePath() + "");
        if (app_dir.exists()) {
            File[] savedFiles = app_dir.listFiles();

            if (savedFiles != null && savedFiles.length > 0) {
                Arrays.sort(savedFiles);
                for (File file : savedFiles) {
                    Status status = new Status(file, file.getName(), file.getAbsolutePath());
                    if (name.equals(status.getTitle())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
