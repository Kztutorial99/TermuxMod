package com.termux.app.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.termux.R;

/** Adapter untuk halaman onboarding di ViewPager. */
class OnboardingPageAdapter extends PagerAdapter {

    private final String[] mTitles;
    private final String[] mBodies;

    OnboardingPageAdapter(String[] titles, String[] bodies) {
        mTitles = titles;
        mBodies = bodies;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = LayoutInflater.from(container.getContext())
            .inflate(R.layout.item_onboarding_page, container, false);
        ((TextView) v.findViewById(R.id.onboarding_page_title)).setText(mTitles[position]);
        ((TextView) v.findViewById(R.id.onboarding_page_body)).setText(mBodies[position]);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
