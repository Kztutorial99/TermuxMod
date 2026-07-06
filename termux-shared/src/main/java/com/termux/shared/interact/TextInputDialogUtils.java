package com.termux.shared.interact;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.termux.shared.R;

public final class TextInputDialogUtils {

    public interface TextSetListener {
        void onTextSet(String text);
    }

    public static void textInput(Activity activity, int titleText, String initialText,
                                 int positiveButtonText, final TextSetListener onPositive,
                                 int neutralButtonText, final TextSetListener onNeutral,
                                 int negativeButtonText, final TextSetListener onNegative,
                                 final DialogInterface.OnDismissListener onDismiss) {

        BottomSheetDialog sheet = new BottomSheetDialog(activity, R.style.TermuxBottomSheetStyle);

        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.bottom_sheet_text_input, null);

        TextView titleView = view.findViewById(R.id.sheet_title);
        if (titleView != null)
            titleView.setText(titleText);

        TextInputEditText input = view.findViewById(R.id.text_input);
        if (input != null && initialText != null) {
            input.setText(initialText);
            Selection.setSelection(input.getText(), initialText.length());
        }

        MaterialButton btnPositive = view.findViewById(R.id.btn_positive);
        if (btnPositive != null) {
            btnPositive.setText(positiveButtonText);
            btnPositive.setOnClickListener(v -> {
                String text = (input != null && input.getText() != null) ? input.getText().toString() : "";
                onPositive.onTextSet(text);
                sheet.dismiss();
            });
        }

        MaterialButton btnNeutral = view.findViewById(R.id.btn_neutral);
        if (btnNeutral != null) {
            if (onNeutral != null) {
                btnNeutral.setVisibility(View.VISIBLE);
                btnNeutral.setText(neutralButtonText);
                btnNeutral.setOnClickListener(v -> {
                    String text = (input != null && input.getText() != null) ? input.getText().toString() : "";
                    onNeutral.onTextSet(text);
                    sheet.dismiss();
                });
            } else {
                btnNeutral.setVisibility(View.GONE);
            }
        }

        MaterialButton btnNegative = view.findViewById(R.id.btn_negative);
        if (btnNegative != null) {
            if (onNegative == null) {
                btnNegative.setText(android.R.string.cancel);
                btnNegative.setOnClickListener(v -> sheet.dismiss());
            } else {
                btnNegative.setText(negativeButtonText);
                btnNegative.setOnClickListener(v -> {
                    String text = (input != null && input.getText() != null) ? input.getText().toString() : "";
                    onNegative.onTextSet(text);
                    sheet.dismiss();
                });
            }
        }

        if (onDismiss != null)
            sheet.setOnDismissListener(onDismiss::onDismiss);

        sheet.setContentView(view);
        sheet.setCanceledOnTouchOutside(false);

        sheet.setOnShowListener(d -> {
            if (input != null) {
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        sheet.show();
    }

}
