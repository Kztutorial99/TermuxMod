package com.termux.app.terminal.io;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.shared.terminal.io.extrakeys.ExtraKeysView;

public class TerminalToolbarViewPager {

    public static class PageAdapter extends PagerAdapter {

        final TermuxActivity mActivity;

        public PageAdapter(TermuxActivity activity) {
            this.mActivity = activity;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            View layout = inflater.inflate(R.layout.view_terminal_toolbar_extra_keys, collection, false);
            ExtraKeysView extraKeysView = layout.findViewById(R.id.terminal_toolbar_extra_keys);
            extraKeysView.setExtraKeysViewClient(new TermuxTerminalExtraKeys(mActivity.getTerminalView(),
                mActivity.getTermuxTerminalViewClient(), mActivity.getTermuxTerminalSessionClient()));
            extraKeysView.setButtonTextAllCaps(mActivity.getProperties().shouldExtraKeysTextBeAllCaps());
            mActivity.setExtraKeysView(extraKeysView);
            extraKeysView.reload(mActivity.getProperties().getExtraKeysInfo());

            if (mActivity.getProperties().isUsingFullScreen() && mActivity.getProperties().isUsingFullScreenWorkAround()) {
                FullScreenWorkAround.apply(mActivity);
            }

            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

    }



    public static class OnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        final TermuxActivity mActivity;
        final ViewPager mTerminalToolbarViewPager;

        public OnPageChangeListener(TermuxActivity activity, ViewPager viewPager) {
            this.mActivity = activity;
            this.mTerminalToolbarViewPager = viewPager;
        }

        @Override
        public void onPageSelected(int position) {
            mActivity.getTerminalView().requestFocus();
        }

    }

}
