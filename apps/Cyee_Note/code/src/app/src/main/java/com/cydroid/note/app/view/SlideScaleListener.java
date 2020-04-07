package com.cydroid.note.app.view;

/**
 * Created by spc on 16-3-24.
 */
public class SlideScaleListener implements SlideDetector.OnSlideListener {

    private SlideEndResponse mSlideEndResponse;

    public SlideScaleListener() {
    }

    public SlideScaleListener(SlideEndResponse slideEndResponse) {
        mSlideEndResponse = slideEndResponse;
    }

    @Override
    public boolean onSlideStart(SlideDetector detector) {
        return true;
    }

    @Override
    public void onSlideEnd(SlideDetector detector) {
        if (null != mSlideEndResponse) {
            mSlideEndResponse.onSlideEndResponse();
        }
    }

    @Override
    public boolean onSlide(SlideDetector detector) {
        return true;
    }

    public void setSlideEndResponse(SlideEndResponse slideEndResponse) {
        mSlideEndResponse = slideEndResponse;
    }

    public interface SlideEndResponse {
        public void onSlideEndResponse();
    }
}
