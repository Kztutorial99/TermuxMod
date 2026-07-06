package com.termux.app.file;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.termux.R;

import java.io.File;

/**
 * BottomSheet kustom untuk aksi share output / open-with dari hasil command.
 * Menggantikan chooser sistem polos bila relevan.
 */
public class ShareOptionsBottomSheet {

    private final BottomSheetDialog mSheet;

    public ShareOptionsBottomSheet(Context context, String text, File file) {
        mSheet = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_share_options, null);
        mSheet.setContentView(view);

        view.findViewById(R.id.btn_share_text).setOnClickListener(v -> {
            mSheet.dismiss();
            if (text == null) return;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            context.startActivity(Intent.createChooser(shareIntent, null));
        });

        view.findViewById(R.id.btn_share_file).setOnClickListener(v -> {
            mSheet.dismiss();
            if (file == null || !file.exists()) return;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, null));
        });

        view.findViewById(R.id.btn_open_with).setOnClickListener(v -> {
            mSheet.dismiss();
            if (file == null || !file.exists()) return;
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setData(FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", file));
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(openIntent, null));
        });
    }

    /** Tampilkan BottomSheet share options. */
    public void show() {
        mSheet.show();
    }
}
