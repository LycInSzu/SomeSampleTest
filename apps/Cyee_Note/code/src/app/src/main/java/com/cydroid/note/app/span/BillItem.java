package com.cydroid.note.app.span;

import android.content.Context;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.WrapTogetherSpan;
import android.view.View;

import com.cydroid.note.app.Config;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.utils.EditUtils;
import com.cydroid.note.app.view.NoteContentEditText;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class BillItem implements ParagraphStyle, WrapTogetherSpan, JsonableSpan {
    private static final String TAG = "BillItem";
    public static final String CHECKED_KEY = "checked";
    private static final BillItem[] EMPTY_ITEM = new BillItem[0];
    private SpannableStringBuilder mText;
    private Context mContext;
    private boolean mChecked = false;
    private StrikethroughSpan mStrikethroughSpan;
    private ForegroundColorSpan mForegroundColorSpan;
    private int mBillForegroundColor;
    private BillSpanWatcher mSpanWatcher;
    private OnImageSpanChangeListener mListener;
    private boolean mChanged = false;


    public BillItem(Context context, SpannableStringBuilder text) {
        mContext = context;
        mText = text;
        mBillForegroundColor = Config.EditPage.get(context).mBillForegroundColor;
    }

    public void init(boolean checked, int pStart, int pEnd) {
        mChecked = checked;
        initSpans(pStart, pEnd);
    }

    //precondition: haven't any spans, we set them.
    private void initSpans(int start, int end) {
        int flags = Spanned.SPAN_INCLUSIVE_INCLUSIVE;
        mText.setSpan(this, start, end, flags);

        BillImageSpan billSpan = new BillImageSpan(mContext, this);
        EditUtils.insertBillImageSpan(mText, billSpan, start);

        end = mText.getSpanEnd(this);
        if (mChecked) {
            setStrikethroughSpan(start, end, flags);
        }
        setBillSpanWatcher(start, end, flags);
    }

    public void setOnImageSpanChangeListener(OnImageSpanChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void updateSpanEditableText(SpannableStringBuilder stringBuilder) {

        if (mText != stringBuilder) {
            mText = stringBuilder;
            int start = mText.getSpanStart(this);
            int end = mText.getSpanEnd(this);
            setBillSpanWatcher(start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    public void destroy() {
        /**
         *  deleting <Image> sign string, will cause internal
         *  deleting of the BillImageSpan and ClickableSpan.
         */
        if (null == mText) {
            return;
        }
        int start = mText.getSpanStart(this);
        String text = mText.toString();
        if (text.startsWith(Constants.MEDIA_BILL, start)) {
            mText.delete(start, start + Constants.MEDIA_BILL.length());
            return;
        }
        removeAuxSpans();
    }

    public void onBillClick(View widget) {
        mChecked = !mChecked;
        toggleStrikethroughSpan();
        setChanged(true);
        if (widget instanceof NoteContentEditText) {
            int end = mText.getSpanEnd(BillItem.this);
            ((NoteContentEditText) widget).shouldFixCursor(end);
        }
        if (mListener != null) {
            mListener.onImageChanged();
        }
    }

    public static BillItem[] get(SpannableStringBuilder text, int start, int end) {
        BillItem[] items = text.getSpans(start, end, BillItem.class);
        if (items.length == 1) {
            BillItem item = items[0];
            int iStart = text.getSpanStart(item);
            String iText = text.toString();
            if (!iText.startsWith(Constants.MEDIA_BILL, iStart)) {
                item.removeBillItem();
                return EMPTY_ITEM;
            }
        }
        return items;
    }

    public void removeBillItem() {
        removeImageSpan();
    }

    public void adjustRange(int newStart, int newEnd) {
        //wanghaiyan 2017-9-18 modify for 212958 begin
        if (newEnd < newStart){
            return;
        }
	    //wanghaiyan 2017-9-18 modify for 212958 end
        SpannableStringBuilder text = mText;
        text.setSpan(this, newStart, newEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        if (mStrikethroughSpan != null) {
            text.setSpan(mStrikethroughSpan, newStart, newEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        if (mForegroundColorSpan != null) {
            text.setSpan(mForegroundColorSpan, newStart, newEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        text.setSpan(mSpanWatcher, newStart, newEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    public void adjustCursorIfInvalid(int currSelection) {
        if (null == mText) {
            return;
        }
        int start = mText.getSpanStart(this);
        int billImageSpanEnd = start + Constants.MEDIA_BILL.length();
        if (currSelection >= start && currSelection < billImageSpanEnd) {
            Selection.setSelection(mText, billImageSpanEnd);
        }
    }

    private void setBillSpanWatcher(int start, int end, int flags) {
        if (mSpanWatcher == null) {
            mSpanWatcher = new BillSpanWatcher(this);
        }
        mText.setSpan(mSpanWatcher, start, end, flags);
    }

    private void toggleStrikethroughSpan() {
        if (mChecked) {
            int start = mText.getSpanStart(this);
            int end = mText.getSpanEnd(this);
            setStrikethroughSpan(start, end, mText.getSpanFlags(this));
        } else {
            removeStrikethroughSpan();
        }
    }

    private void setStrikethroughSpan(int start, int end, int flags) {
        NoteUtils.assertTrue(mStrikethroughSpan == null);
        StrikethroughSpan strikeSpan = new StrikethroughSpan();
        mText.setSpan(strikeSpan, start, end, flags);
        mStrikethroughSpan = strikeSpan;

        NoteUtils.assertTrue(mForegroundColorSpan == null);
        ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(mBillForegroundColor);
        mText.setSpan(foregroundSpan, start, end, flags);
        mForegroundColorSpan = foregroundSpan;
    }

    private void removeStrikethroughSpan() {
        if (mStrikethroughSpan != null) {
            mText.removeSpan(mStrikethroughSpan);
            mStrikethroughSpan = null;
        }
        if (mForegroundColorSpan != null) {
            mText.removeSpan(mForegroundColorSpan);
            mForegroundColorSpan = null;
        }
    }

    private void removeImageSpan() {
        if (null == mText) {
            return;
        }
        int start = mText.getSpanStart(this);
        int end = mText.getSpanEnd(this);
        BillImageSpan[] spans = mText.getSpans(start, end, BillImageSpan.class);
        for (BillImageSpan span : spans) {
            mText.removeSpan(span);
        }
    }

    private void removeAuxSpans() {
        if (null == mText) {
            return;
        }
        mText.removeSpan(mSpanWatcher);
        mText.removeSpan(this);
        if (mChecked) {
            NoteUtils.assertTrue(mStrikethroughSpan != null);
            NoteUtils.assertTrue(mForegroundColorSpan != null);
            removeStrikethroughSpan();
        }
    }

    public void setChanged(boolean changed) {
        synchronized (BillItem.this) {
            mChanged = changed;
        }
    }

    public boolean getAndResetChanged() {
        synchronized (BillItem.this) {
            boolean changed = mChanged;
            mChanged = false;
            return changed;
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void writeToJson(JSONObject jsonObject) throws JSONException {
        int start = mText.getSpanStart(this);
        int end = mText.getSpanEnd(this);
        int flags = mText.getSpanFlags(this);

        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, flags);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, BillItem.class.getName());
        jsonObject.put(CHECKED_KEY, mChecked);
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

    /**
     * A JsonableSpan must have a static public field named AAPPLYER
     */
    public static final JsonableSpan.Applyer<BillItem> APPLYER = new JsonableSpan.Applyer<BillItem>() {

        @Override
        public BillItem applyFromJson(JSONObject json, SpannableStringBuilder builder,
                                      Context context, boolean isEncrypt) throws JSONException {
            int start = json.getInt(DataConvert.SPAN_ITEM_START);
            int end = json.getInt(DataConvert.SPAN_ITEM_END);
            if (end > builder.length()) {
                end = builder.length();
            }
            int flag = json.getInt(DataConvert.SPAN_ITEM_FLAG);
            boolean checked = json.getBoolean(CHECKED_KEY);
            BillItem item = new BillItem(context, builder);
            item.init(checked, start, end);
            builder.setSpan(item, start, end, flag);
            return item;
        }
    };


    private class BillSpanWatcher implements SpanWatcher {
        private final BillItem mItem;

        BillSpanWatcher(BillItem item) {
            mItem = item;
        }

        @Override
        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        @Override
        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what instanceof BillImageSpan) {
                checkDeleteRedundancyBillTag(text, start, end);
                mItem.removeAuxSpans();
            }
        }

        @Override
        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        }

        private void checkDeleteRedundancyBillTag(Spannable spanText, int start, int end) {
            int redundancyBillTagLength = Constants.MEDIA_BILL.length() - 1;
            if (redundancyBillTagLength == (end - start)) {
                String redundancyBillTag = Constants.MEDIA_BILL.substring(0, redundancyBillTagLength);
                String text = spanText.toString();
                if (!TextUtils.isEmpty(text) && text.length() >= end) {
                    String substring = text.substring(start, end);
                    boolean hasRedundancyBillTag = redundancyBillTag.equals(substring);
                    if (hasRedundancyBillTag) {
                        mText.delete(start, end);
                    }
                }
            }
        }
    }
}
