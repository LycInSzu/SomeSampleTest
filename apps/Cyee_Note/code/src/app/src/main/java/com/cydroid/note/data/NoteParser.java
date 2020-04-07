package com.cydroid.note.data;

import android.net.Uri;
import android.text.TextUtils;

import com.cydroid.note.common.Log;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.SlidingWindow.NoteEntry;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.trash.data.TrashNoteItem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;

public class NoteParser {
    private static final String TAG = "NoteParser";
    public static final String EMPTY_STRING = "";
    //GIONEE wanghaiyan 2016-12-23 modify for 53637 begin
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    //GIONEE wanghaiyan 2016-12-23 modify for 53637 end
    //GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
    public static final String DATE_FORMAT_IP= "dd-MM-yyyy HH:mm";
    //GIONEE wanghaiyan 2017-3-2 modify for 77724 end
    public void parseNote(NoteEntry entry, NoteItem item) {
        if (item == null || null == entry) {
            return;
        }

        entry.title = item.getTitle();

        getOriginContent(item.getContent(), entry);
        entry.content = parserText(entry.content);
        if (!TextUtils.isEmpty(entry.content) && entry.content.length() > 100) {
            entry.content = entry.content.substring(0, 100);
        }

        entry.reminder = item.getDateTimeReminder();
        entry.timeMillis = item.getDateTimeModified();
        entry.isEncrypt = item.getIsEncrypted();
        entry.encrytRemindReadState = item.getEncrytRemindReadState();
        if (item instanceof TrashNoteItem) {
            long dateDeleted = ((TrashNoteItem) item).getDateTimeDeleted();
            entry.time = NoteUtils.formatLeftDays(NoteAppImpl.getContext(), dateDeleted, TrashNoteItem.KEEP_DAYS);
        } 
		//GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
		else if (NoteUtils.gnIPFlag) {
			entry.time = NoteUtils.formatDateTime(item.getDateTimeModified(), new SimpleDateFormat(DATE_FORMAT_IP));	
		}
		//GIONEE wanghaiyan 2017-3-2 modify for 77724 end
		else {
			entry.time = NoteUtils.formatDateTime(item.getDateTimeModified(), new SimpleDateFormat(DATE_FORMAT));
		}
    }

    public void parseNoteContent(String jsContent, NoteEntry entry) {
        getOriginContent(jsContent, entry);
        entry.content = replaceMediaString(entry.content);
        if (!TextUtils.isEmpty(entry.content) && entry.content.length() > 500) {
            entry.content = entry.content.substring(0, 500);
        }
    }

    //get Content, Image or Video file path , mediaType
    public void getOriginContent(String json, NoteEntry entry) {
        if (json == null || json.length() == 0) {
            entry.mediaType = NoteItem.MEDIA_TYPE_NONE;
            return;
        }

        try {
            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            entry.content = jsonObject.getString(DataConvert.JSON_CONTENT_KEY);
            getOriginMedia(jsonObject, entry, entry.content);
        } catch (Exception e) {
            Log.d(TAG, "getOriginContent fail : " + e);
        }
    }

    private void getOriginMedia(JSONObject jsonObject, NoteEntry entry, String content) {
        try {
            JSONArray spans = jsonObject.optJSONArray(DataConvert.JSON_SPANS_KEY);
            if (spans == null) {
                entry.mediaType = NoteItem.MEDIA_TYPE_NONE;
                return;
            }
            int start = 0;
            int mediaType = NoteItem.MEDIA_TYPE_NONE;
            String thumbUri = null;
            String originUri = null;
            int position = content.length();
            int length = spans.length();
            String noteImageSpan = PhotoImageSpan.class.getName();
            for (int i = 0; i < length; i++) {
                JSONObject json = spans.getJSONObject(i);
                String type = json.getString(DataConvert.SPAN_ITEM_TYPE);
                if (type.equals(noteImageSpan)) {
                    start = json.getInt(DataConvert.SPAN_ITEM_START);
                    if (start < position) {
                        position = start;
                        mediaType = NoteItem.MEDIA_TYPE_IMAGE;
                        thumbUri = json.getString(PhotoImageSpan.THUMB_URI);
                        originUri = json.getString(PhotoImageSpan.ORIGIN_URI);
                    }
                }
            }
            entry.mediaType = mediaType;
            if (thumbUri != null) {
                entry.thumbnailUri = Uri.parse(thumbUri);
            }
            if (originUri != null) {
                entry.originUri = Uri.parse(originUri);
            }
        } catch (Exception e) {
            Log.d(TAG, "getOriginMedia fail : " + e);
        }

    }

    public static String replaceMediaString(String origin) {
        if (origin == null) {
            return null;
        }

        if ("<".equals(origin)) {
            return EMPTY_STRING;
        }

        origin = origin.replaceAll(Constants.MEDIA_PHOTO + "\n", EMPTY_STRING);
        origin = origin.replaceAll(Constants.MEDIA_SOUND + "\n", EMPTY_STRING);
        origin = origin.replaceAll(Constants.MEDIA_BILL, EMPTY_STRING);
        origin = origin.replaceAll(Constants.MEDIA_PHOTO, EMPTY_STRING);
        origin = origin.replaceAll(Constants.MEDIA_SOUND, EMPTY_STRING);
        return origin;
    }

    public static String parserText(String origin) {
        if (origin == null) {
            return null;
        }
        origin = replaceMediaString(origin);

        String[] result = origin.split(Constants.STR_NEW_LINE);
        for (String text : result) {
            if (!TextUtils.isEmpty(text)) {
                return text;
            }
        }
        return origin;
    }
}
