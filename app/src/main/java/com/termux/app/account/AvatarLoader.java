package com.termux.app.account;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Minimal avatar downloader so no image loading library needs to be added.
 * The bitmap is fetched on a background thread and applied on the main thread.
 */
public final class AvatarLoader {

    private AvatarLoader() {
    }

    public static void load(@Nullable final String url, @NonNull ImageView target) {
        if (url == null || url.isEmpty()) return;

        final WeakReference<ImageView> targetRef = new WeakReference<>(target);
        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            Bitmap bitmap = fetch(url);
            if (bitmap == null) return;
            handler.post(() -> {
                ImageView imageView = targetRef.get();
                if (imageView != null) imageView.setImageBitmap(bitmap);
            });
        }).start();
    }

    @Nullable
    private static Bitmap fetch(@NonNull String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

}
