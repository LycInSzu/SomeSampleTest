package com.cydroid.ota.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cydroid.ota.R;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.SystemTheme;
import com.cydroid.ota.logic.IContextState;

/**
 * @author borney
 *         Created by borney on 6/9/15.
 */
public class AnimatorStateLayout extends RelativeLayout implements ITheme, IStateView {
    private AnimatorStateView mStateView;
    private ExpendTextView mExpendTextView;
    private TextView mVersionView;
    private SystemTheme mSystemTheme;
    private float mNormalMarginBottom, mChangeMarginBottom;

    public AnimatorStateLayout(Context context) {
        this(context, null);
    }

    public AnimatorStateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatorStateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSystemTheme = ((SettingUpdateApplication) context.getApplicationContext()).getSystemTheme();
        mSystemTheme.addTheme(this);
        Resources res = context.getResources();
        mNormalMarginBottom = res.getDimension(R.dimen.gn_su_layout_main_expend_text_marginBottom);
        mChangeMarginBottom = res.getDimension(R.dimen.gn_su_layout_main_versionview_marginBottom);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mStateView = getView(R.id.gn_su_layout_main_statebutton);
        mSystemTheme.addTheme(mStateView);
        mExpendTextView = getView(R.id.gn_su_layout_main_expend_text);
        mVersionView = getView(R.id.gn_su_layout_main_versionview);
    }

    @Override
    public void changeState(IContextState contextState) {
        mStateView.changeState(contextState);
        LayoutParams expendParams = (LayoutParams) mExpendTextView.getLayoutParams();
        if (mVersionView.getVisibility() == VISIBLE) {
            expendParams.bottomMargin = (int) mNormalMarginBottom;
        } else {
            expendParams.bottomMargin = (int) mChangeMarginBottom;
        }
        mExpendTextView.setLayoutParams(expendParams);
    }

    @Override
    public void onDestory() {
        mStateView.onDestory();
    }

    @Override
    public void onChameleonChanged(Chameleon chameleon) {

    }

    public void setBackShow(boolean isBackShow) {
        Resources res = getResources();
        mStateView.setBackShow(isBackShow);
        mExpendTextView.setTextColor(isBackShow ?
                res.getColor(R.color.gn_su_layout_main_expend_text_back_textColor) :
                res.getColor(R.color.gn_su_layout_main_expend_text_textColor));
        mVersionView.setTextColor(isBackShow ?
                res.getColor(R.color.gn_su_layout_main_versionview_back_textColor) :
                res.getColor(R.color.gn_su_layout_main_expend_text_textColor));
    }

    private <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }
}
