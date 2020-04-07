package com.cydroid.note.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.cydroid.note.R;
import com.cydroid.note.common.NoteUtils;


public final class Config {

    public static class NoteCard {
        private static NoteCard sInstance;
        private static Drawable sDefaultNoteCardImage;
        public int mItemWidth;
        public int mImageWidth;
        public int mImageHeight;

        public NoteCard(Context context) {
            Resources rs = context.getResources();
            int screenWidth = NoteUtils.sScreenWidth;
            int activityHorizonMargin = rs.getDimensionPixelSize(R.dimen.home_activity_horizontal_margin);
            int note_item_horizon_margin = rs.getDimensionPixelSize(R.dimen.home_note_item_gap);
            int column = rs.getInteger(R.integer.home_note_item_column);
            mItemWidth = (screenWidth - activityHorizonMargin * 2 - note_item_horizon_margin * (column - 1)) / 2;
            mImageWidth = mItemWidth;
            mImageHeight = rs.getDimensionPixelSize(R.dimen.home_note_item_image_height);

        }

        public static synchronized NoteCard get(Context context) {
            if (sInstance == null) {
                sInstance = new NoteCard(context);
            }
            return sInstance;
        }

        public static synchronized Drawable getDefaultNoteCardImage(Context context) {
            if (sDefaultNoteCardImage == null) {
                sDefaultNoteCardImage = ContextCompat.getDrawable(context,
                        R.drawable.note_card_default_image);
            }
            return sDefaultNoteCardImage;
        }
    }

    public static class EditPage {
        private static EditPage sInstance;
        private static Drawable sDefaultImageDrawable;
        public int mImageWidth;
        public int mImageHeight;
        public int mImageShiftSize;

        public int mListItemImageWidth;
        public int mListItemImageHeight;

        public int mSoundWidth;
        public int mSoundHeight;
        public int mSoundPointOffsetLeft;
        public int mSoundPointOffsetRight;
        public int mSoundPointColor;
        public int mSoundPointRadius;
        public int mSoundDurationOffsetLeft;
        public int mSoundDurationSize;
        public int mSoundDurationColor;

        public int mBillWidth;
        public int mBillForegroundColor;

        public int mTimeSize;
        public int mTimeColor;

        public int mReminderSize;
        public int mReminderColor;
        public int mReminderGap;

        public int mSignatureSize;
        public int mSignatureColor;

        public EditPage(Context context) {
            Resources rs = context.getResources();
            int screenWidth = NoteUtils.sScreenWidth;
            int editMargin = rs.getDimensionPixelSize(R.dimen.edit_note_content_padding_left);
            int cursorWidth = rs.getDimensionPixelSize(R.dimen.edit_note_item_cursor_width);
            mImageHeight = rs.getDimensionPixelSize(R.dimen.edit_note_item_image_height);
            mImageWidth = screenWidth - editMargin * 2 - cursorWidth;
            mImageShiftSize = rs.getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);

            mListItemImageWidth = rs.getDimensionPixelOffset(R.dimen.list_note_item_iamge_width);
            mListItemImageHeight = rs.getDimensionPixelOffset(R.dimen.list_note_item_iamge_height);

            mSoundWidth = mImageWidth;
            mSoundHeight = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_height);
            mSoundPointRadius = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_point_radius);
            mSoundPointOffsetLeft = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_point_offset_left);
            mSoundPointOffsetRight = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_point_offset_right);
            mSoundPointColor = ContextCompat.getColor(context, R.color.edit_note_item_sound_point_color);
            mSoundDurationOffsetLeft = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_duration_offset_left);
            mSoundDurationSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_duration_size);
            mSoundDurationColor = ContextCompat.getColor(context, R.color.edit_note_item_sound_duration_color);
            mBillWidth = rs.getDimensionPixelSize(R.dimen.edit_note_item_bill_width);
            mBillForegroundColor = ContextCompat.getColor(context, R.color.edit_note_bill_foreground_color);
            mTimeSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_time_size);
            mTimeColor = ContextCompat.getColor(context, R.color.edit_note_item_time_color);
            mReminderSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_reminder_size);
            mReminderColor = ContextCompat.getColor(context, R.color.edit_note_item_reminder_color);
            mReminderGap = rs.getDimensionPixelSize(R.dimen.edit_note_item_reminder_gap);
            mSignatureSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_signature_size);
            mSignatureColor = ContextCompat.getColor(context, R.color.edit_note_item_signature_color);

        }

        public static synchronized EditPage get(Context context) {
            if (sInstance == null) {
                sInstance = new EditPage(context);
            }
            return sInstance;
        }

        public static synchronized Drawable getDefaultImageDrawable(Context context) {
            if (sDefaultImageDrawable == null) {
                sDefaultImageDrawable = ContextCompat.getDrawable(context,
                        R.drawable.image_span_default_drawable);
            }
            return sDefaultImageDrawable;
        }
    }

    public static class SoundImageSpanConfig {
        private static SoundImageSpanConfig sInstance;

        public int mImageShiftSize;
        public int mDotCircleX;
        public int mDotCircleY;
        public int mDotCircleRadius;
        public int mTextColor;
        public int mDotColor;
        public float mTextSize;
        public int mTextLeftMargin;

        public SoundImageSpanConfig(Context context) {
            mImageShiftSize = context.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
            mDotCircleX = context.getResources().getDimensionPixelSize(R.dimen.red_dot_circle_x);
            mDotCircleY = context.getResources().getDimensionPixelSize(R.dimen.red_dot_circle_y);
            mDotCircleRadius = context.getResources().getDimensionPixelSize(R.dimen.red_dot_circle_radius);
            mTextColor = ContextCompat.getColor(context, R.color.sound_record_time_textcolor);
            mDotColor = ContextCompat.getColor(context, R.color.sound_dot_color);
            mTextSize = context.getResources().getDimensionPixelSize(R.dimen.record_time_text_size);
            mTextLeftMargin = context.getResources().getDimensionPixelSize(R.dimen.record_text_left_margin);

        }

        public synchronized static SoundImageSpanConfig get(Context context) {
            if (sInstance == null) {
                sInstance = new SoundImageSpanConfig(context);
            }
            return sInstance;
        }
    }

    public static class WidgetPage {

        private static WidgetPage sWidgetPage;

        public int mWidth;
        public int mHeight;

        private WidgetPage(Context context) {
            mWidth = context.getResources().getDimensionPixelOffset(R.dimen.widget_width_4x);
            mHeight = context.getResources().getDimensionPixelOffset(R.dimen.widget_photo_height_4x);
        }

        public static WidgetPage getInstance(Context context) {
            if (null == sWidgetPage) {
                sWidgetPage = new WidgetPage(context);
            }
            return sWidgetPage;
        }

    }

}
