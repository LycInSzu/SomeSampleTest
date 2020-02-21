package com.wtk.charge;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wtk.charge.ChargeAnimationService.IRemoveView;
import com.android.systemui.R;

public class ChargeAnimationView extends FrameLayout{

    private Context mContext;
    private BubbleViscosity mBubbleViscosity;
    private TextView mBattertPercent;
    private TextView mChargeStatus;
    private ImageView mChargeStatusImage;
    private ImageView mChargeCircleImage;
    private IRemoveView iRemoveView;
    private View view;

    public ChargeAnimationView(Context context) {
        super(context);
        mContext = context;
        initChargeAnimation();
    }

    public ChargeAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //initChargeAnimation();
    }

    public void setIRemoveView(IRemoveView iRemoveView) {
        this.iRemoveView = iRemoveView;
    }

    private void initChargeAnimation() {
        view = View.inflate(mContext, R.layout.charge_animation_layout, null);
        addView(view);
        mBubbleViscosity = view.findViewById(R.id.bubble_viscosity);
        mBattertPercent = view.findViewById(R.id.battert_percent);
        mChargeStatus = view.findViewById(R.id.charge_status);
        mChargeStatusImage = view.findViewById(R.id.charge_status_image);
        mChargeCircleImage = view.findViewById(R.id.charge_circle);
        Animation rotate = AnimationUtils.loadAnimation(mContext, R.anim.rotate_anim);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        rotate.setInterpolator(linearInterpolator);
        mChargeCircleImage.setAnimation(rotate);
        mChargeCircleImage.startAnimation(rotate);
        mBubbleViscosity.run();
    }

    public void setBattertPercent(int percent) {
        mBattertPercent.setText(percent + "");
    }

    public void setChargeStatus(String status) {
        mChargeStatus.setText(status);
    }

    public void setChargeStatusImageVisibility(int visibility) {
        mChargeStatusImage.setVisibility(visibility);
    }

    public void setmChargeStatusImage(int resId) {
        mChargeStatusImage.setImageResource(resId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        iRemoveView.remove();
        return true;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        /*if (visibility == View.VISIBLE) {
            Animation rotate = AnimationUtils.loadAnimation(mContext, R.anim.rotate_anim);
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            rotate.setInterpolator(linearInterpolator);
            mChargeCircleImage.setAnimation(rotate);
            mChargeCircleImage.startAnimation(rotate);
            mBubbleViscosity.run();
        }*/
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

}
