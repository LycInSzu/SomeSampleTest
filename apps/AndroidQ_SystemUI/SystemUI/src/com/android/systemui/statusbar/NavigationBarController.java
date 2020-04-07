/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import static android.view.Display.DEFAULT_DISPLAY;

import static com.android.systemui.Dependency.MAIN_HANDLER_NAME;
import static com.android.systemui.SysUiServiceProvider.getComponent;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;

import androidx.annotation.Nullable;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.RegisterStatusBarResult;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue.Callbacks;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.BarTransitions;
import com.android.systemui.statusbar.phone.BarTransitions.TransitionMode;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.BatteryController;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


/** A controller to handle navigation bars. */
@Singleton
public class NavigationBarController implements Callbacks {

    private static final String TAG = NavigationBarController.class.getSimpleName();

    private final Context mContext;
    private final Handler mHandler;
    private final DisplayManager mDisplayManager;

    /** A displayId - nav bar maps. */
    @VisibleForTesting
    SparseArray<NavigationBarFragment> mNavigationBars = new SparseArray<>();

    // add by wangjian for TEJWQE-414 20200331 start
    private boolean mHasNavigationBar = true;
    private int mCurrentStyle = -1;
    private int mNormalStyle = 0;
    private int mAbnormalStyle = 1;
    private int mHaveNfcNormalStyle = 2;
    private int mHaveNfcAbnormalStyle = 3;
    private NavigationBarView mNbv;
    private static final boolean HAVE_SMART_NAVIBAR =
            android.os.SystemProperties.getBoolean("ro.odm_smart_navibar_support", false);
    private int mDisabled1 = 0;
    private StatusBar mStatusBar;
    protected IStatusBarService mBarService;
    protected RegisterStatusBarResult result = null;
    private boolean isReloadNavigationBar = false;
    // add by wangjian for TEJWQE-414 20200331 end

    @Inject
    public NavigationBarController(Context context, @Named(MAIN_HANDLER_NAME) Handler handler) {
        mContext = context;
        mHandler = handler;
        mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        CommandQueue commandQueue = getComponent(mContext, CommandQueue.class);
        if (commandQueue != null) {
            commandQueue.addCallback(this);
        }

        // add by wangjian for TEJWQE-414 20200331 start
        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));
        try {
            result = mBarService.registerStatusBar(commandQueue);
        } catch (RemoteException ex) {
            ex.rethrowFromSystemServer();
        }
        if (HAVE_SMART_NAVIBAR) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SMART_NAVIGATION_BAR),
                    false,
                    mNavibarSettingsObserver);
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SMART_NAVIGATION_BAR_STYLE),
                    false,
                    mNavibarStyleChoiceObserver);
        }
        // add by wangjian for TEJWQE-414 20200331 end
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        removeNavigationBar(displayId);
    }

    @Override
    public void onDisplayReady(int displayId) {
        Display display = mDisplayManager.getDisplay(displayId);
        createNavigationBar(display, null);
    }

    // TODO(b/117478341): I use {@code includeDefaultDisplay} to make this method compatible to
    // CarStatusBar because they have their own nav bar. Think about a better way for it.
    /**
     * Creates navigation bars when car/status bar initializes.
     *
     * @param includeDefaultDisplay {@code true} to create navigation bar on default display.
     */
    public void createNavigationBars(final boolean includeDefaultDisplay,
            RegisterStatusBarResult result) {
        Display[] displays = mDisplayManager.getDisplays();
        for (Display display : displays) {
            if (includeDefaultDisplay || display.getDisplayId() != DEFAULT_DISPLAY) {
                createNavigationBar(display, result);
            }
        }
    }

    /**
     * Adds a navigation bar on default display or an external display if the display supports
     * system decorations.
     *
     * @param display the display to add navigation bar on.
     */
    @VisibleForTesting
    void createNavigationBar(Display display, RegisterStatusBarResult result) {
        if (display == null) {
            return;
        }

        final int displayId = display.getDisplayId();
        final boolean isOnDefaultDisplay = displayId == DEFAULT_DISPLAY;
        final IWindowManager wms = WindowManagerGlobal.getWindowManagerService();

        try {
            if (!wms.hasNavigationBar(displayId)) {
                // add by wangjian for TEJWQE-414 20200331 start
                mHasNavigationBar = false;
                // add by wangjian for TEJWQE-414 20200331 end
                return;
            }
        } catch (RemoteException e) {
            // Cannot get wms, just return with warning message.
            Log.w(TAG, "Cannot get WindowManager.");
            return;
        }
        final Context context = isOnDefaultDisplay
                ? mContext
                : mContext.createDisplayContext(display);
        NavigationBarFragment.create(context, (tag, fragment) -> {
            NavigationBarFragment navBar = (NavigationBarFragment) fragment;

            // Unfortunately, we still need it because status bar needs LightBarController
            // before notifications creation. We cannot directly use getLightBarController()
            // from NavigationBarFragment directly.
            LightBarController lightBarController = isOnDefaultDisplay
                    ? Dependency.get(LightBarController.class)
                    : new LightBarController(context,
                            Dependency.get(DarkIconDispatcher.class),
                            Dependency.get(BatteryController.class));
            navBar.setLightBarController(lightBarController);

            // TODO(b/118592525): to support multi-display, we start to add something which is
            //                    per-display, while others may be global. I think it's time to add
            //                    a new class maybe named DisplayDependency to solve per-display
            //                    Dependency problem.
            AutoHideController autoHideController = isOnDefaultDisplay
                    ? Dependency.get(AutoHideController.class)
                    : new AutoHideController(context, mHandler);
            navBar.setAutoHideController(autoHideController);
            // modify by wangjian for TEJWQE-414 20200331 start
            //navBar.restoreSystemUiVisibilityState();
            if (!isReloadNavigationBar) {
                navBar.restoreSystemUiVisibilityState();
            } else {
                isReloadNavigationBar = false;
            }
            // modify by wangjian for TEJWQE-414 20200331 end
            mNavigationBars.append(displayId, navBar);
            // add by wangjian for TEJWQE-414 20200331 start
            InitSmartNavibar();
            // add by wangjian for TEJWQE-414 20200331 end

            if (result != null) {
                navBar.setImeWindowStatus(display.getDisplayId(), result.mImeToken,
                        result.mImeWindowVis, result.mImeBackDisposition,
                        result.mShowImeSwitcher);
            }
        });
    }

    private void removeNavigationBar(int displayId) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        if (navBar != null) {
            View navigationWindow = navBar.getView().getRootView();
            WindowManagerGlobal.getInstance()
                    .removeView(navigationWindow, true /* immediate */);
            mNavigationBars.remove(displayId);
        }
    }

    /** @see NavigationBarFragment#checkNavBarModes() */
    public void checkNavBarModes(int displayId) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        if (navBar != null) {
            navBar.checkNavBarModes();
        }
    }

    /** @see NavigationBarFragment#finishBarAnimations() */
    public void finishBarAnimations(int displayId) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        if (navBar != null) {
            navBar.finishBarAnimations();
        }
    }

    /** @see NavigationBarFragment#touchAutoDim() */
    public void touchAutoDim(int displayId) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        if (navBar != null) {
            navBar.touchAutoDim();
        }
    }

    /** @see NavigationBarFragment#transitionTo(int, boolean) */
    public void transitionTo(int displayId, @TransitionMode int barMode, boolean animate) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        if (navBar != null) {
            navBar.transitionTo(barMode, animate);
        }
    }

    /** @see NavigationBarFragment#disableAnimationsDuringHide(long) */
    public void disableAnimationsDuringHide(int displayId, long delay) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        if (navBar != null) {
            navBar.disableAnimationsDuringHide(delay);
        }
    }

    /** @return {@link NavigationBarView} on the default display. */
    public @Nullable NavigationBarView getDefaultNavigationBarView() {
        return getNavigationBarView(DEFAULT_DISPLAY);
    }

    /**
     * @param displayId the ID of display which Navigation bar is on
     * @return {@link NavigationBarView} on the display with {@code displayId}.
     *         {@code null} if no navigation bar on that display.
     */
    public @Nullable NavigationBarView getNavigationBarView(int displayId) {
        NavigationBarFragment navBar = mNavigationBars.get(displayId);
        return (navBar == null) ? null : (NavigationBarView) navBar.getView();
    }

    /** @return {@link NavigationBarFragment} on the default display. */
    public NavigationBarFragment getDefaultNavigationBarFragment() {
        return mNavigationBars.get(DEFAULT_DISPLAY);
    }

    // add by wangjian for TEJWQE-414 20200331 start
    private void InitSmartNavibar() {
        if (mHasNavigationBar
                && HAVE_SMART_NAVIBAR) {
            mNbv = getDefaultNavigationBarView();
            if (mNbv == null) return;
            boolean isNavibarEnabled = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.SMART_NAVIGATION_BAR,
                    0) == 1;
            mNbv.setNavibarEnabled(isNavibarEnabled);
            mNbv.setNavibarArrowState();
            int mStyle = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SMART_NAVIGATION_BAR_STYLE, 0);
            mNbv.setIsSecondStyle((mStyle == mAbnormalStyle || mStyle == mHaveNfcAbnormalStyle) ? true : false);
            mNbv.setNtfPanelContorlEnabled((mStyle == mHaveNfcNormalStyle || mStyle == mHaveNfcAbnormalStyle) ? true : false);
            mNbv.setNtfPanelContorlState();
            mNbv.getNavButtons().setVisibility(
                    (mStyle == mAbnormalStyle || mStyle == mHaveNfcAbnormalStyle) ? View.GONE : View.VISIBLE);
            mNbv.getSecondNavButtons().setVisibility(
                    (mStyle == mAbnormalStyle || mStyle == mHaveNfcAbnormalStyle) ? View.VISIBLE : View.GONE);
            //mNbv.setDisabledFlags(mDisabled1);

            mNbv.getNavigationBarButton().setOnClickListener(mNavigationBarClickListener);
            mNbv.getNtfPanelContorlButton().setOnClickListener(mNavigationStatusBarClickListener);

            mNbv.getBarTransitions().transitionTo(BarTransitions.MODE_TRANSPARENT, false);
        }
    }

    private ContentObserver mNavibarSettingsObserver = new ContentObserver(new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            if (mNbv == null) {
                reloadNavigationBar();
                return;
            }
            boolean isNavibarEnabled = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.SMART_NAVIGATION_BAR,
                    0) == 1;
            if (isNavibarEnabled) {
                mNbv.setNavibarEnabled(true);
                mNbv.setNavibarArrowState();
            } else {
                mNbv.setNavibarEnabled(false);
                mNbv.setNavibarArrowState();
            }
        }
    };

    private ContentObserver mNavibarStyleChoiceObserver = new ContentObserver(new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            int style = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SMART_NAVIGATION_BAR_STYLE,0);
            changeNavibarStyle(style);
        }
    };

    private void changeNavibarStyle(int style) {
        if(mNbv == null) {
            reloadNavigationBar();
            return;
        } else {
            mNbv.setIsSecondStyle((style == mAbnormalStyle || style == mHaveNfcAbnormalStyle) ? true : false);
            mNbv.setLayoutTransitionsEnabled(true);
            mHandler.removeCallbacks(mNaviBarAnimationRunnable);
            if((mCurrentStyle == mAbnormalStyle && style == mHaveNfcNormalStyle)
                    || (mCurrentStyle == mHaveNfcNormalStyle && style == mAbnormalStyle)
                    || (mCurrentStyle == mNormalStyle && style == mHaveNfcAbnormalStyle)
                    || (mCurrentStyle == mHaveNfcAbnormalStyle && style == mNormalStyle)) {
                mNbv.getNavButtons().setVisibility(
                        (style == mAbnormalStyle || style == mHaveNfcAbnormalStyle) ? View.GONE : View.VISIBLE);
                mNbv.getSecondNavButtons().setVisibility(
                        (style == mAbnormalStyle || style == mHaveNfcAbnormalStyle) ? View.VISIBLE : View.GONE);
                mCurrentStyle = style;
                mHandler.postDelayed(mNaviBarAnimationRunnable, 50);
            } else {
                mNbv.setNtfPanelContorlEnabled((style == mHaveNfcNormalStyle || style == mHaveNfcAbnormalStyle) ? true : false);
                mNbv.setNtfPanelContorlState();
                mNbv.getNavButtons().setVisibility(
                        (style == mAbnormalStyle || style == mHaveNfcAbnormalStyle) ? View.GONE : View.VISIBLE);
                mNbv.getSecondNavButtons().setVisibility(
                        (style == mAbnormalStyle || style == mHaveNfcAbnormalStyle) ? View.VISIBLE : View.GONE);
                mCurrentStyle = style;
            }
        }
    }

    private View.OnClickListener mNavigationBarClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            removeNavigationBar();
        }
    };

    private View.OnClickListener mNavigationStatusBarClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(mStatusBar.getNotificationPanel().isQsExpanded()) {
                mStatusBar.animateCollapsePanels();
            } else if(mStatusBar.getNotificationPanel().isFullyExpanded()){
                mStatusBar.getNotificationPanel().openQs();
            } else {
                mStatusBar.instantExpandNotificationsPanel();
            }
        }
    };

    private Runnable mNaviBarAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if(null == mNbv) return;
            mNbv.setLayoutTransitionsEnabled(true);
            mNbv.setNtfPanelContorlEnabled(
                    (mCurrentStyle == mHaveNfcNormalStyle || mCurrentStyle == mHaveNfcAbnormalStyle) ? true : false);
            mNbv.setNtfPanelContorlState();
            mNbv.getNavButtons().setVisibility(
                    (mCurrentStyle == mAbnormalStyle || mCurrentStyle == mHaveNfcAbnormalStyle) ? View.GONE : View.VISIBLE);
            mNbv.getSecondNavButtons().setVisibility(
                    (mCurrentStyle == mAbnormalStyle || mCurrentStyle == mHaveNfcAbnormalStyle) ? View.VISIBLE : View.GONE);
        }
    };

    public void reloadNavigationBar() {
        Log.d(TAG, "reloadNavigationBar: about to reload getDefaultNavigationBarView() = " + getDefaultNavigationBarView());
        if (getDefaultNavigationBarView() != null) return;

        if (mHasNavigationBar) {
            try {
                isReloadNavigationBar = true;
                createNavigationBars(true,result);
                //checkNavBarModes(DEFAULT_DISPLAY);
            } catch (WindowManager.BadTokenException be) {
                Log.w(TAG, "WindowManager add NavigationBarView has a BadTokenException");
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "WindowManager add NavigationBarView has a IllegalArgumentException");
            }
        }
    }

    public void removeNavigationBar() {
        Log.d(TAG, "removeNavigationBar: about to remove ");
        removeNavigationBar(DEFAULT_DISPLAY);
        mNbv = null;
    }

    public void setDisableFlags (int disabled1) {
        mDisabled1 = disabled1;
    }
    public void setStatusBar (StatusBar statusBar) {
        mStatusBar = statusBar;
    }
    // add by wangjian for TEJWQE-414 20200331 end
}
