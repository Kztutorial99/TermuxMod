package com.termux.app.terminal;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.termux.R;

/**
 * Overlay pencarian teks di scrollback terminal.
 * Tampil/hilang dengan animasi fade, highlight hasil dengan warna aksen.
 */
public class TerminalSearchOverlay {

    private final View mOverlayView;
    private final EditText mSearchInput;
    private final TextView mResultCount;
    private final ImageButton mBtnPrev;
    private final ImageButton mBtnNext;
    private final ImageButton mBtnClose;

    private String mLastQuery = "";
    private boolean mVisible = false;

    public TerminalSearchOverlay(View overlayView) {
        mOverlayView = overlayView;

        mSearchInput = overlayView.findViewById(R.id.search_input);
        mResultCount = overlayView.findViewById(R.id.search_result_count);
        mBtnPrev = overlayView.findViewById(R.id.btn_search_prev);
        mBtnNext = overlayView.findViewById(R.id.btn_search_next);
        mBtnClose = overlayView.findViewById(R.id.btn_search_close);

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                onQueryChanged(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        mBtnPrev.setOnClickListener(v -> searchPrev());
        mBtnNext.setOnClickListener(v -> searchNext());
        mBtnClose.setOnClickListener(v -> hide());
    }

    /** Tampilkan overlay dengan animasi fade-in. */
    public void show() {
        if (mVisible) {
            mSearchInput.requestFocus();
            return;
        }
        mVisible = true;
        mOverlayView.setVisibility(View.VISIBLE);
        mOverlayView.animate().alpha(1f).setDuration(200).start();
        mSearchInput.requestFocus();
    }

    /** Sembunyikan overlay dengan animasi fade-out. */
    public void hide() {
        if (!mVisible) return;
        mVisible = false;
        mOverlayView.animate().alpha(0f).setDuration(150)
            .withEndAction(() -> mOverlayView.setVisibility(View.GONE)).start();
        mSearchInput.setText("");
        mLastQuery = "";
        clearSearch();
    }

    /** Apakah overlay sedang tampil. */
    public boolean isVisible() {
        return mVisible;
    }

    private void onQueryChanged(String query) {
        mLastQuery = query;
        if (query.isEmpty()) {
            mResultCount.setVisibility(View.GONE);
            mBtnPrev.setVisibility(View.GONE);
            mBtnNext.setVisibility(View.GONE);
            clearSearch();
            return;
        }
        if (mSearchCallback != null) mSearchCallback.onSearch(query, false);
        mBtnPrev.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.VISIBLE);
    }

    private void searchNext() {
        if (mLastQuery.isEmpty()) return;
        // Hook: TerminalEmulator search forward — diimplementasikan melalui mSearchCallback
        if (mSearchCallback != null) mSearchCallback.onSearch(mLastQuery, false);
    }

    private void searchPrev() {
        if (mLastQuery.isEmpty()) return;
        // Hook: TerminalEmulator search backward
        if (mSearchCallback != null) mSearchCallback.onSearch(mLastQuery, true);
    }

    private void clearSearch() {
        if (mSearchCallback != null) mSearchCallback.onSearch("", false);
    }

    /** Callback untuk menghubungkan overlay ke implementasi pencarian di TerminalView/Emulator. */
    public interface SearchCallback {
        void onSearch(String query, boolean searchUp);
    }

    private SearchCallback mSearchCallback;

    /** Set callback pencarian agar overlay bisa mengirim query ke TerminalEmulator. */
    public void setSearchCallback(SearchCallback callback) {
        mSearchCallback = callback;
    }
}
