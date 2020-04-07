package com.cydroid.note.app.span;

import android.app.filecrypt.zyt.filesdk.FileCryptSDK;
import android.app.filecrypt.zyt.services.CryptWork;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.Config;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.attachment.SoundPlayer;
import com.cydroid.note.common.BitmapUtils;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.ZYTGroupListener;
import com.cydroid.note.encrypt.ZYTProgressListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class SoundImageSpan extends ReplacementSpan implements AbstractClickSpan, JsonableSpan, OnlyImageSpan {
    public static final String ORIGIN_PATH = "origin_path";
    public static final String SOUND_DURATION = "duration"; //in Second
    private static final SoundImageSpan[] EMPTY_ITEM = new SoundImageSpan[0];
    private Rect mRect = new Rect();
    private SpannableStringBuilder mText;
    private Context mContext;
    private int mTextAccent;
    private SoundSpanWatcher mSpanWatcher;
    private String mOriginPath;
    private int mDurationInSec;
    private int mSoundWidth = 0;
    private int mSoundHeight = 0;
    private int mImageShiftSize = 0;
    private int mDotCircleX = 0;
    private int mDotCircleY = 0;
    private int mDotCircleRadius = 0;
    private int mTextColor = 0;
    private int DOT_COLOR = 0;
    private float mTextSize = 0;
    private int mTextLeftMargin = 0;
    private int PLAY_ICON_X = 0;
    private Drawable mSavedDrawable;
    private boolean mIscrypted;
    private SoundPlayer mPlayer;

    public SoundImageSpan(Context context, SpannableStringBuilder builder, String originPath,
                          int durationInSec, boolean isCrypted) {
        mContext = context;
        mText = builder;
        mOriginPath = originPath;
        mDurationInSec = durationInSec;
        mSpanWatcher = new SoundSpanWatcher();
        Config.EditPage page = Config.EditPage.get(context);
        Config.SoundImageSpanConfig soundImageSpanConfig = Config.SoundImageSpanConfig.get(context);
        mSoundWidth = page.mSoundWidth;
        mSoundHeight = page.mSoundHeight;
        mImageShiftSize = soundImageSpanConfig.mImageShiftSize;
        mDotCircleX = soundImageSpanConfig.mDotCircleX;
        mDotCircleY = soundImageSpanConfig.mDotCircleY;
        mDotCircleRadius = soundImageSpanConfig.mDotCircleRadius;
        mTextColor = soundImageSpanConfig.mTextColor;
        DOT_COLOR = soundImageSpanConfig.mDotColor;
        mTextSize = soundImageSpanConfig.mTextSize;
        mTextLeftMargin = soundImageSpanConfig.mTextLeftMargin;
        mRect.set(0, 0, mSoundWidth, mSoundHeight);

        Resources res = context.getResources();
        int textSize = res.getDimensionPixelSize(R.dimen.edit_note_content_text_size);
        Paint paint = BitmapUtils.getTextPaint(textSize, Color.GRAY, false);
        mTextAccent = paint.getFontMetricsInt().ascent;
        mIscrypted = isCrypted;
    }

    public static SoundImageSpan[] get(SpannableStringBuilder text, int start, int end) {
        SoundImageSpan[] items = text.getSpans(start, end, SoundImageSpan.class);
        if (items.length == 1) {
            SoundImageSpan item = items[0];
            int iStart = text.getSpanStart(item);
            String iText = text.toString();
            if (!iText.startsWith(Constants.MEDIA_SOUND, iStart)) {
                item.removeSoundImageSpan();
                return EMPTY_ITEM;
            }
        }
        return items;
    }

    public void adjustCursorIfInvalid(int currSelection) {
        if (null == mText) {
            return;
        }
        int start = mText.getSpanStart(this);
        if (currSelection == start) {
            int newSel = start + Constants.MEDIA_SOUND.length();
            Selection.setSelection(mText, newSel);
        }
    }

    @Override
    public void updateSpanEditableText(SpannableStringBuilder stringBuilder) {
        if (mText != stringBuilder) {
            mText = stringBuilder;
        }
    }

    public void initSpan(int spanStart) {
        setSpanWatcher(spanStart);
    }

    private void setSpanWatcher(int spanStart) {
        mText.setSpan(mSpanWatcher, spanStart, spanStart + Constants.MEDIA_SOUND.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void removeSoundImageSpan() {
        //Chenyee wanghaiyan 2017-12-14 modify for CSW1702A-804 begin
        if(mText != null) {
            mText.removeSpan(SoundImageSpan.this);
        }
        //Chenyee wanghaiyan 2017-12-14 modify for CSW1702A-804 end
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Rect rect = getRect();

        if (fm != null) {
            fm.ascent = -rect.bottom - mTextAccent;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int baseLine, int bottom, Paint paint) {
        int lastLinePos = baseLine - (bottom - top) + mImageShiftSize;
        int nextLinePos = baseLine + mImageShiftSize;
        canvas.save();
        Rect r = getRect();
        int transY = lastLinePos + ((nextLinePos - lastLinePos) - r.height()) / 2;
        if (transY < 0) {
            transY = 0;
        }
        canvas.translate(x, transY);
        drawSoundSpan(canvas, paint);
        drawPlayIcon(canvas);
        canvas.restore();
    }

    private void drawSoundSpan(Canvas canvas, Paint paint) {
        Paint.Style defStyle = paint.getStyle();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        Rect r = getRect();
        canvas.drawRect(r, paint);

        paint.setColor(DOT_COLOR);
        canvas.drawCircle(mDotCircleX, mDotCircleY, mDotCircleRadius, paint);

        String str = NoteUtils.formatTime(mDurationInSec, ":");
        int baseX = mTextLeftMargin;
        int baseY = (int) (r.height() / 2 - (paint.ascent() + paint.descent()) / 2);  //NOSONAR
        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);
        paint.setStyle(defStyle);
        canvas.drawText(str, baseX, baseY, paint);
    }

    private void drawPlayIcon(Canvas canvas) {
        Drawable drawable = getPlayDrawable();
//        PLAY_ICON_X = NoteAppImpl.getContext().getResources().getDimensionPixelSize(R.dimen.record_play_margin_left);
        int transY = getRect().height() / 2 - drawable.getBounds().height() / 2;
        canvas.translate(mSoundWidth - drawable.getIntrinsicWidth(), transY);
//        canvas.translate(PLAY_ICON_X, transY);
        drawable.draw(canvas);
    }

    private Rect getRect() {
        return mRect;
    }

    private Drawable getPlayDrawable() {
        if (mSavedDrawable == null) {
            mSavedDrawable = ContextCompat.getDrawable(NoteAppImpl.getContext(), R.drawable.sound_play_icon);
            mSavedDrawable.setBounds(0, 0, mSavedDrawable.getIntrinsicWidth(), mSavedDrawable.getIntrinsicHeight());
        }

        return mSavedDrawable;
    }

    @Override
    public void onClick(View view) {
        if (mContext == null) {
            return;
        }
        if (null == mPlayer) {
            mPlayer = new SoundPlayer(mContext, mIscrypted, new SoundPlayer.SoundStopoListener() {
                @Override
                public void onStop() {
                    mPlayer = null;
                }
            });
        }

        boolean playFromSecuritySpace = PlatformUtil.isSecurityOS() && mIscrypted;
        String playPath = "";
        if (playFromSecuritySpace) {
            playForSecurityOS(mPlayer);
            return;

        } else if (mIscrypted) {
            playPath = Constants.SOUND_ENCRYPT_PATH + File.separator + EncryptUtil.getFileName(mOriginPath);
        } else {
            playPath = mOriginPath;
        }

        if (NoteUtils.fileNotFound(mContext, playPath)) {
            return;
        }

        mPlayer.launchPlayer(playPath, mDurationInSec);
    }

    @Override
    public boolean isClickValid(TextView widget, MotionEvent event, int lineBottom) {
        int paddingLeft = widget.getTotalPaddingLeft();
        int minX = paddingLeft + 1;
        int maxX = paddingLeft + mSoundWidth - 1;
        int clickX = (int) event.getX();
        int clickY = (int) event.getY();
        return !(clickX < minX || clickX > maxX || clickY >= lineBottom);
    }

    @Override
    public void writeToJson(JSONObject jsonObject) throws JSONException {
        int start = mText.getSpanStart(this);
        int end = mText.getSpanEnd(this);
        int flags = mText.getSpanFlags(this);

        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, flags);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, SoundImageSpan.class.getName());
        jsonObject.put(ORIGIN_PATH, mOriginPath);
        jsonObject.put(SOUND_DURATION, mDurationInSec);
    }

    @Override
    public void recycle() {
        if (mSpanWatcher != null) {
            mText.removeSpan(mSpanWatcher);
            mSpanWatcher = null;
        }
        mText = null;
        mContext = null;
    }

    public void stop() {
        if (null != mPlayer) {
            mPlayer.completePlayer();
            mPlayer = null;
        }
    }

    /**
     * A JsonableSpan must have a static public field named AAPPLYER
     */
    public static final JsonableSpan.Applyer<SoundImageSpan> APPLYER = new JsonableSpan.Applyer<SoundImageSpan>() {

        @Override
        public SoundImageSpan applyFromJson(JSONObject json, SpannableStringBuilder builder,
                                            Context context, boolean isCrypt) throws JSONException {
            int start = json.getInt(DataConvert.SPAN_ITEM_START);
            int end = json.getInt(DataConvert.SPAN_ITEM_END);
            int flag = json.getInt(DataConvert.SPAN_ITEM_FLAG);
            String originPath = json.getString(ORIGIN_PATH);
            int durationInSec = json.getInt(SOUND_DURATION);


            SoundImageSpan span = new SoundImageSpan(context, builder, originPath, durationInSec, isCrypt);
            builder.setSpan(span, start, end, flag);
            span.initSpan(start);
            return span;
        }
    };

    private void playForSecurityOS(final SoundPlayer player) {
        ArrayList<CryptWork> workList = new ArrayList<>();
        final String dstPath = new StringBuilder().append(Constants.NOTE_MEDIA_SOUND_PATH.getAbsolutePath())
                .append(File.separator)
                .append(EncryptUtil.getFileName(mOriginPath))
                .toString();
        String srcFilePath = EncryptUtil.getSecuritySpacePath(mOriginPath);
        CryptWork cryptWork = new CryptWork(srcFilePath, dstPath, false);
        FileCryptSDK.setProgressCallback(cryptWork, new ZYTProgressListener(srcFilePath,
                false, new ZYTProgressListener.ZYTProgressCompleteListener() {
            @Override
            public void onCompleted() {
                player.launchPlayer(dstPath, mDurationInSec);
            }
        }));
        workList.add(cryptWork);
        FileCryptSDK.addTasks(workList, new ZYTGroupListener());
    }

    private class SoundSpanWatcher implements SpanWatcher {
        @Override
        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        @Override
        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what == SoundImageSpan.this) {
                checkDeleteRedundancySoundTag(text, start, end);
                if (mSpanWatcher != null) {
                    mText.removeSpan(mSpanWatcher);
                    mSpanWatcher = null;
                }
                NoteUtils.deleteFile(mOriginPath);
                mPlayer = null;
            }
        }

        @Override
        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        }

        private void checkDeleteRedundancySoundTag(Spannable spanText, int start, int end) {
            int redundancySoundTagLength = Constants.MEDIA_SOUND.length() - 1;
            if (redundancySoundTagLength == (end - start)) {
                String redundancySoundTag = Constants.MEDIA_SOUND.substring(0, redundancySoundTagLength);
                String text = spanText.toString();
                if (!TextUtils.isEmpty(text) && text.length() >= end) {
                    String substring = text.substring(start, end);
                    boolean hasRedundancySoudnTag = redundancySoundTag.equals(substring);
                    if (hasRedundancySoudnTag) {
                        mText.delete(start, end);
                    }
                }
            }
        }
    }

}
