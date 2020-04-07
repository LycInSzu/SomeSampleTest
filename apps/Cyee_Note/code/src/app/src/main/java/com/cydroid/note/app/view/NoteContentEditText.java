package com.cydroid.note.app.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.cydroid.note.common.Log;

import com.cydroid.note.R;
import com.cydroid.note.app.Config;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.span.BillItem;
import com.cydroid.note.app.span.JsonableSpan;
import com.cydroid.note.app.span.OnImageSpanChangeListener;
import com.cydroid.note.app.span.OnlyImageSpan;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.app.utils.EditUtils;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.common.BitmapUtils;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteParser;
import com.cydroid.note.provider.NoteShareDataManager;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import cyee.widget.CyeeEditText;
//Gionee wanghaiyan 2017-3-31 modify for 96979 begin
import com.cydroid.note.common.FileUtils;
//Gionee wanghaiyan 2017-3-31 modify for 96979 end

public class NoteContentEditText extends CyeeEditText implements OnImageSpanChangeListener {
    private static final String TAG = "NoteContentEditText";
    private static final SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
    private static final SimpleDateFormat DateFormatter_IP= new SimpleDateFormat("dd-MM-yyyy HH:mm");
    //GIONEE wanghaiyan 2017-3-2 modify for 77724 end
    private static int LINE_PADDING_TOP = 0;
    private static int LINE_PADDING_BOTTOM = 0;
    private Context mContext;
    private SpannableStringBuilder mText;
    private Paint mLinePaint;
    private Drawable mReminderDrawable;
    private String mReminder;
    private String mSignature;
    private Paint mReminderPaint;
    private String mShowTime;
    private Paint mNoteTimePaint;
    private Paint mSignaturePaint;
    private Drawable mTagDrawable;
    private int mNoteTimeWidth;
    private int mNoteSignatureWidth;
    private boolean mTextChanged = false;
    private TextWatcher mTextWatcher;
    private OnPrimaryClipChangedListener mClipDataChangedListener;
    private int mSelection;
    private boolean mShouldFixCursor = false;
    private boolean mLocked = false;
    private boolean mHasDrawAmiTag = false;
    private float mCustomerSpacingAdd;
    private double mMaxContentSize;
    private int mReacheMaxLengthCharacterCount;
    private int mPaddingBottomNoSignature;
    private int mPaddingButtomSignature;
    private static final double DEFAULT_MAX_CONTENT_SIZE_IN_M = 0.5 * Constants.ONE_M;
    //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
    public static final int DEFAULT_MAX_CONTENT_SIZE = 10000;
    private ToastManager mToastManager;

    public NoteContentEditText(Context context) {
        this(context, null);
    }

    public NoteContentEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public NoteContentEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NoteContentEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
        setSaveEnabled(false);
        adjustViewSizeBySignature();
    }

    private void init(Context context) {
        mToastManager = new ToastManager(context);
			mTagDrawable = ContextCompat.getDrawable(context, R.drawable.odm_share_content_tag);
        if (Build.VERSION.SDK_INT >= 21) {
            setElegantTextHeight(true);
        }
        mContext = context;
        mText = (SpannableStringBuilder) getText();
        LINE_PADDING_TOP = mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_top);
        LINE_PADDING_BOTTOM = mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
        mCustomerSpacingAdd = LINE_PADDING_BOTTOM + LINE_PADDING_TOP;
        int id = getResources().getIdentifier("config_cursorWindowSize", "integer", "android");
        mMaxContentSize = Resources.getSystem().getInteger(id) * 1024;
        setLineSpacing(mCustomerSpacingAdd, 1.0f);
        initPaint();
        initClipDataListener();
        mSignature = getSignatureText();
        mPaddingBottomNoSignature = (int) mContext.getResources().getDimension(R.dimen.edit_note_content_padding_bottom);
        mPaddingButtomSignature = (int) mContext.getResources().getDimension(R.dimen.edit_note_content_padding_bottom_signal);
        mNoteSignatureWidth = (int) Math.ceil(mNoteTimePaint.measureText(mSignature));
    }

    public void adjustViewSizeBySignature() {
        if (TextUtils.isEmpty(mSignature)) {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), mPaddingBottomNoSignature);
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), mPaddingButtomSignature);
        }
    }

    private void initPaint() {
        mLinePaint = new Paint(getPaint());
        mLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.note_edit_text_line_color));
        mLinePaint.setAntiAlias(false);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(0.0F);

        Config.EditPage page = Config.EditPage.get(getContext());
        mNoteTimePaint = BitmapUtils.getTextPaint(page.mTimeSize, page.mTimeColor, false);
        mReminderPaint = BitmapUtils.getTextPaint(page.mReminderSize, page.mReminderColor, true);
        mSignaturePaint = BitmapUtils.getTextPaint(page.mSignatureSize, page.mSignatureColor, false);
    }

    public void initClipDataListener() {
        final ClipboardManager clip = (ClipboardManager) NoteAppImpl.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        mClipDataChangedListener = new OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                ClipData clipData = clip.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    CharSequence text = item.getText();
                    if (text == null || text instanceof String) {
                        return;
                    }

                    String newData = text.toString();
                    newData = NoteParser.replaceMediaString(newData);
                    clip.setPrimaryClip(ClipData.newPlainText(null, newData));
                }
            }
        };

        clip.addPrimaryClipChangedListener(mClipDataChangedListener);
    }

    public void initWatcher(final View shareView, final View deleteView) {
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*
                if (checkInputContentExceedMaxSize(s)) {
                    setInputContentMaxSize();//wanghaiyan
                    if (!TextUtils.isEmpty(s)) {
//                        setText(s.subSequence(0, start));
                        getText().delete(start, s.length());
                    } else {
                        setText("");
                    }
                    mToastManager.showToast(R.string.max_content_input_mum_limit);
                }
               */
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setHint(R.string.content_hint);
                if (shareView != null) {
                    shareView.setEnabled(!TextUtils.isEmpty(editable.toString()));
                }
                if (deleteView != null) {
                    deleteView.setEnabled(!TextUtils.isEmpty(editable.toString()));
                }
                setTextChanged(true);
            }
        };
        addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onImageChanged() {
        invalidate();
        setEnabled(false);
        setEnabled(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTextWatcher != null) {
            removeTextChangedListener(mTextWatcher);
        }

        ClipboardManager clip = (ClipboardManager) NoteAppImpl.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clip.removePrimaryClipChangedListener(mClipDataChangedListener);
        mToastManager.destroy();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        updateSpanEditableText();
    }

    private void updateSpanEditableText() {
        JsonableSpan[] spans = mText.getSpans(0, mText.length(), JsonableSpan.class);
        for (JsonableSpan span : spans) {
            span.updateSpanEditableText(mText);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
        mText = (SpannableStringBuilder) getText();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP && mShouldFixCursor) {
            mShouldFixCursor = false;
            int selection = getSelectionEnd();
            if (selection != mSelection) {
                if (isSelectPositionReachMaxSize() && mSelection > getText().length()) {
                    mSelection = getText().length();
                }
                setSelection(mSelection);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDecoration(canvas);
        super.onDraw(canvas);
    }

    private void drawDecoration(Canvas canvas) {
        int height = getHeight();
        int xStop = getWidth();
        Layout layout = getLayout();
        int lineHeight = getLineHeight();
        int yPos = 0;
        int totalTextLine = layout.getLineCount();
        Paint linePaint = mLinePaint;
        int paddingTop = getCompoundPaddingTop();

        canvas.translate(0, paddingTop);
        for (int i = 0; i < (totalTextLine - 1); i++) {
            int baseline = layout.getLineBaseline(i);
            yPos = baseline + LINE_PADDING_BOTTOM;
            canvas.drawLine(0, yPos, xStop, yPos, linePaint);
        }

        if (layout.getLineStart(totalTextLine - 1) == getText().length()) {
            yPos += lineHeight;
            canvas.drawLine(0, yPos, xStop, yPos, linePaint);
        } else {
            int baseline = layout.getLineBaseline(totalTextLine - 1);
            yPos = baseline + LINE_PADDING_BOTTOM;
            canvas.drawLine(0, yPos, xStop, yPos, linePaint);
        }

        int nextLineYPos;
        while (yPos + lineHeight < height) {
            nextLineYPos = yPos + lineHeight;
            if ((nextLineYPos + paddingTop) >= height) {
                break;
            }
            yPos = nextLineYPos;
            canvas.drawLine(0, yPos, xStop, yPos, linePaint);
        }

        drawSignature(canvas, yPos, lineHeight);
        drawBottom(canvas, yPos, lineHeight);
        canvas.translate(0, -paddingTop);
    }

    private void drawBottom(Canvas canvas, int lastLineYPos, int lineHeight) {
        int baseY = lastLineYPos - lineHeight;
        canvas.save();
        canvas.translate(0, baseY);
        drawNoteTime(canvas, lineHeight);
        if (mHasDrawAmiTag) {
            drawAmiTag(canvas, lineHeight);
        } else {
            drawReminder(canvas, lineHeight);
        }
        canvas.restore();

    }

    private void drawSignature(Canvas canvas, int lastLineYPos, int lineHeight) {
        if (TextUtils.isEmpty(mSignature)) {
            return;
        }
        int baseY = lastLineYPos - 2 * lineHeight;
        canvas.save();
        canvas.translate(0, baseY);
        int x = getWidth() - getPaddingRight() - mNoteSignatureWidth;
        float yPos = lineHeight / (float) 2 - (mSignaturePaint.ascent() -
                mSignaturePaint.descent()) / (float) 2 + mSignaturePaint.descent();
        canvas.drawText(mSignature, x, yPos, mSignaturePaint);
        canvas.restore();
    }

    private String getSignatureText() {
        return NoteShareDataManager.getSignatureText(getContext());
    }

    private void drawNoteTime(Canvas canvas, int lineHeight) {
        if (TextUtils.isEmpty(mShowTime)) {
            return;
        }
        int x = getWidth() - getPaddingRight() - mNoteTimeWidth;
        float yPos = lineHeight / (float) 2 - (mNoteTimePaint.ascent() -
                mNoteTimePaint.descent()) / (float) 2 + mNoteTimePaint.descent();
        canvas.drawText(mShowTime, x, yPos, mNoteTimePaint);
    }

    private void drawAmiTag(Canvas canvas, int lineHeight) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.share_ami_tag_textsize));
        paint.setColor(ContextCompat.getColor(getContext(), R.color.share_ami_tag_text_color));


        int textCoorX = getResources().getDimensionPixelSize(R.dimen.ami_tag_text_coorx);
        float textY = lineHeight / (float) 2;
        Bitmap tag = ((BitmapDrawable) mTagDrawable).getBitmap();
        canvas.drawBitmap(tag, textCoorX, textY, paint);
    }

    private void drawReminder(Canvas canvas, int lineHeight) {
        if (mReminder == null) {
            return;
        }
        int bitmapY = lineHeight / 2;
        int bitmapX = getPaddingLeft();
        canvas.translate(bitmapX, bitmapY);
        mReminderDrawable.draw(canvas);
        canvas.translate(-bitmapX, -bitmapY);
        int textX = getPaddingLeft() + mReminderDrawable.getIntrinsicWidth() + Config.EditPage.get(getContext()).mReminderGap;
        float textY = lineHeight / (float) 2 - (mReminderPaint.ascent() - mReminderPaint.descent()) / (float) 2
                + mReminderPaint.descent();
        canvas.drawText(mReminder, textX, textY, mReminderPaint);
    }

    public void setAmiTagEnable(boolean isDraw) {
        mHasDrawAmiTag = isDraw;
    }

    public void setReminderTime(long reminderTime) {
        if (reminderTime == NoteItem.INVALID_REMINDER) {
            mReminder = null;
            invalidate();
            return;
        }
		//GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
 		//GIONEE wanghaiyan 2017-4-25 modify for 122967 begin
		if(NoteUtils.gnIPFlag){
			mReminder = NoteUtils.formatDateTime(reminderTime, new SimpleDateFormat(NoteParser.DATE_FORMAT_IP));
		}else{
			mReminder = NoteUtils.formatDateTime(reminderTime, new SimpleDateFormat(NoteParser.DATE_FORMAT));
		}
		//GIONEE wanghaiyan 2017-4-25 modify for 122967 end
		//GIONEE wanghaiyan 2017-3-2 modify for 77724 end
        invalidate();
        if (mReminderDrawable == null) {
            mReminderDrawable = ContextCompat.getDrawable(getContext(), R.drawable.edit_page_reminder);
            mReminderDrawable.setBounds(0, 0, mReminderDrawable.getIntrinsicWidth(), mReminderDrawable.getIntrinsicHeight());
        }
    }

    public void setNoteTime(long noteTime) {
		//GIONEE wanghaiyan 2017-4-25 modify for 122967 begin
        //mShowTime = NoteUtils.formatDateTime(noteTime, DateFormatter);
	 	//GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
	 	if(NoteUtils.gnIPFlag){
	 		mShowTime = NoteUtils.formatDateTime(noteTime, new SimpleDateFormat(NoteParser.DATE_FORMAT_IP));
		}else{
	 		mShowTime = NoteUtils.formatDateTime(noteTime, new SimpleDateFormat(NoteParser.DATE_FORMAT));
	 	}
	 	//GIONEE wanghaiyan 2017-3-2 modify for 77724 end
		//GIONEE wanghaiyan 2017-4-25 modify for 122967 end
        //Chenyee wanghaiyan 2017-12-1 modify for SW17W16A-2093 begin
        mShowTime = NoteUtils.formateTime(mShowTime,mContext);
        //Chenyee wanghaiyan 2017-12-1 modify for SW17W16A-2093 end
        mNoteTimeWidth = (int) Math.ceil(mNoteTimePaint.measureText(mShowTime));
    }

    public void shouldFixCursor(int selection) {
        mShouldFixCursor = true;
        mSelection = selection;
    }

    public void setTextChanged(boolean changed) {
        synchronized (NoteContentEditText.this) {
            mTextChanged = changed;
        }
    }

    public boolean getAndResetTextChanged() {
        boolean billItemChanged = isBillItemChanged();
        synchronized (NoteContentEditText.this) {
            boolean textChanged = mTextChanged;
            mTextChanged = false;
            return billItemChanged || textChanged;
        }
    }

    private boolean isBillItemChanged() {
        boolean changed = false;
        SpannableStringBuilder text = mText;
        BillItem[] billItems = text.getSpans(0, text.length(), BillItem.class);
        for (BillItem billItem : billItems) {
            if (billItem.getAndResetChanged()) {
                changed = true;
            }
        }
        return changed;
    }

    //next code to handle when selection or text is changed
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (mText == null) {
            return;
        }
        if (selStart != selEnd) {
            return;
        }
        if (isLocked()) {
            return;
        }

        try {
            lock();
            adjustCursorIfNeed(selStart);
        } finally {
            unLock();
        }
    }

    private void adjustCursorIfNeed(int currSelection) {
        /**
         * consider when cursor positioned ahead of a BillItem,
         * if that happen, we should adjust cursor.
         */
        SpannableStringBuilder text = mText;
        BillItem[] bills = BillItem.get(text, currSelection, currSelection);
        if (bills.length == 1) {
            BillItem bill = bills[0];
            bill.adjustCursorIfInvalid(currSelection);
            return;
        }

        PhotoImageSpan[] photoSpans = PhotoImageSpan.get(text, currSelection, currSelection);
        if (photoSpans.length == 1) {
            PhotoImageSpan photoSpan = photoSpans[0];
            photoSpan.adjustCursorIfInvalid(currSelection);
            return;
        }

        SoundImageSpan[] soundSpans = SoundImageSpan.get(text, currSelection, currSelection);
        if (soundSpans.length == 1) {
            SoundImageSpan soundSpan = soundSpans[0];
            soundSpan.adjustCursorIfInvalid(currSelection);
            return;
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (isLocked()) {
            return;
        }

        try {
            lock();
            SpannableStringBuilder builder = (SpannableStringBuilder) text;

            if (createNewBillItemIfNeed(builder, start, lengthBefore, lengthAfter)) {
                return;
            }
            insertLineBreakIfNeed(builder, start, lengthBefore, lengthAfter);
        } finally {
            unLock();
        }
    }

    private boolean createNewBillItemIfNeed(SpannableStringBuilder text, int start, int lengthBefore, int lengthAfter) {
        boolean inputLineBreak = (lengthBefore == 0 && lengthAfter == 1 && text.charAt(start) == Constants.CHAR_NEW_LINE);
        if (!inputLineBreak) {
            return false;
        }
        if (isSelectPositionReachMaxSize()) {
            mToastManager.showToast(R.string.max_content_input_mum_limit);
            return false;
        }

        BillItem[] bills = BillItem.get(text, start, start);
        if (bills.length != 1) {
            return false;
        }
        BillItem bill = bills[0];
        int billStart = text.getSpanStart(bill);
        int billEnd = text.getSpanEnd(bill);
        bill.adjustRange(billStart, start);

        int newBillStart = start + 1;
        int newBillEnd = billEnd;
        if (start == billEnd) {
            newBillEnd = start + 1;
        }
        createBillItem(text, newBillStart, newBillEnd);
        return true;
    }

    private void insertLineBreakIfNeed(SpannableStringBuilder text, int start, int lengthBefore, int lengthAfter) {
        int selStart = start;
        int selEnd = start;
        if (lengthAfter > 0) {
            selEnd = start + lengthAfter;
        }
        boolean insertByDelete = (lengthBefore > 0 && lengthAfter <= 0);
        ensurePreSpan(text, selStart, insertByDelete);
        ensureNextSpan(text, selEnd);
    }

    private void ensurePreSpan(SpannableStringBuilder text, int currSelection, boolean insertByDelete) {
        if (ensurePreOnlyImageSpan(text, currSelection, insertByDelete)) {
            return;
        }
        ensurePreBillItem(text, currSelection);
    }

    private void ensureNextSpan(SpannableStringBuilder text, int currSelection) {
        if (ensureNextOnlyImageSpan(text, currSelection)) {
            return;
        }
        ensureNextBillItem(text, currSelection);
    }

    private boolean ensurePreOnlyImageSpan(SpannableStringBuilder text, int currSelection, boolean insertByDelete) {
        OnlyImageSpan[] onlyImageSpans = text.getSpans(currSelection, currSelection, OnlyImageSpan.class);
        if (onlyImageSpans.length <= 0) {
            return false;
        }

        OnlyImageSpan onlyImageSpan = null;
        int imageSpanEnd = 0;
        for (OnlyImageSpan span : onlyImageSpans) {
            imageSpanEnd = text.getSpanEnd(span);
            if (imageSpanEnd == currSelection) {
                onlyImageSpan = span;
                break;
            }
        }
        if (onlyImageSpan == null) {
            return false;
        }

        if (imageSpanEnd != text.length() && text.charAt(imageSpanEnd) != Constants.CHAR_NEW_LINE) {
            BillItem[] bills = text.getSpans(imageSpanEnd, imageSpanEnd, BillItem.class);
            BillItem bill = null;
            int billStart = 0;
            int billEnd = 0;
            if (bills.length == 1) {
                billStart = text.getSpanStart(bills[0]);
                billEnd = text.getSpanEnd(bills[0]);
                if (billStart == imageSpanEnd) {
                    bill = bills[0];
                    bill.adjustRange(billStart + 1, billEnd);
                }
            }
            text.insert(imageSpanEnd, Constants.STR_NEW_LINE);
            if (insertByDelete) {
                Selection.setSelection(text, imageSpanEnd);
            }
            if (bill != null) {
                bill.adjustRange(billStart + 1, billEnd + 1);
            }
        }
        return true;
    }

    private void ensurePreBillItem(SpannableStringBuilder text, int currSelection) {
        BillItem[] bills = BillItem.get(text, currSelection, currSelection);
        if (bills.length <= 0) {
            return;
        }

        BillItem preBill = null;
        int preStart = 0;
        for (BillItem bill : bills) {
            preStart = text.getSpanStart(bill);
            if (preStart < currSelection) {
                preBill = bill;
                break;
            }
        }
        if (preBill == null) {
            return;
        }

        int newEnd = text.length();
        int index = TextUtils.indexOf(text, Constants.CHAR_NEW_LINE, preStart);
        if (index != -1) {
            newEnd = index;
        }
        JsonableSpan[] jsonableSpans = text.getSpans(preStart, newEnd, JsonableSpan.class);
        if (jsonableSpans.length > 1) {
            JsonableSpan span = null;
            for (JsonableSpan temp : jsonableSpans) {
                if (temp != preBill) {
                    span = temp;
                    break;
                }
            }
            newEnd = text.getSpanStart(span);
            int nextEnd = text.getSpanEnd(span);
            if (span instanceof BillItem) {
                ((BillItem) span).adjustRange(newEnd + 1, nextEnd);
            }
            text.insert(newEnd, Constants.STR_NEW_LINE);
            if (span instanceof BillItem) {
                ((BillItem) span).adjustRange(newEnd + 1, nextEnd + 1);
            }
        }
        preBill.adjustRange(preStart, newEnd);
    }

    private boolean ensureNextOnlyImageSpan(SpannableStringBuilder text, int currSelection) {
        OnlyImageSpan[] onlyImageSpans = text.getSpans(currSelection, currSelection, OnlyImageSpan.class);
        if (onlyImageSpans.length <= 0) {
            return false;
        }
        OnlyImageSpan onlyImageSpan = null;
        int start = 0;
        for (OnlyImageSpan span : onlyImageSpans) {
            start = text.getSpanStart(span);
            if (start == currSelection) {
                onlyImageSpan = span;
                break;
            }
        }
        if (onlyImageSpan == null) {
            return false;
        }

        if (start != 0 && text.charAt(start - 1) != Constants.CHAR_NEW_LINE) {
            text.insert(start, Constants.STR_NEW_LINE);
        }
        return true;
    }

    private void ensureNextBillItem(SpannableStringBuilder text, int currSelection) {
        BillItem[] bills = BillItem.get(text, currSelection, currSelection);
        if (bills.length <= 0) {
            return;
        }

        BillItem bill = null;
        int start = 0;
        int end = 0;
        for (BillItem item : bills) {
            start = text.getSpanStart(item);
            end = text.getSpanEnd(item);
            if (start >= currSelection) {
                bill = item;
                break;
            }
        }
        if (bill == null) {
            return;
        }
        if (start != 0 && text.charAt(start - 1) != Constants.CHAR_NEW_LINE) {
            bill.adjustRange(start + 1, end);
            text.insert(start, Constants.STR_NEW_LINE);
            bill.adjustRange(start + 1, end + 1);
        }
    }

    public void toggleBillItem() {
        SpannableStringBuilder text = mText;
	 Log.d(TAG,"text.length()" + text.length());
        if (text.length() <= 0) {
            try {
                lock();
                createBillItem(text, 0, 0);
            } finally {
                unLock();
            }
            return;
        }

        int curPositionStart = getSelectionStart();
        int curPositionEnd = getSelectionEnd();
        if (curPositionStart != curPositionEnd) {
            return;
        }
        if (curPositionStart < 0) {
            setSelection(0);
        }

        int start = curPositionStart < 0 ? 0 : curPositionStart;
        int billStart = start;
        int billEnd = start;

        try {
            lock();

            if (isNextToOnlyImageSpan(text, start, start)) {
                text.insert(start, Constants.STR_NEW_LINE);
                billStart = start + 1;
                billEnd = billStart;
            } else {
                billStart = EditUtils.getCurParagraphStart(text, start);
                billEnd = EditUtils.getCurParagraphEnd(text, start);
            }

            BillItem[] items = BillItem.get(text, billStart, billEnd);

            if (items.length <= 0) {
                createBillItem(text, billStart, billEnd);
            } else {
                BillItem item = items[0];
                item.destroy();
            }
        } finally {
            unLock();
        }
    }

    public boolean isSelectPositionReachMaxSize() {
        boolean isRechMaxLength = false;
        if (0 != mReacheMaxLengthCharacterCount &&
                mReacheMaxLengthCharacterCount >= getSelectionEnd() - 20) {
            isRechMaxLength = true;
        }
        return isRechMaxLength;
    }

    public void insertPhoto(Uri thumbUri, Uri originUri, Bitmap bitmap,
                            boolean isEncrypt) {
        SpannableStringBuilder text = mText;
        int currSelection = getSelectionEnd();
        currSelection = NoteUtils.clamp(currSelection, 0, currSelection);

        try {
            lock();
            currSelection = ensurePreChar(text, currSelection, Constants.CHAR_NEW_LINE);
            PhotoImageSpan span = new PhotoImageSpan(mContext, text, thumbUri, originUri, bitmap, isEncrypt);
            EditUtils.insertPhotoImageSpan(text, span, currSelection);
            span.initSpan(currSelection);
            span.setOnImageSpanChangeListener(this);
            ensureNextChar(text, currSelection + Constants.MEDIA_PHOTO.length(), Constants.CHAR_NEW_LINE);
        //wanghaiyan 2017-9-28 modify for 226343 begin
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
		//wanghaiyan 2017-9-28 modify for 226343 end
        } finally {
            unLock();
        }
    }

    public void insertSound(String originSoundPath, int durationInSec, boolean isEncrypt) {
        SpannableStringBuilder text = mText;
        int currSelection = getSelectionEnd();
        currSelection = NoteUtils.clamp(currSelection, 0, currSelection);

        try {
            lock();
            currSelection = ensurePreChar(text, currSelection, Constants.CHAR_NEW_LINE);
            SoundImageSpan span = new SoundImageSpan(mContext, text, originSoundPath, durationInSec, isEncrypt);
            EditUtils.insertSoundImageSpan(text, span, currSelection);
            span.initSpan(currSelection);
            ensureNextChar(text, currSelection + Constants.MEDIA_SOUND.length(), Constants.CHAR_NEW_LINE);
		//wanghaiyan 2017-9-28 modify for 226343 begin
        } catch (IndexOutOfBoundsException e) {
		//wanghaiyan 2017-9-28 modify for 226343 end
            e.printStackTrace();
        } finally {
            unLock();
        }
    }

    private int ensurePreChar(SpannableStringBuilder text, int currSelection, char c) {
        if (currSelection == 0) {
            return currSelection;
        }

        int preSelection = currSelection - 1;
        if (text.charAt(preSelection) != c) {
            BillItem[] bills = text.getSpans(currSelection, currSelection, BillItem.class);
            BillItem bill = null;
            int start = 0;
            if (bills.length == 1) {
                start = text.getSpanStart(bills[0]);
                if (start < currSelection) {
                    bill = bills[0];
                }
            }
            text.insert(currSelection, Constants.STR_NEW_LINE);
            if (bill != null) {
                bill.adjustRange(start, currSelection);
            }
            currSelection++;
        }
        return currSelection;
    }

    private void ensureNextChar(SpannableStringBuilder text, int currSelection, char c) {
        int length = text.length();
        if (currSelection == length || text.charAt(currSelection) != c) {
            text.insert(currSelection, Constants.STR_NEW_LINE);
        }
    }

    private boolean isNextToOnlyImageSpan(SpannableStringBuilder text, int start, int end) {
        OnlyImageSpan[] spans = text.getSpans(start, end, OnlyImageSpan.class);
        int length = spans.length;
        if (length > 0) {
            int spanEnd = text.getSpanEnd(spans[length - 1]);
            if (spanEnd == end) {
                return true;
            }
        }

        return false;
    }

    private void createBillItem(SpannableStringBuilder text, int pStart, int pEnd) {
        BillItem item = new BillItem(mContext, text);
        item.init(false, pStart, pEnd);
    }

    private void lock() {
        mLocked = true;
    }

    private void unLock() {
        mLocked = false;
    }

    private boolean isLocked() {
        return mLocked;
    }

    private void setInputContentMaxSize(int maxSize) {
        int size = maxSize;
        if (size < DEFAULT_MAX_CONTENT_SIZE) {
            size = DEFAULT_MAX_CONTENT_SIZE;
        }
        mReacheMaxLengthCharacterCount = size;
        setFilters(new InputFilter[]{new TextLengthFilter(size, mToastManager)});
    }

    //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
    public void setInputContentMaxSize() {
        setFilters(new InputFilter[]{new TextLengthFilter(DEFAULT_MAX_CONTENT_SIZE, mToastManager)});
    }
   //gionee wanghaiyan add on 2016-08-03 for CR01739902 end

    private boolean checkInputContentExceedMaxSize(CharSequence s) {
        boolean isExceed = false;
        int currentSize = getCurrentContentSize(s);
        double maxContentSize = getMaxInputContentSize();
        if (currentSize >= maxContentSize * 0.05) {
            isExceed = true;
        }
        return isExceed;
    }

    private int getCurrentContentSize(CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            return 0;
        }

        StringBuilder builder = new StringBuilder(s);
        int size = 0;
        try {
            size = builder.toString().getBytes("utf-8").length;
        } catch (UnsupportedEncodingException e) {

        }
        return size;
    }

    private double getMaxInputContentSize() {
        if (mMaxContentSize == 0) {
            mMaxContentSize = DEFAULT_MAX_CONTENT_SIZE_IN_M;
        }
        return mMaxContentSize;
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        return false;
    }
    //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
    private int neededCharNums=0;
    private static int SPAN_CONTENT_SIZE=20;
    public boolean checkContentKeepMore16CharsRemaing(){
    	int strLength=getText().length();
    	if(DEFAULT_MAX_CONTENT_SIZE-strLength<SPAN_CONTENT_SIZE){
    		neededCharNums=SPAN_CONTENT_SIZE-(DEFAULT_MAX_CONTENT_SIZE-strLength);
    		return false;
    	}
    	return true;
    	
    }
    public int getNeededCharNums(){
    	return neededCharNums;
    }
    //gionee wanghaiyan add on 2016-08-03 for CR01739902 end

}
