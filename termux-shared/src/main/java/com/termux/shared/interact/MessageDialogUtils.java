package com.termux.shared.interact;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.termux.shared.R;
import com.termux.shared.logger.Logger;

public class MessageDialogUtils {

    /**
     * Show a message in a BottomSheetDialog
     *
     * @param context The {@link Context} to use to start the dialog.
     * @param titleText The title text of the dialog.
     * @param messageText The message text of the dialog.
     * @param onDismiss The {@link DialogInterface.OnDismissListener} to run when dialog is dismissed.
     */
    public static void showMessage(Context context, String titleText, String messageText, final DialogInterface.OnDismissListener onDismiss) {
        showMessage(context, titleText, messageText, null, null, null, null, onDismiss);
    }

    /**
     * Show a message in a BottomSheetDialog
     *
     * @param context The {@link Context} to use to start the dialog.
     * @param titleText The title text of the dialog.
     * @param messageText The message text of the dialog.
     * @param positiveText The positive button text of the dialog.
     * @param onPositiveButton The listener to run when positive button is pressed.
     * @param negativeText The negative button text. If {@code null}, negative button is hidden.
     * @param onNegativeButton The listener to run when negative button is pressed.
     * @param onDismiss The {@link DialogInterface.OnDismissListener} to run when dialog is dismissed.
     */
    public static void showMessage(Context context, String titleText, String messageText,
                                   String positiveText,
                                   final DialogInterface.OnClickListener onPositiveButton,
                                   String negativeText,
                                   final DialogInterface.OnClickListener onNegativeButton,
                                   final DialogInterface.OnDismissListener onDismiss) {

        BottomSheetDialog sheet = new BottomSheetDialog(context, R.style.TermuxBottomSheetStyle);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.bottom_sheet_message, null);

        TextView titleView = view.findViewById(R.id.dialog_title);
        if (titleView != null && titleText != null)
            titleView.setText(titleText);

        TextView messageView = view.findViewById(R.id.dialog_message);
        if (messageView != null && messageText != null)
            messageView.setText(messageText);

        MaterialButton btnPositive = view.findViewById(R.id.btn_positive);
        if (btnPositive != null) {
            String posLabel = (positiveText != null) ? positiveText : context.getString(android.R.string.ok);
            btnPositive.setText(posLabel);
            btnPositive.setOnClickListener(v -> {
                if (onPositiveButton != null)
                    onPositiveButton.onClick(sheet, DialogInterface.BUTTON_POSITIVE);
                sheet.dismiss();
            });
        }

        MaterialButton btnNegative = view.findViewById(R.id.btn_negative);
        if (btnNegative != null) {
            if (negativeText != null) {
                btnNegative.setVisibility(View.VISIBLE);
                btnNegative.setText(negativeText);
                btnNegative.setOnClickListener(v -> {
                    if (onNegativeButton != null)
                        onNegativeButton.onClick(sheet, DialogInterface.BUTTON_NEGATIVE);
                    sheet.dismiss();
                });
            } else {
                btnNegative.setVisibility(View.GONE);
            }
        }

        if (onDismiss != null)
            sheet.setOnDismissListener(onDismiss::onDismiss);

        sheet.setContentView(view);
        sheet.setCanceledOnTouchOutside(false);
        sheet.show();
    }

    public static void exitAppWithErrorMessage(Context context, String titleText, String messageText) {
        showMessage(context, titleText, messageText, dialog -> System.exit(0));
    }

}
