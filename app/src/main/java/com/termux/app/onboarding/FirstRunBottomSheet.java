package com.termux.app.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.termux.R;

/**
 * BottomSheet onboarding untuk pengguna baru.
 * Ditampilkan sekali via flag SharedPreferences.
 * Tidak mengubah flag atau preference yang sudah ada.
 */
public class FirstRunBottomSheet {

    private static final String PREFS_NAME = "termuxmod_onboarding";
    private static final String KEY_FIRST_RUN_SHOWN = "first_run_shown";

    /** Cek apakah onboarding perlu ditampilkan dan tampilkan jika ya. */
    public static void showIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_FIRST_RUN_SHOWN, false)) return;

        prefs.edit().putBoolean(KEY_FIRST_RUN_SHOWN, true).apply();
        new FirstRunBottomSheet(context).show();
    }

    private final BottomSheetDialog mSheet;

    private FirstRunBottomSheet(Context context) {
        mSheet = new BottomSheetDialog(context);
        View root = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_first_run, null);
        mSheet.setContentView(root);
        mSheet.setCancelable(true);

        ViewPager pager = root.findViewById(R.id.onboarding_pager);
        LinearLayout dotsContainer = root.findViewById(R.id.onboarding_dots);
        MaterialButton btnNext = root.findViewById(R.id.btn_onboarding_next);
        MaterialButton btnSkip = root.findViewById(R.id.btn_onboarding_skip);

        String[] titles = {
            context.getString(R.string.onboarding_title_sessions),
            context.getString(R.string.onboarding_title_extrakeys),
            context.getString(R.string.onboarding_title_settings),
        };
        String[] bodies = {
            context.getString(R.string.onboarding_body_sessions),
            context.getString(R.string.onboarding_body_extrakeys),
            context.getString(R.string.onboarding_body_settings),
        };

        pager.setAdapter(new OnboardingPageAdapter(titles, bodies));
        setupDots(context, dotsContainer, titles.length);
        updateDots(dotsContainer, 0);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int pos, float offset, int offsetPx) {}
            @Override public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageSelected(int position) {
                updateDots(dotsContainer, position);
                boolean isLast = position == titles.length - 1;
                btnNext.setText(isLast
                    ? context.getString(R.string.onboarding_btn_start)
                    : context.getString(R.string.onboarding_btn_next));
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = pager.getCurrentItem();
            if (current < titles.length - 1) {
                pager.setCurrentItem(current + 1, true);
            } else {
                mSheet.dismiss();
            }
        });

        btnSkip.setOnClickListener(v -> mSheet.dismiss());
    }

    private void setupDots(Context context, LinearLayout container, int count) {
        container.removeAllViews();
        int sizePx = (int) (8 * context.getResources().getDisplayMetrics().density);
        int marginPx = (int) (4 * context.getResources().getDisplayMetrics().density);
        for (int i = 0; i < count; i++) {
            View dot = new View(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            lp.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            container.addView(dot);
        }
    }

    private void updateDots(LinearLayout container, int activeIndex) {
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setBackgroundResource(
                i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void show() {
        mSheet.show();
    }
}
