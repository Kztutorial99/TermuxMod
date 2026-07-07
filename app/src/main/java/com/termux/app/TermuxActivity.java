package com.termux.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.termux.app.activities.DeveloperInfoActivity;
import com.termux.app.activities.TerminalAppearanceActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;


import com.termux.R;
import com.termux.app.terminal.TermuxActivityRootView;
import com.termux.shared.activities.ReportActivity;
import com.termux.shared.packages.PermissionUtils;
import com.termux.shared.data.DataUtils;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_ACTIVITY;
import com.termux.app.activities.HelpActivity;
import com.termux.app.activities.SettingsActivity;
import com.termux.shared.settings.preferences.TermuxAppSharedPreferences;
import com.termux.app.terminal.TermuxSessionsListViewController;
import com.termux.app.terminal.io.TerminalToolbarViewPager;
import com.termux.app.terminal.TermuxTerminalSessionClient;
import com.termux.app.terminal.TermuxTerminalViewClient;
import com.termux.shared.terminal.io.extrakeys.ExtraKeysView;
import com.termux.app.settings.properties.TermuxAppSharedProperties;
import com.termux.shared.interact.TextInputDialogUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxUtils;
import com.termux.shared.view.ViewUtils;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;
import com.termux.app.utils.CrashUtils;
import com.termux.view.TerminalView;
import com.termux.view.TerminalViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

/**
 * A terminal emulator activity.
 * <p/>
 * See
 * <ul>
 * <li>http://www.mongrel-phones.com.au/default/how_to_make_a_local_service_and_bind_to_it_in_android</li>
 * <li>https://code.google.com/p/android/issues/detail?id=6426</li>
 * </ul>
 * about memory leaks.
 */
public final class TermuxActivity extends Activity implements ServiceConnection {

    /**
     * The connection to the {@link TermuxService}. Requested in {@link #onCreate(Bundle)} with a call to
     * {@link #bindService(Intent, ServiceConnection, int)}, and obtained and stored in
     * {@link #onServiceConnected(ComponentName, IBinder)}.
     */
    TermuxService mTermuxService;

    /**
     * The {@link TerminalView} shown in  {@link TermuxActivity} that displays the terminal.
     */
    TerminalView mTerminalView;

    /**
     *  The {@link TerminalViewClient} interface implementation to allow for communication between
     *  {@link TerminalView} and {@link TermuxActivity}.
     */
    TermuxTerminalViewClient mTermuxTerminalViewClient;

    /**
     *  The {@link TerminalSessionClient} interface implementation to allow for communication between
     *  {@link TerminalSession} and {@link TermuxActivity}.
     */
    TermuxTerminalSessionClient mTermuxTerminalSessionClient;

    /**
     * Termux app shared preferences manager.
     */
    private TermuxAppSharedPreferences mPreferences;

    /**
     * Termux app shared properties manager, loaded from termux.properties
     */
    private TermuxAppSharedProperties mProperties;

    /**
     * The root view of the {@link TermuxActivity}.
     */
    TermuxActivityRootView mTermuxActivityRootView;

    /**
     * The space at the bottom of {@link @mTermuxActivityRootView} of the {@link TermuxActivity}.
     */
    View mTermuxActivityBottomSpaceView;

    /**
     * The terminal extra keys view.
     */
    ExtraKeysView mExtraKeysView;

    /**
     * The termux sessions list controller.
     */
    TermuxSessionsListViewController mTermuxSessionListViewController;

    /**
     * The {@link TermuxActivity} broadcast receiver for various things like terminal style configuration changes.
     */
    private final BroadcastReceiver mTermuxActivityBroadcastReceiver = new TermuxActivityBroadcastReceiver();

    /**
     * The last Snackbar shown, used to dismiss current before showing new.
     */
    Snackbar mLastSnackbar;

    /**
     * If between onResume() and onStop(). Note that only one session is in the foreground of the terminal view at the
     * time, so if the session causing a change is not in the foreground it should probably be treated as background.
     */
    private boolean mIsVisible;

    /**
     * If onResume() was called after onCreate().
     */
    private boolean isOnResumeAfterOnCreate = false;

    /**
     * The {@link TermuxActivity} is in an invalid state and must not be run.
     */
    private boolean mIsInvalidState;

    private int mNavBarHeight;

    private int mTerminalToolbarDefaultHeight;

    // Bottom navigation
    private BottomNavigationView mBottomNav;
    private FrameLayout mFilesContainer;
    private FrameLayout mPackagesContainer;
    private FrameLayout mToolsContainer;
    private int mCurrentTabId = R.id.nav_terminal;

    // Files tab state
    private File mCurrentDirectory;
    private File mHomeDirectory;
    private FileAdapter mFileAdapter;

    // Packages tab state
    private PackageAdapter mPackageAdapter;

    // Tools tab state
    private ToolAdapter mToolAdapter;

    // Handler untuk auto-refresh status paket
    private android.os.Handler mPkgRefreshHandler;
    private Runnable mPkgRefreshRunnable;


    private static final int CONTEXT_MENU_SELECT_URL_ID = 0;
    private static final int CONTEXT_MENU_SHARE_TRANSCRIPT_ID = 1;
    private static final int CONTEXT_MENU_SHARE_SELECTED_TEXT = 10;
    private static final int CONTEXT_MENU_AUTOFILL_USERNAME = 11;
    private static final int CONTEXT_MENU_AUTOFILL_PASSWORD = 2;
    private static final int CONTEXT_MENU_RESET_TERMINAL_ID = 3;
    private static final int CONTEXT_MENU_KILL_PROCESS_ID = 4;
    private static final int CONTEXT_MENU_STYLING_ID = 5;
    private static final int CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON = 6;
    private static final int CONTEXT_MENU_HELP_ID = 7;
    private static final int CONTEXT_MENU_SETTINGS_ID = 8;
    private static final int CONTEXT_MENU_REPORT_ID = 9;

    private static final String ARG_TERMINAL_TOOLBAR_TEXT_INPUT = "terminal_toolbar_text_input";

    private static final String LOG_TAG = "TermuxActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Logger.logDebug(LOG_TAG, "onCreate");
        isOnResumeAfterOnCreate = true;

        // Check if a crash happened on last run of the app and show a
        // notification with the crash details if it did
        CrashUtils.notifyAppCrashOnLastRun(this, LOG_TAG);

        // Delete ReportInfo serialized object files from cache older than 14 days
        ReportActivity.deleteReportInfoFilesOlderThanXDays(this, 14, false);

        // Load termux shared properties
        mProperties = new TermuxAppSharedProperties(this);

        setActivityTheme();

        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);

        setContentView(R.layout.activity_termux);

        // Load termux shared preferences
        // This will also fail if TermuxConstants.TERMUX_PACKAGE_NAME does not equal applicationId
        mPreferences = TermuxAppSharedPreferences.build(this, true);
        if (mPreferences == null) {
            // An AlertDialog should have shown to kill the app, so we don't continue running activity code
            mIsInvalidState = true;
            return;
        }

        setMargins();

        mTermuxActivityRootView = findViewById(R.id.activity_termux_root_view);
        mTermuxActivityRootView.setActivity(this);
        mTermuxActivityBottomSpaceView = findViewById(R.id.activity_termux_bottom_space_view);
        mTermuxActivityRootView.setOnApplyWindowInsetsListener(new TermuxActivityRootView.WindowInsetsListener());

        View content = findViewById(android.R.id.content);
        content.setOnApplyWindowInsetsListener((v, insets) -> {
            mNavBarHeight = insets.getSystemWindowInsetBottom();
            return insets;
        });

        if (mProperties.isUsingFullScreen()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setDrawerTheme();

        setDrawerParallaxEffect();

        setTermuxTerminalViewAndClients();

        setTerminalToolbarView(savedInstanceState);

        setSettingsButtonView();

        setNewSessionButtonView();

        setToggleKeyboardView();

        setupCustomToolbar();
        setupBottomNavigation();
        setupFilesTab();
        setupPackagesTab();
        setupToolsTab();
        setupRightDrawer();

        registerForContextMenu(mTerminalView);

        // Start the {@link TermuxService} and make it run regardless of who is bound to it
        Intent serviceIntent = new Intent(this, TermuxService.class);
        startService(serviceIntent);

        // Attempt to bind to the service, this will call the {@link #onServiceConnected(ComponentName, IBinder)}
        // callback if it succeeds.
        if (!bindService(serviceIntent, this, 0))
            throw new RuntimeException("bindService() failed");

        // Send the {@link TermuxConstants#BROADCAST_TERMUX_OPENED} broadcast to notify apps that Termux
        // app has been opened.
        TermuxUtils.sendTermuxOpenedBroadcast(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        Logger.logDebug(LOG_TAG, "onStart");

        if (mIsInvalidState) return;

        mIsVisible = true;

        if (mTermuxTerminalSessionClient != null)
            mTermuxTerminalSessionClient.onStart();

        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onStart();

        if (mPreferences.isTerminalMarginAdjustmentEnabled())
            addTermuxActivityRootViewGlobalLayoutListener();

        registerTermuxActivityBroadcastReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();

        Logger.logVerbose(LOG_TAG, "onResume");

        if (mIsInvalidState) return;

        if (mTermuxTerminalSessionClient != null)
            mTermuxTerminalSessionClient.onResume();

        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onResume();

        isOnResumeAfterOnCreate = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        Logger.logDebug(LOG_TAG, "onStop");

        if (mIsInvalidState) return;

        mIsVisible = false;

        if (mTermuxTerminalSessionClient != null)
            mTermuxTerminalSessionClient.onStop();

        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onStop();

        removeTermuxActivityRootViewGlobalLayoutListener();

        unregisterTermuxActivityBroadcastReceiever();
        getDrawer().closeDrawers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Logger.logDebug(LOG_TAG, "onDestroy");

        if (mIsInvalidState) return;

        if (mTermuxService != null) {
            // Do not leave service and session clients with references to activity.
            mTermuxService.unsetTermuxTerminalSessionClient();
            mTermuxService = null;
        }

        try {
            unbindService(this);
        } catch (Exception e) {
            // ignore.
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveTerminalToolbarTextInput(savedInstanceState);
    }





    /**
     * Part of the {@link ServiceConnection} interface. The service is bound with
     * {@link #bindService(Intent, ServiceConnection, int)} in {@link #onCreate(Bundle)} which will cause a call to this
     * callback method.
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

        Logger.logDebug(LOG_TAG, "onServiceConnected");

        mTermuxService = ((TermuxService.LocalBinder) service).service;

        setTermuxSessionsListView();

        if (mTermuxService.isTermuxSessionsEmpty()) {
            if (mIsVisible) {
                TermuxInstaller.setupBootstrapIfNeeded(TermuxActivity.this, () -> {
                    if (mTermuxService == null) return; // Activity might have been destroyed.
                    try {
                        Bundle bundle = getIntent().getExtras();
                        boolean launchFailsafe = false;
                        if (bundle != null) {
                            launchFailsafe = bundle.getBoolean(TERMUX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
                        }
                        mTermuxTerminalSessionClient.addNewSession(launchFailsafe, null);
                    } catch (WindowManager.BadTokenException e) {
                        // Activity finished - ignore.
                    }
                });
            } else {
                // The service connected while not in foreground - just bail out.
                finishActivityIfNotFinishing();
            }
        } else {
            Intent i = getIntent();
            if (i != null && Intent.ACTION_RUN.equals(i.getAction())) {
                // Android 7.1 app shortcut from res/xml/shortcuts.xml.
                boolean isFailSafe = i.getBooleanExtra(TERMUX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
                mTermuxTerminalSessionClient.addNewSession(isFailSafe, null);
            } else {
                mTermuxTerminalSessionClient.setCurrentSession(mTermuxTerminalSessionClient.getCurrentStoredSessionOrLast());
            }
        }

        // Update the {@link TerminalSession} and {@link TerminalEmulator} clients.
        mTermuxService.setTermuxTerminalSessionClient(mTermuxTerminalSessionClient);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        Logger.logDebug(LOG_TAG, "onServiceDisconnected");

        // Respect being stopped from the {@link TermuxService} notification action.
        finishActivityIfNotFinishing();
    }





    private void setActivityTheme() {
        if (mProperties.isUsingBlackUI()) {
            this.setTheme(R.style.Theme_Termux_Black);
        } else {
            this.setTheme(R.style.Theme_Termux);
        }
    }

    private void setDrawerTheme() {
        if (mProperties.isUsingBlackUI()) {
            findViewById(R.id.left_drawer).setBackgroundColor(ContextCompat.getColor(this,
                android.R.color.background_dark));
            ((ImageButton) findViewById(R.id.settings_button)).setColorFilter(Color.WHITE);
        }
    }

    /** Applies a subtle parallax motion to the terminal content while the drawer slides,
     * so opening/closing the drawer feels smooth instead of an abrupt overlay. */
    private void setDrawerParallaxEffect() {
        DrawerLayout drawer = getDrawer();
        final View terminalContent = findViewById(R.id.terminal_view);
        if (drawer == null || terminalContent == null) return;

        final float parallaxFactor = 0.25f;
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                terminalContent.setTranslationX(drawerView.getWidth() * slideOffset * parallaxFactor);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // Lepas hardware layer setelah animasi selesai
                terminalContent.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                terminalContent.setTranslationX(0f);
                terminalContent.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING || newState == DrawerLayout.STATE_DRAGGING) {
                    // Aktifkan hardware layer saat animasi drawer berjalan agar parallax smooth
                    terminalContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            }
        });
    }

    private void setMargins() {
        ConstraintLayout relativeLayout = findViewById(R.id.activity_termux_root_relative_layout);
        int marginHorizontal = mProperties.getTerminalMarginHorizontal();
        int marginVertical = mProperties.getTerminalMarginVertical();
        ViewUtils.setLayoutMarginsInDp(relativeLayout, marginHorizontal, marginVertical, marginHorizontal, marginVertical);
    }



    public void addTermuxActivityRootViewGlobalLayoutListener() {
        getTermuxActivityRootView().getViewTreeObserver().addOnGlobalLayoutListener(getTermuxActivityRootView());
    }

    public void removeTermuxActivityRootViewGlobalLayoutListener() {
        if (getTermuxActivityRootView() != null)
            getTermuxActivityRootView().getViewTreeObserver().removeOnGlobalLayoutListener(getTermuxActivityRootView());
    }



    private void setTermuxTerminalViewAndClients() {
        // Set termux terminal view and session clients
        mTermuxTerminalSessionClient = new TermuxTerminalSessionClient(this);
        mTermuxTerminalViewClient = new TermuxTerminalViewClient(this, mTermuxTerminalSessionClient);

        // Set termux terminal view
        mTerminalView = findViewById(R.id.terminal_view);
        mTerminalView.setTerminalViewClient(mTermuxTerminalViewClient);

        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onCreate();

        if (mTermuxTerminalSessionClient != null)
            mTermuxTerminalSessionClient.onCreate();
    }

    private void setTermuxSessionsListView() {
        ListView termuxSessionsListView = findViewById(R.id.terminal_sessions_list);
        mTermuxSessionListViewController = new TermuxSessionsListViewController(this, mTermuxService.getTermuxSessions());
        termuxSessionsListView.setAdapter(mTermuxSessionListViewController);
        termuxSessionsListView.setOnItemClickListener(mTermuxSessionListViewController);
        termuxSessionsListView.setOnItemLongClickListener(mTermuxSessionListViewController);
    }



    private void setTerminalToolbarView(Bundle savedInstanceState) {
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (mPreferences.shouldShowTerminalToolbar()) terminalToolbarViewPager.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = terminalToolbarViewPager.getLayoutParams();
        mTerminalToolbarDefaultHeight = layoutParams.height;

        setTerminalToolbarHeight();

        String savedTextInput = null;
        if (savedInstanceState != null)
            savedTextInput = savedInstanceState.getString(ARG_TERMINAL_TOOLBAR_TEXT_INPUT);

        terminalToolbarViewPager.setAdapter(new TerminalToolbarViewPager.PageAdapter(this, savedTextInput));
        terminalToolbarViewPager.addOnPageChangeListener(new TerminalToolbarViewPager.OnPageChangeListener(this, terminalToolbarViewPager));
    }

    private void setTerminalToolbarHeight() {
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (terminalToolbarViewPager == null) return;

        ViewGroup.LayoutParams layoutParams = terminalToolbarViewPager.getLayoutParams();
        layoutParams.height = (int) Math.round(mTerminalToolbarDefaultHeight *
            (mProperties.getExtraKeysInfo() == null ? 0 : mProperties.getExtraKeysInfo().getMatrix().length) *
            mProperties.getTerminalToolbarHeightScaleFactor());
        terminalToolbarViewPager.setLayoutParams(layoutParams);
    }

    public void toggleTerminalToolbar() {
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (terminalToolbarViewPager == null) return;

        final boolean showNow = mPreferences.toogleShowTerminalToolbar();
        showToast((showNow ? getString(R.string.msg_enabling_terminal_toolbar) : getString(R.string.msg_disabling_terminal_toolbar)), true);
        terminalToolbarViewPager.setVisibility(showNow ? View.VISIBLE : View.GONE);
        if (showNow && isTerminalToolbarTextInputViewSelected()) {
            // Focus the text input view if just revealed.
            findViewById(R.id.terminal_toolbar_text_input).requestFocus();
        }
    }

    private void saveTerminalToolbarTextInput(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        final EditText textInputView =  findViewById(R.id.terminal_toolbar_text_input);
        if (textInputView != null) {
            String textInput = textInputView.getText().toString();
            if (!textInput.isEmpty()) savedInstanceState.putString(ARG_TERMINAL_TOOLBAR_TEXT_INPUT, textInput);
        }
    }



    private void setSettingsButtonView() {
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void setNewSessionButtonView() {
        View newSessionButton = findViewById(R.id.new_session_button);
        newSessionButton.setOnClickListener(v -> mTermuxTerminalSessionClient.addNewSession(false, null));
        newSessionButton.setOnLongClickListener(v -> {
            TextInputDialogUtils.textInput(TermuxActivity.this, R.string.title_create_named_session, null,
                R.string.action_create_named_session_confirm, text -> mTermuxTerminalSessionClient.addNewSession(false, text),
                R.string.action_new_session_failsafe, text -> mTermuxTerminalSessionClient.addNewSession(true, text),
                -1, null, null);
            return true;
        });
    }

    private void setToggleKeyboardView() {
        findViewById(R.id.toggle_keyboard_button).setOnClickListener(v -> {
            mTermuxTerminalViewClient.onToggleSoftKeyboardRequest();
            getDrawer().closeDrawers();
        });

        findViewById(R.id.toggle_keyboard_button).setOnLongClickListener(v -> {
            toggleTerminalToolbar();
            return true;
        });
    }





    @SuppressLint("RtlHardcoded")
    @Override
    public void onBackPressed() {
        if (getDrawer().isDrawerOpen(Gravity.LEFT)) {
            getDrawer().closeDrawers();
        } else {
            finishActivityIfNotFinishing();
        }
    }

    public void finishActivityIfNotFinishing() {
        // prevent duplicate calls to finish() if called from multiple places
        if (!TermuxActivity.this.isFinishing()) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
        }
    }

    /** Show a Snackbar and dismiss the last one if still visible. */
    public void showToast(String text, boolean longDuration) {
        if (text == null || text.isEmpty()) return;
        if (mLastSnackbar != null && mLastSnackbar.isShownOrQueued()) mLastSnackbar.dismiss();
        View rootView = getWindow().getDecorView().getRootView();
        int duration = longDuration ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT;
        mLastSnackbar = Snackbar.make(rootView, text, duration);
        mLastSnackbar.show();
    }



    /** Held while the modern context menu {@link BottomSheetDialog} is showing, since the
     * legacy {@link ContextMenu} passed by the framework is left empty on purpose. */
    private static final class ContextMenuOption {
        final int id;
        final String label;
        final boolean enabled;
        final boolean checked;

        ContextMenuOption(int id, String label, boolean enabled, boolean checked) {
            this.id = id;
            this.label = label;
            this.enabled = enabled;
            this.checked = checked;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        // The legacy floating ContextMenu is intentionally left empty (no items added below).
        // The actual options are shown in a modern BottomSheetDialog instead.
        showContextMenuBottomSheet();
    }

    /** Hook system menu to show context menu instead. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mTerminalView.showContextMenu();
        return false;
    }

    /** Build the list of context menu options for the current session state. */
    private List<ContextMenuOption> buildContextMenuOptions(TerminalSession currentSession) {
        List<ContextMenuOption> options = new ArrayList<>();
        boolean autoFillEnabled = mTerminalView.isAutoFillEnabled();

        options.add(new ContextMenuOption(CONTEXT_MENU_SELECT_URL_ID, getString(R.string.action_select_url), true, false));
        options.add(new ContextMenuOption(CONTEXT_MENU_SHARE_TRANSCRIPT_ID, getString(R.string.action_share_transcript), true, false));
        if (!DataUtils.isNullOrEmpty(mTerminalView.getStoredSelectedText()))
            options.add(new ContextMenuOption(CONTEXT_MENU_SHARE_SELECTED_TEXT, getString(R.string.action_share_selected_text), true, false));
        if (autoFillEnabled)
            options.add(new ContextMenuOption(CONTEXT_MENU_AUTOFILL_USERNAME, getString(R.string.action_autofill_username), true, false));
        if (autoFillEnabled)
            options.add(new ContextMenuOption(CONTEXT_MENU_AUTOFILL_PASSWORD, getString(R.string.action_autofill_password), true, false));
        options.add(new ContextMenuOption(CONTEXT_MENU_RESET_TERMINAL_ID, getString(R.string.action_reset_terminal), true, false));
        options.add(new ContextMenuOption(CONTEXT_MENU_KILL_PROCESS_ID, getResources().getString(R.string.action_kill_process, currentSession.getPid()), currentSession.isRunning(), false));
        options.add(new ContextMenuOption(CONTEXT_MENU_STYLING_ID, getString(R.string.action_style_terminal), true, false));
        options.add(new ContextMenuOption(CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON, getString(R.string.action_toggle_keep_screen_on), true, mPreferences.shouldKeepScreenOn()));
        options.add(new ContextMenuOption(CONTEXT_MENU_HELP_ID, getString(R.string.action_open_help), true, false));
        options.add(new ContextMenuOption(CONTEXT_MENU_SETTINGS_ID, getString(R.string.action_open_settings), true, false));
        options.add(new ContextMenuOption(CONTEXT_MENU_REPORT_ID, getString(R.string.action_report_issue), true, false));
        return options;
    }

    /** Show the terminal context menu as a modern {@link BottomSheetDialog} instead of the
     * classic floating {@link ContextMenu}. */
    private void showContextMenuBottomSheet() {
        TerminalSession currentSession = getCurrentSession();
        if (currentSession == null) return;

        List<ContextMenuOption> options = buildContextMenuOptions(currentSession);

        BottomSheetDialog sheet = new BottomSheetDialog(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View sheetView = inflater.inflate(R.layout.bottom_sheet_context_menu, null);
        LinearLayout container = sheetView.findViewById(R.id.context_menu_items_container);

        for (ContextMenuOption option : options) {
            View row = inflater.inflate(R.layout.item_context_menu_option, container, false);
            TextView label = row.findViewById(R.id.context_menu_item_label);
            ImageView checkIcon = row.findViewById(R.id.context_menu_item_check);

            label.setText(option.label);
            checkIcon.setVisibility(option.checked ? View.VISIBLE : View.GONE);

            if (option.enabled) {
                row.setAlpha(1f);
                row.setOnClickListener(rowView -> {
                    sheet.dismiss();
                    handleContextMenuAction(option.id);
                });
            } else {
                row.setAlpha(0.4f);
                row.setClickable(false);
            }

            container.addView(row);
        }

        sheet.setOnDismissListener(dialog -> mTerminalView.onContextMenuClosed(null));
        sheet.setContentView(sheetView);
        sheet.show();
    }

    /** Handle a context menu action selected from the {@link #showContextMenuBottomSheet()}. */
    private void handleContextMenuAction(int id) {
        TerminalSession session = getCurrentSession();

        switch (id) {
            case CONTEXT_MENU_SELECT_URL_ID:
                mTermuxTerminalViewClient.showUrlSelection();
                break;
            case CONTEXT_MENU_SHARE_TRANSCRIPT_ID:
                mTermuxTerminalViewClient.shareSessionTranscript();
                break;
            case CONTEXT_MENU_SHARE_SELECTED_TEXT:
                mTermuxTerminalViewClient.shareSelectedText();
                break;
            case CONTEXT_MENU_AUTOFILL_USERNAME:
                mTerminalView.requestAutoFillUsername();
                break;
            case CONTEXT_MENU_AUTOFILL_PASSWORD:
                mTerminalView.requestAutoFillPassword();
                break;
            case CONTEXT_MENU_RESET_TERMINAL_ID:
                onResetTerminalSession(session);
                break;
            case CONTEXT_MENU_KILL_PROCESS_ID:
                showKillSessionDialog(session);
                break;
            case CONTEXT_MENU_STYLING_ID:
                showStylingDialog();
                break;
            case CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON:
                toggleKeepScreenOn();
                break;
            case CONTEXT_MENU_HELP_ID:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case CONTEXT_MENU_SETTINGS_ID:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case CONTEXT_MENU_REPORT_ID:
                mTermuxTerminalViewClient.reportIssueFromTranscript();
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Kept as a safety fallback; the legacy ContextMenu is left empty on purpose since
        // options are presented via showContextMenuBottomSheet() instead.
        handleContextMenuAction(item.getItemId());
        return true;
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        // onContextMenuClosed() is triggered twice if back button is pressed to dismiss instead of tap for some reason
        mTerminalView.onContextMenuClosed(menu);
    }

    private void showKillSessionDialog(TerminalSession session) {
        if (session == null) return;

        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.title_confirm_kill_process);
        b.setPositiveButton(android.R.string.yes, (dialog, id) -> {
            dialog.dismiss();
            session.finishIfRunning();
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }

    private void onResetTerminalSession(TerminalSession session) {
        if (session != null) {
            session.reset();
            showToast(getResources().getString(R.string.msg_terminal_reset), true);

            if (mTermuxTerminalSessionClient != null)
                mTermuxTerminalSessionClient.onResetTerminalSession();
        }
    }

    private void showStylingDialog() {
        Intent stylingIntent = new Intent();
        stylingIntent.setClassName(TermuxConstants.TERMUX_STYLING_PACKAGE_NAME, TermuxConstants.TERMUX_STYLING.TERMUX_STYLING_ACTIVITY_NAME);
        try {
            startActivity(stylingIntent);
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            // Tampilkan BottomSheet modern sebagai pengganti AlertDialog klasik
            BottomSheetDialog sheet = new BottomSheetDialog(this);
            View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_styling_install, null);
            sheet.setContentView(sheetView);
            sheetView.findViewById(R.id.btn_styling_install).setOnClickListener(v -> {
                sheet.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TermuxConstants.TERMUX_STYLING_FDROID_PACKAGE_URL)));
            });
            sheetView.findViewById(R.id.btn_styling_cancel).setOnClickListener(v -> sheet.dismiss());
            sheet.show();
        }
    }
    private void toggleKeepScreenOn() {
        if (mTerminalView.getKeepScreenOn()) {
            mTerminalView.setKeepScreenOn(false);
            mPreferences.setKeepScreenOn(false);
        } else {
            mTerminalView.setKeepScreenOn(true);
            mPreferences.setKeepScreenOn(true);
        }
    }



    /**
     * For processes to access shared internal storage (/sdcard) we need this permission.
     */
    public boolean ensureStoragePermissionGranted() {
        if (PermissionUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return true;
        } else {
            Logger.logInfo(LOG_TAG, "Storage permission not granted, requesting permission.");
            PermissionUtils.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Logger.logInfo(LOG_TAG, "Storage permission granted by user on request.");
            TermuxInstaller.setupStorageSymlinks(this);
        } else {
            Logger.logInfo(LOG_TAG, "Storage permission denied by user on request.");
        }
    }



    public int getNavBarHeight() {
        return mNavBarHeight;
    }

    public TermuxActivityRootView getTermuxActivityRootView() {
        return mTermuxActivityRootView;
    }

    public View getTermuxActivityBottomSpaceView() {
        return mTermuxActivityBottomSpaceView;
    }

    public ExtraKeysView getExtraKeysView() {
        return mExtraKeysView;
    }

    public void setExtraKeysView(ExtraKeysView extraKeysView) {
        mExtraKeysView = extraKeysView;
    }

    public DrawerLayout getDrawer() {
        return (DrawerLayout) findViewById(R.id.drawer_layout);
    }


    public ViewPager getTerminalToolbarViewPager() {
        return (ViewPager) findViewById(R.id.terminal_toolbar_view_pager);
    }

    public boolean isTerminalViewSelected() {
        return getTerminalToolbarViewPager().getCurrentItem() == 0;
    }

    public boolean isTerminalToolbarTextInputViewSelected() {
        return getTerminalToolbarViewPager().getCurrentItem() == 1;
    }


    public void termuxSessionListNotifyUpdated() {
        mTermuxSessionListViewController.notifyDataSetChanged();
        updateSessionCountBadge();
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public boolean isOnResumeAfterOnCreate() {
        return isOnResumeAfterOnCreate;
    }



    public TermuxService getTermuxService() {
        return mTermuxService;
    }

    public TerminalView getTerminalView() {
        return mTerminalView;
    }

    public TermuxTerminalViewClient getTermuxTerminalViewClient() {
        return mTermuxTerminalViewClient;
    }

    public TermuxTerminalSessionClient getTermuxTerminalSessionClient() {
        return mTermuxTerminalSessionClient;
    }

    @Nullable
    public TerminalSession getCurrentSession() {
        if (mTerminalView != null)
            return mTerminalView.getCurrentSession();
        else
            return null;
    }

    public TermuxAppSharedPreferences getPreferences() {
        return mPreferences;
    }

    public TermuxAppSharedProperties getProperties() {
        return mProperties;
    }




    public static void updateTermuxActivityStyling(Context context) {
        // Make sure that terminal styling is always applied.
        Intent stylingIntent = new Intent(TERMUX_ACTIVITY.ACTION_RELOAD_STYLE);
        context.sendBroadcast(stylingIntent);
    }

    private void registerTermuxActivityBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);
        intentFilter.addAction(TERMUX_ACTIVITY.ACTION_RELOAD_STYLE);

        registerReceiver(mTermuxActivityBroadcastReceiver, intentFilter);
    }

    private void unregisterTermuxActivityBroadcastReceiever() {
        unregisterReceiver(mTermuxActivityBroadcastReceiver);
    }

    private void fixTermuxActivityBroadcastReceieverIntent(Intent intent) {
        if (intent == null) return;

        String extraReloadStyle = intent.getStringExtra(TERMUX_ACTIVITY.EXTRA_RELOAD_STYLE);
        if ("storage".equals(extraReloadStyle)) {
            intent.removeExtra(TERMUX_ACTIVITY.EXTRA_RELOAD_STYLE);
            intent.setAction(TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);
        }
    }

    class TermuxActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            if (mIsVisible) {
                fixTermuxActivityBroadcastReceieverIntent(intent);

                switch (intent.getAction()) {
                    case TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS:
                        Logger.logDebug(LOG_TAG, "Received intent to request storage permissions");
                        if (ensureStoragePermissionGranted())
                            TermuxInstaller.setupStorageSymlinks(TermuxActivity.this);
                        return;
                    case TERMUX_ACTIVITY.ACTION_RELOAD_STYLE:
                        Logger.logDebug(LOG_TAG, "Received intent to reload styling");
                        reloadActivityStyling();
                        return;
                    default:
                }
            }
        }
    }

    private void reloadActivityStyling() {
        if (mProperties!= null) {
            mProperties.loadTermuxPropertiesFromDisk();

            if (mExtraKeysView != null) {
                mExtraKeysView.setButtonTextAllCaps(mProperties.shouldExtraKeysTextBeAllCaps());
                mExtraKeysView.reload(mProperties.getExtraKeysInfo());
            }
        }

        setMargins();
        setTerminalToolbarHeight();

        if (mTermuxTerminalSessionClient != null)
            mTermuxTerminalSessionClient.onReload();

        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onReload();

        if (mTermuxService != null)
            mTermuxService.setTerminalTranscriptRows();

        // To change the activity and drawer theme, activity needs to be recreated.
        // But this will destroy the activity, and will call the onCreate() again.
        // We need to investigate if enabling this is wise, since all stored variables and
        // views will be destroyed and bindService() will be called again. Extra keys input
        // text will we restored since that has already been implemented. Terminal sessions
        // and transcripts are also already preserved. Theme does change properly too.
        // TermuxActivity.this.recreate();
    }



    public static void startTermuxActivity(@NonNull final Context context) {
        context.startActivity(newInstance(context));
    }

    public static Intent newInstance(@NonNull final Context context) {
        Intent intent = new Intent(context, TermuxActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    // ══════════════════════════════════════════════════════════
    // CUSTOM TOOLBAR
    // ══════════════════════════════════════════════════════════

    private void setupCustomToolbar() {
        // Hamburger → open sessions drawer
        View hamburger = findViewById(R.id.btn_hamburger);
        if (hamburger != null) {
            hamburger.setOnClickListener(v -> {
                DrawerLayout drawer = getDrawer();
                if (drawer != null) {
                    if (drawer.isDrawerOpen(android.view.Gravity.LEFT)) {
                        drawer.closeDrawers();
                    } else {
                        drawer.openDrawer(android.view.Gravity.LEFT);
                    }
                }
            });
        }

        // Right panel button → open right sidebar
        View rightPanelBtn = findViewById(R.id.btn_right_panel);
        if (rightPanelBtn != null) {
            rightPanelBtn.setOnClickListener(v -> {
                DrawerLayout drawer = getDrawer();
                if (drawer == null) return;
                if (drawer.isDrawerOpen(android.view.Gravity.RIGHT)) {
                    drawer.closeDrawer(android.view.Gravity.RIGHT);
                } else {
                    // Tampilkan terminal dulu kalau sedang di tab lain
                    if (mCurrentTabId != R.id.nav_terminal) {
                        if (mBottomNav != null) mBottomNav.setSelectedItemId(R.id.nav_terminal);
                        showTab(R.id.nav_terminal);
                    }
                    populateRightDrawerInfo();
                    drawer.openDrawer(android.view.Gravity.RIGHT);
                }
            });
        }

        // Version box → open Developer Info
        View versionBox = findViewById(R.id.toolbar_version_box);
        if (versionBox != null) {
            versionBox.setOnClickListener(v ->
                startActivity(new Intent(this, DeveloperInfoActivity.class)));
        }
    }


    // ══════════════════════════════════════════════════════════
    // BOTTOM NAVIGATION
    // ══════════════════════════════════════════════════════════

    private void setupBottomNavigation() {
        mFilesContainer    = findViewById(R.id.files_container);
        mPackagesContainer = findViewById(R.id.packages_container);
        mToolsContainer    = findViewById(R.id.tools_container);

        mBottomNav = findViewById(R.id.bottom_nav);
        if (mBottomNav == null) return;

        mBottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == mCurrentTabId) return true;
            showTab(id);
            return true;
        });
    }

    private void showTab(int tabId) {
        mCurrentTabId = tabId;
        DrawerLayout drawerLayout  = getDrawer();
        boolean isTerminal = (tabId == R.id.nav_terminal);

        // Terminal DrawerLayout visibility
        if (drawerLayout != null) {
            drawerLayout.setVisibility(isTerminal ? View.VISIBLE : View.GONE);
        }

        // Hamburger button — only meaningful on terminal tab
        View hamburger = findViewById(R.id.btn_hamburger);
        if (hamburger != null) hamburger.setVisibility(isTerminal ? View.VISIBLE : View.GONE);

        // File / Package / Tools containers
        if (mFilesContainer    != null) mFilesContainer.setVisibility(tabId == R.id.nav_files    ? View.VISIBLE : View.GONE);
        if (mPackagesContainer != null) mPackagesContainer.setVisibility(tabId == R.id.nav_packages ? View.VISIBLE : View.GONE);
        if (mToolsContainer    != null) mToolsContainer.setVisibility(tabId == R.id.nav_tools    ? View.VISIBLE : View.GONE);

        // Settings tab → open SettingsActivity
        if (tabId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            // Revert selection back to terminal
            mCurrentTabId = R.id.nav_terminal;
            if (mBottomNav != null) mBottomNav.setSelectedItemId(R.id.nav_terminal);
            showTab(R.id.nav_terminal);
            return;
        }

        // Refresh files list when switching to Files tab
        if (tabId == R.id.nav_files && mHomeDirectory != null) {
            loadDirectory(mCurrentDirectory != null ? mCurrentDirectory : mHomeDirectory);

        // Auto-refresh status install/uninstall saat buka tab Packages
        if (tabId == R.id.nav_packages) {
            refreshPackageStatus();
        }
        }
    }


    // ══════════════════════════════════════════════════════════
    // TAB: FILES
    // ══════════════════════════════════════════════════════════

    private void setupFilesTab() {
        if (mFilesContainer == null) return;

        mHomeDirectory = new File(TermuxConstants.TERMUX_HOME_DIR_PATH);
        mCurrentDirectory = mHomeDirectory;

        ListView listView = mFilesContainer.findViewById(R.id.files_list);
        if (listView == null) return;

        mFileAdapter = new FileAdapter(new ArrayList<>());
        listView.setAdapter(mFileAdapter);

        // Directory navigation on item click
        listView.setOnItemClickListener((parent, view, pos, id) -> {
            File f = mFileAdapter.getItem(pos);
            if (f != null && f.isDirectory()) loadDirectory(f);
        });

        // Breadcrumb back/home buttons
        View backBtn = mFilesContainer.findViewById(R.id.files_btn_back);
        if (backBtn != null) backBtn.setOnClickListener(v -> {
            if (mCurrentDirectory != null && mCurrentDirectory.getParentFile() != null
                    && !mCurrentDirectory.equals(mHomeDirectory)) {
                loadDirectory(mCurrentDirectory.getParentFile());
            }
        });

        View homeBtn = mFilesContainer.findViewById(R.id.files_btn_home);
        if (homeBtn != null) homeBtn.setOnClickListener(v -> loadDirectory(mHomeDirectory));


        // + File button
        View addFile = mFilesContainer.findViewById(R.id.files_btn_new_file);
        if (addFile != null) addFile.setOnClickListener(v -> promptCreateEntry(false));

        // + Folder button
        View addFolder = mFilesContainer.findViewById(R.id.files_btn_new_folder);
        if (addFolder != null) addFolder.setOnClickListener(v -> promptCreateEntry(true));

        loadDirectory(mHomeDirectory);
    }

    private void loadDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;
        mCurrentDirectory = dir;

        // Update breadcrumb text
        TextView breadcrumb = (mFilesContainer != null) ? mFilesContainer.findViewById(R.id.files_breadcrumb_path) : null;
        if (breadcrumb != null) {
            String path = dir.getAbsolutePath()
                .replace(TermuxConstants.TERMUX_HOME_DIR_PATH, "~");
            breadcrumb.setText(path);
        }

        File[] children = dir.listFiles();
        List<File> files = (children != null) ? Arrays.asList(children) : new ArrayList<>();
        Collections.sort(files, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        if (mFileAdapter != null) {
            mFileAdapter.setData(files);
        }
    }

    private void promptCreateEntry(boolean isFolder) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(isFolder ? R.string.files_new_folder_hint : R.string.files_new_file_hint);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) return;
            File target = new File(mCurrentDirectory, name);
            try {
                if (isFolder) {
                    target.mkdir();
                } else {
                    target.createNewFile();
                }
            } catch (Exception ignored) {}
            loadDirectory(mCurrentDirectory);
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }


    // ══════════════════════════════════════════════════════════
    // TAB: PACKAGES
    // ══════════════════════════════════════════════════════════

    private static final String PREFIX_BIN = TermuxConstants.TERMUX_PREFIX_DIR_PATH + "/bin/";

    private static final String[][] PACKAGE_LIST = {
        // { name, version, description, binary-name }
        {"git",        "2.x",    "Distributed version control system",        "git"},
        {"curl",       "8.x",    "Command line tool for transferring data",    "curl"},
        {"wget",       "1.x",    "Non-interactive network downloader",         "wget"},
        {"vim",        "9.x",    "Highly configurable text editor",            "vim"},
        {"nano",       "7.x",    "Easy-to-use command line text editor",       "nano"},
        {"python",     "3.x",    "Interpreted high-level programming language","python3"},
        {"nodejs",     "20.x",   "JavaScript runtime built on V8",             "node"},
        {"ruby",       "3.x",    "Dynamic, interpreted scripting language",    "ruby"},
        {"golang",     "1.21",   "Open source programming language by Google", "go"},
        {"rust",       "1.x",    "Systems programming language",               "rustc"},
        {"php",        "8.x",    "Server-side scripting language",             "php"},
        {"ffmpeg",     "6.x",    "Multimedia framework for audio/video",       "ffmpeg"},
        {"imagemagick","7.x",    "Image manipulation tools",                   "convert"},
        {"openssh",    "9.x",    "Connectivity tool for remote login",         "ssh"},
        {"nmap",       "7.x",    "Network exploration and security scanner",   "nmap"},
        {"htop",       "3.x",    "Interactive process viewer",                 "htop"},
        {"tree",       "2.x",    "Recursive directory listing command",        "tree"},
        {"jq",         "1.x",    "Lightweight JSON processor",                 "jq"},
        {"zip",        "3.x",    "Package and compress files",                 "zip"},
        {"unzip",      "6.x",    "Extraction utility for .zip files",          "unzip"},
        {"tar",        "1.x",    "Archive utility",                            "tar"},
        {"tmate",      "2.x",    "Instant terminal sharing",                   "tmate"},
        {"tmux",       "3.x",    "Terminal multiplexer",                       "tmux"},
        {"zsh",        "5.x",    "Extended Bourne shell with improvements",    "zsh"},
        {"fish",       "3.x",    "Friendly interactive shell",                 "fish"},
    };

    private void setupPackagesTab() {
        if (mPackagesContainer == null) return;

        List<String[]> packages = new ArrayList<>(Arrays.asList(PACKAGE_LIST));
        mPackageAdapter = new PackageAdapter(packages);

        ListView listView = mPackagesContainer.findViewById(R.id.packages_list);
        if (listView != null) listView.setAdapter(mPackageAdapter);

        // Search filter
        EditText search = mPackagesContainer.findViewById(R.id.packages_search);
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    filterPackages(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Update All
        View updateAll = mPackagesContainer.findViewById(R.id.packages_update_all);
        if (updateAll != null) updateAll.setOnClickListener(v ->
            runCommandInTerminal("pkg update -y\n"));

        // Upgrade All
        View upgradeAll = mPackagesContainer.findViewById(R.id.packages_upgrade_all);
        if (upgradeAll != null) upgradeAll.setOnClickListener(v ->
            runCommandInTerminal("pkg upgrade -y\n"));
    }

    private void filterPackages(String query) {
        if (mPackageAdapter == null) return;
        List<String[]> filtered = new ArrayList<>();
        for (String[] pkg : PACKAGE_LIST) {
            if (pkg[0].contains(query.toLowerCase()) || pkg[2].toLowerCase().contains(query.toLowerCase())) {
                filtered.add(pkg);
            }
        }
        mPackageAdapter.setData(filtered);
    }


    // ══════════════════════════════════════════════════════════
    // TAB: TOOLS
    // ══════════════════════════════════════════════════════════

    private static final String[][] TOOL_LIST = {
        // { name, description, command-tag, install-command, binary-name }
        {"Oh My Zsh",            "Framework for managing Zsh configuration",
            "sh install.sh",  "sh -c \"$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)\"\n", "zsh"},
        {"zsh-autosuggestions",  "Fish-like autosuggestions for Zsh",
            "git clone",      "git clone https://github.com/zsh-users/zsh-autosuggestions ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/zsh-autosuggestions\n", "zsh"},
        {"zsh-syntax-highlighting", "Fish-like syntax highlighting for Zsh",
            "git clone",      "git clone https://github.com/zsh-users/zsh-syntax-highlighting.git ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/zsh-syntax-highlighting\n", "zsh"},
        {"fzf",                  "Command-line fuzzy finder",
            "pkg install fzf","pkg install -y fzf\n", "fzf"},
        {"tmux",                 "Terminal multiplexer — split panes, sessions",
            "pkg install tmux","pkg install -y tmux\n", "tmux"},
        {"thefuck",              "Corrects previous console commands",
            "pip install thefuck","pip install thefuck\n", "thefuck"},
        {"zoxide",               "Smarter cd command with jump support",
            "pkg install zoxide","pkg install -y zoxide\n", "zoxide"},
        {"bat",                  "cat clone with syntax highlighting and Git",
            "pkg install bat", "pkg install -y bat\n", "bat"},
        {"exa",                  "Modern replacement for ls",
            "pkg install exa", "pkg install -y exa\n", "exa"},
        {"ripgrep",              "Recursive regex search (fast grep alternative)",
            "pkg install ripgrep","pkg install -y ripgrep\n", "rg"},
        {"neovim",               "Hyperextensible Vim-based text editor",
            "pkg install neovim","pkg install -y neovim\n", "nvim"},
        {"lazygit",              "Simple terminal UI for git commands",
            "pkg install lazygit","pkg install -y lazygit\n", "lazygit"},
    };

    private void setupToolsTab() {
        if (mToolsContainer == null) return;

        List<String[]> tools = new ArrayList<>(Arrays.asList(TOOL_LIST));
        mToolAdapter = new ToolAdapter(tools);

        ListView listView = mToolsContainer.findViewById(R.id.tools_list);
        if (listView != null) listView.setAdapter(mToolAdapter);

        // Search filter
        EditText search = mToolsContainer.findViewById(R.id.tools_search);
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    filterTools(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterTools(String query) {
        if (mToolAdapter == null) return;
        List<String[]> filtered = new ArrayList<>();
        for (String[] t : TOOL_LIST) {
            if (t[0].toLowerCase().contains(query.toLowerCase())
                    || t[1].toLowerCase().contains(query.toLowerCase())) {
                filtered.add(t);
            }
        }
        mToolAdapter.setData(filtered);
    }


    // ══════════════════════════════════════════════════════════
    // TERMINAL COMMAND INJECTION
    // ══════════════════════════════════════════════════════════

    /** Injects a shell command into the current terminal session. */
    public void runCommandInTerminal(String command) {
        TerminalSession session = getCurrentSession();
        if (session == null || command == null) return;
        // Switch back to terminal tab first
        showTab(R.id.nav_terminal);
        if (mBottomNav != null) mBottomNav.setSelectedItemId(R.id.nav_terminal);
        // Feed the command bytes
        byte[] bytes = command.getBytes();
        session.write(bytes, 0, bytes.length);
    }


    // ══════════════════════════════════════════════════════════
    // SESSION COUNT BADGE
    // ══════════════════════════════════════════════════════════

    private void updateSessionCountBadge() {
        TextView badge = findViewById(R.id.sessions_count_badge);
        if (badge == null || mTermuxService == null) return;
        int count = mTermuxService.getTermuxSessions().size();
        badge.setText(String.valueOf(count));
    }


    // ══════════════════════════════════════════════════════════
    // ADAPTERS
    // ══════════════════════════════════════════════════════════

    /** Adapter for the built-in file browser. */
    private class FileAdapter extends BaseAdapter {
        private List<File> mFiles;

        FileAdapter(List<File> files) {
            mFiles = (files != null) ? files : new ArrayList<>();
        }

        void setData(List<File> files) {
            mFiles = (files != null) ? files : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override public int getCount() { return mFiles.size(); }
        @Override public File getItem(int pos) { return mFiles.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(TermuxActivity.this)
                    .inflate(R.layout.item_file_entry, parent, false);
            }
            File f = mFiles.get(pos);

            ImageView icon = convertView.findViewById(R.id.file_icon);
            TextView name  = convertView.findViewById(R.id.file_name);
            TextView meta  = convertView.findViewById(R.id.file_meta);

            if (icon != null) {
                icon.setImageResource(f.isDirectory()
                    ? R.drawable.ic_nav_files
                    : R.drawable.ic_add_file);
            }
            if (name != null) name.setText(f.getName());
            if (meta != null) {
                if (f.isDirectory()) {
                    File[] ch = f.listFiles();
                    int cnt = (ch != null) ? ch.length : 0;
                    meta.setText(cnt + " items");
                } else {
                    long kb = f.length() / 1024;
                    meta.setText(kb > 0 ? kb + " KB" : f.length() + " B");
                }
            }

            // Kebab menu (delete only for now)
            View kebab = convertView.findViewById(R.id.file_menu_btn);
            if (kebab != null) {
                kebab.setOnClickListener(v -> {
                    android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(TermuxActivity.this);
                    b.setTitle(f.getName());
                    b.setItems(new CharSequence[]{"Delete"}, (dialog, which) -> {
                        deleteRecursive(f);
                        loadDirectory(mCurrentDirectory);
                    });
                    b.show();
                });
            }

            return convertView;
        }

        private void deleteRecursive(File f) {
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null) for (File c : children) deleteRecursive(c);
            }
            f.delete();
        }
    }


    /** Adapter for the package manager tab. */
    private class PackageAdapter extends BaseAdapter {
        private List<String[]> mPkgs;

        PackageAdapter(List<String[]> pkgs) {
            mPkgs = (pkgs != null) ? pkgs : new ArrayList<>();
        }

        void setData(List<String[]> pkgs) {
            mPkgs = (pkgs != null) ? pkgs : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override public int getCount() { return mPkgs.size(); }
        @Override public String[] getItem(int pos) { return mPkgs.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(TermuxActivity.this)
                    .inflate(R.layout.item_package_entry, parent, false);
            }
            String[] pkg = mPkgs.get(pos); // {name, version, desc, binary}
            String name    = pkg[0];
            String version = pkg[1];
            String desc    = pkg[2];
            String binary  = pkg[3];

            boolean installed = new File(PREFIX_BIN + binary).exists();

            TextView nameView = convertView.findViewById(R.id.pkg_name);
            TextView verView  = convertView.findViewById(R.id.pkg_version);
            TextView descView = convertView.findViewById(R.id.pkg_description);
            Button   actionBtn = convertView.findViewById(R.id.pkg_action_btn);

            if (nameView != null) nameView.setText(name);
            if (verView  != null) verView.setText(version);
            if (descView != null) descView.setText(desc);

            if (actionBtn != null) {
                if (installed) {
                    actionBtn.setText(getString(R.string.packages_uninstall));
                    actionBtn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.color_surface_high)));
                    actionBtn.setTextColor(getResources().getColor(R.color.color_text_secondary));
                    actionBtn.setOnClickListener(v ->
                        runCommandInTerminal("pkg uninstall -y " + name + "\n"));
                } else {
                    actionBtn.setText(getString(R.string.packages_install));
                    actionBtn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.color_accent_primary)));
                    actionBtn.setTextColor(getResources().getColor(R.color.color_background));
                    actionBtn.setOnClickListener(v ->
                        runCommandInTerminal("pkg install -y " + name + "\n"));
                }
            }

            return convertView;
        }
    }


    /** Adapter for the tools hub tab. */
    private class ToolAdapter extends BaseAdapter {
        private List<String[]> mTools;

        ToolAdapter(List<String[]> tools) {
            mTools = (tools != null) ? tools : new ArrayList<>();
        }

        void setData(List<String[]> tools) {
            mTools = (tools != null) ? tools : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override public int getCount()            { return mTools.size(); }
        @Override public String[] getItem(int pos) { return mTools.get(pos); }
        @Override public long getItemId(int pos)   { return pos; }

        @Override
        public View getView(int pos, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(TermuxActivity.this)
                    .inflate(R.layout.item_tool_entry, parent, false);
            }
            String[] t = mTools.get(pos); // {name, desc, cmd-tag, install-cmd, binary}
            String name     = t[0];
            String desc     = t[1];
            String cmdTag   = t[2];
            String installCmd = t[3];
            String binary   = t[4];

            boolean installed = new File(PREFIX_BIN + binary).exists();

            TextView nameView = convertView.findViewById(R.id.tool_name);
            TextView descView = convertView.findViewById(R.id.tool_description);
            TextView cmdView  = convertView.findViewById(R.id.tool_command_tag);
            Button actionBtn  = convertView.findViewById(R.id.tool_install_btn);

            if (nameView != null) nameView.setText(name);
            if (descView != null) descView.setText(desc);
            if (cmdView  != null) cmdView.setText(cmdTag);

            if (actionBtn != null) {
                if (installed) {
                    actionBtn.setText("Installed");
                    actionBtn.setEnabled(false);
                    actionBtn.setAlpha(0.5f);
                    actionBtn.setOnClickListener(null);
                } else {
                    actionBtn.setText(getString(R.string.packages_install));
                    actionBtn.setEnabled(true);
                    actionBtn.setAlpha(1f);
                    actionBtn.setOnClickListener(v -> runCommandInTerminal(installCmd));
                }
            }

            return convertView;
        }
    }


    // ══════════════════════════════════════════════════════════
    // RIGHT SIDEBAR — Setup & Logic
    // ══════════════════════════════════════════════════════════

    private void setupRightDrawer() {
        DrawerLayout drawer = getDrawer();
        if (drawer == null) return;

        // Tombol close di dalam sidebar
        View closeBtn = drawer.findViewById(R.id.btn_close_right_drawer);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> drawer.closeDrawer(android.view.Gravity.RIGHT));
        }

        // ── Quick Actions ──
        View actionNewSession = drawer.findViewById(R.id.sidebar_action_new_session);
        if (actionNewSession != null) {
            actionNewSession.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                View newSessionBtn = findViewById(R.id.new_session_button);
                if (newSessionBtn != null) newSessionBtn.performClick();
            });
        }

                  View actionKillSession = drawer.findViewById(R.id.sidebar_action_kill_session);
        if (actionKillSession != null) {
            actionKillSession.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                TerminalSession current = getCurrentSession();
                if (current != null) current.finishIfRunning();
            });
        }

        View actionKeyboard = drawer.findViewById(R.id.sidebar_action_keyboard);
        if (actionKeyboard != null) {
            actionKeyboard.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                if (mTermuxTerminalViewClient != null) {
                    mTermuxTerminalViewClient.onToggleSoftKeyboardRequest();
                }
            });
        }

        View actionReset = drawer.findViewById(R.id.sidebar_action_reset);
        if (actionReset != null) {
            actionReset.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                TerminalSession current = getCurrentSession();
                if (current != null) {
                    current.reset();
                    showToast(getString(R.string.msg_terminal_reset), true);
                }
            });
        }

        // ── App Settings ──
        View actionAppearance = drawer.findViewById(R.id.sidebar_action_appearance);
        if (actionAppearance != null) {
            actionAppearance.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                startActivity(new Intent(this, TerminalAppearanceActivity.class));
            });
        }

        View actionSettings = drawer.findViewById(R.id.sidebar_action_settings);
        if (actionSettings != null) {
            actionSettings.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }

        View actionDevInfo = drawer.findViewById(R.id.sidebar_action_devinfo);
        if (actionDevInfo != null) {
            actionDevInfo.setOnClickListener(v -> {
                drawer.closeDrawer(android.view.Gravity.RIGHT);
                startActivity(new Intent(this, DeveloperInfoActivity.class));
            });
        }
    }

    /** Isi data sistem di right sidebar setiap kali dibuka. */
    private void populateRightDrawerInfo() {
        DrawerLayout drawer = getDrawer();
        if (drawer == null) return;

        // Android API level
        android.widget.TextView androidView = drawer.findViewById(R.id.sidebar_info_android);
        if (androidView != null) {
            androidView.setText("API " + android.os.Build.VERSION.SDK_INT
                + " (Android " + android.os.Build.VERSION.RELEASE + ")");
        }

        // CPU architecture
        android.widget.TextView archView = drawer.findViewById(R.id.sidebar_info_arch);
        if (archView != null) {
            String[] abis = android.os.Build.SUPPORTED_ABIS;
            archView.setText(abis != null && abis.length > 0 ? abis[0] : "unknown");
        }

        // Free internal storage
        android.widget.TextView storageView = drawer.findViewById(R.id.sidebar_info_storage);
        if (storageView != null) {
            android.os.StatFs stat = new android.os.StatFs(getFilesDir().getPath());
            long freeBytes = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            storageView.setText(formatBytes(freeBytes));
        }

        // Termux prefix directory size (background thread agar tidak freeze)
        android.widget.TextView prefixView = drawer.findViewById(R.id.sidebar_info_prefix);
        if (prefixView != null) {
            prefixView.setText("...");
            new Thread(() -> {
                File prefix = new File(com.termux.shared.termux.TermuxConstants.TERMUX_PREFIX_DIR_PATH);
                final String size = prefix.exists() ? formatBytes(getDirSize(prefix)) : "N/A";
                runOnUiThread(() -> {
                    android.widget.TextView v = drawer.findViewById(R.id.sidebar_info_prefix);
                    if (v != null) v.setText(size);
                });
            }).start();
        }

        // Active sessions count
        android.widget.TextView sessView = drawer.findViewById(R.id.sidebar_info_sessions);
        if (sessView != null) {
            int count = (mTermuxService != null)
                ? mTermuxService.getTermuxSessions().size() : 0;
            sessView.setText(String.valueOf(count));
        }
    }

    /** Hitung total ukuran direktori secara rekursif. */
    private long getDirSize(File dir) {
        long size = 0;
        if (dir == null || !dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            size += f.isDirectory() ? getDirSize(f) : f.length();
        }
        return size;
    }

    /** Format bytes ke string human-readable (KB / MB / GB). */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }


    // ══════════════════════════════════════════════════════════
    // PACKAGES AUTO-SYNC
    // ══════════════════════════════════════════════════════════

    /**
     * Sinkronkan status install/uninstall semua paket secara langsung.
     * Cukup panggil notifyDataSetChanged() — adapter sudah cek file existence di getView().
     */
    private void refreshPackageStatus() {
        if (mPackageAdapter != null) {
            mPackageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Jadwalkan auto-refresh paket setelah delay (dipanggil setelah command pkg install/uninstall).
     * Delay diperlukan karena pkg butuh waktu beberapa detik untuk selesai.
     */
    private void schedulePackageRefresh(long delayMs) {
        if (mPkgRefreshHandler == null) {
            mPkgRefreshHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        if (mPkgRefreshRunnable == null) {
            mPkgRefreshRunnable = this::refreshPackageStatus;
        }
        mPkgRefreshHandler.removeCallbacks(mPkgRefreshRunnable);
        mPkgRefreshHandler.postDelayed(mPkgRefreshRunnable, delayMs);
    }

}
