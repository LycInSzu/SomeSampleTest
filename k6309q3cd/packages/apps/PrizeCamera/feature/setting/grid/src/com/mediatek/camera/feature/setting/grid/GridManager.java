package com.mediatek.camera.feature.setting.grid;

import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.ui.AbstractViewManager;
import com.mediatek.camera.ui.CameraAppUI;

public class GridManager extends AbstractViewManager {

    private final GridLineView mGridLineView;
    private final CameraAppUI mCameraAppUI;

    /**
     * Constructor of abstractViewManager.
     *  @param app        The {@link IApp} implementer.
     * @param parentView the root view of ui.
     * @param cameraAppUI
     */
    public GridManager(IApp app, ViewGroup parentView, CameraAppUI cameraAppUI) {
        super(app, parentView);
        mGridLineView = parentView.findViewById(R.id.grid_line);
        this.mCameraAppUI = cameraAppUI;
    }

    @Override
    protected View getView() {
        return mGridLineView;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public void updatePreviewSize(int width, int height) {
        mGridLineView.setPreviewSize(width,height);
        RectF rectF = mCameraAppUI.getPreviewArea();
        int top = (int) rectF.top;
        int bottom = (int) rectF.bottom;
        mGridLineView.setPreviewTopBottom(top,bottom);
    }

    public void onPreviewStart() {
        RectF rectF = mCameraAppUI.getPreviewArea();
        int top = (int) rectF.top;
        int bottom = (int) rectF.bottom;
        mGridLineView.setPreviewTopBottom(top,bottom);
    }
}
