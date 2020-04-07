package com.cydroid.note.app;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Spannable;
import android.text.TextUtils;
import com.cydroid.note.common.Log;

import com.gionee.framework.log.Logger;
import com.cydroid.note.app.attachment.AttachmentSelector;
import com.cydroid.note.app.span.BillItem;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.common.BitmapUtils;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.DecodeUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteShareDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class BuiltInNote {
    private static final String TAG = "BuiltInNote";
    private static final String IMAGE_SPAN = ":image:";
    private static final String BILL_SPAN = ":bill:";
    private static final String TYPE_IMAGE = "image";
    private static final String TYPE_BILL = "bill";
    private static final int TYPE_IMAGE_INT = 1;
    private static final int TYPE_BILL_INT = 2;

    private static final String SPAN_SPLIT = "::";
    private static final String SPAN_ITEM_SPLIT = "-";
    private static final String LABEL_SPLIT = "-";
    private static final String CONFIG_FILE_NAME = "init_config.xml";

    private static final String FROM_APP = "app";
    private static final String FROM_SYSTEM = "system";
    private static final String ERROR_FILE = "error_show_fail.error";

    public static void insertBuildInNoteSync() {
        boolean firstLaunch = getIsFirstLaunch(NoteAppImpl.getContext());
        if (!firstLaunch) {
            return;
        }
        initNoteData();
        setIsFirstLaunch(NoteAppImpl.getContext(), false);
        NoteAppImpl.getContext().notifyDbInitComplete();
    }

    public static void insertBuildInNoteAsync() {
        ThreadPool.Job<Void> job = new ThreadPool.Job<Void>() {
            @Override
            public Void run(ThreadPool.JobContext jc) {
                synchronized (BuiltInNote.class) {
                    boolean firstLaunch = getIsFirstLaunch(NoteAppImpl.getContext());
                    if (!firstLaunch) {
                        return null;
                    }
                    initNoteData();
                    setIsFirstLaunch(NoteAppImpl.getContext(), false);
                    NoteAppImpl.getContext().notifyDbInitComplete();
                }
                return null;
            }
        };
        NoteAppImpl.getContext().getThreadPool().submit(job);
    }

    private static File makeOriginFile(Context context) {
        File fileDirectory = StorageUtils.getAvailableFileDirectory(context,
                AttachmentSelector.IMAGW_MIN_SIZE, Constants.NOTE_MEDIA_PHOTO_PATH);
        if (fileDirectory == null) {
            fileDirectory = Constants.NOTE_MEDIA_PHOTO_PATH;
        }
        if (!fileDirectory.exists()) {
            boolean success = fileDirectory.mkdirs();
            if (!success) {
                return null;
            }
        }
        File file = NoteUtils.getSaveImageFile(fileDirectory);
        return file;
    }

    private static Uri convertToThumbnail(Context context, Bitmap rawBitmap) {
        if (rawBitmap == null) {
            return null;
        }
        Config.EditPage page = Config.EditPage.get(context);
        int width = page.mImageWidth;
        int height = page.mImageHeight;
        Bitmap bitmap = BitmapUtils.resizeAndCropCenter(rawBitmap, width, height, false, true);
        if (bitmap == null) {
            return null;
        }
        File fileDirectory = StorageUtils.getAvailableFileDirectory(context, width * height * 4,
                Constants.NOTE_MEDIA_THUMBNAIL_PATH);
        if (fileDirectory == null) {
            fileDirectory = Constants.NOTE_MEDIA_THUMBNAIL_PATH;
        }
        if (!fileDirectory.exists()) {
            boolean success = fileDirectory.mkdirs();
            if (!success) {
                return null;
            }
        }
        File thumbFile = NoteUtils.getSaveImageFile(fileDirectory);
        final Uri thumbnailUri = Uri.fromFile(thumbFile);
        NoteUtils.saveBitmap(bitmap, thumbFile);

        return thumbnailUri;
    }

    public static boolean getIsFirstLaunch(Context context) {
        return NoteShareDataManager.getIsFirstLaunch(context);
    }

    public static void setIsFirstLaunch(Context context, boolean first) {
        NoteShareDataManager.setIsFirstLaunch(context, first);
    }

    public static void initNoteData() {
        ArrayList<NoteInfo> noteInfoList = getNoteInfoList();
        if (noteInfoList == null || noteInfoList.size() == 0) {
            return;
        }
        for (NoteInfo info : noteInfoList) {
            info.coverToJsonContent();
        }
        saveToDB(noteInfoList);
    }

    private static class NoteSpan {
        public int mStart;
        public int mEnd;
        public int mType;//1:image,2:bill
        public String mFileName;
        public boolean mChecked;
    }

    private static class NoteInfo {
        public String mTitle;
        public String mContentText;
        public String mContentSpan;
        public String mLabels;
        public String mTime;
        public String mSource;
        public String mJsonContent;
        public int mEncryptEnable;

        public void coverToJsonContent() {
            String contentText = mContentText;
            if (!TextUtils.isEmpty(contentText)) {
                contentText = contentText.replace(IMAGE_SPAN, Constants.MEDIA_PHOTO);
                contentText = contentText.replace(BILL_SPAN, Constants.MEDIA_BILL);
            }
            ArrayList<NoteSpan> spanList = getSpanList(mContentSpan);
            mJsonContent = getJsonContent(spanList, contentText, mSource);
        }

        private ArrayList<NoteSpan> getSpanList(String contentSpan) {
            if (TextUtils.isEmpty(contentSpan)) {
                return null;
            }
            String[] spans = contentSpan.split(SPAN_SPLIT);
            ArrayList<NoteSpan> spanList = new ArrayList<>();
            for (int i = 0, length = spans.length; i < length; i++) {
                String[] spanContents = spans[i].split(SPAN_ITEM_SPLIT);
                String type = spanContents[0];
                NoteSpan span = new NoteSpan();
                span.mStart = Integer.parseInt(spanContents[1]);
                span.mEnd = Integer.parseInt(spanContents[2]);
                if (type.equals(TYPE_IMAGE)) {
                    span.mType = TYPE_IMAGE_INT;
                    span.mFileName = spanContents[3];
                } else if (type.equals(TYPE_BILL)) {
                    span.mType = TYPE_BILL_INT;
                    span.mChecked = Boolean.parseBoolean(spanContents[3]);
                }
                spanList.add(span);
            }
            return spanList;
        }

        private String getJsonContent(ArrayList<NoteSpan> spanList, String contentText, String source) {
            JSONStringer jsonStringer = new JSONStringer();
            try {
                jsonStringer.object();
                jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(contentText);
                if (spanList != null && spanList.size() > 0) {
                    JSONArray jsonArray = getJsonArray(spanList, source);
                    if (jsonArray != null) {
                        jsonStringer.key(DataConvert.JSON_SPANS_KEY).value(jsonArray);
                    }
                }
                jsonStringer.endObject();
            } catch (JSONException e) {
                Log.w(TAG, "error", e);
            }
            return jsonStringer.toString();
        }

        private JSONArray getJsonArray(ArrayList<NoteSpan> spanList, String source) {
            JSONArray jsonArray = new JSONArray();
            for (NoteSpan noteSpan : spanList) {
                JSONObject jsonObject = null;
                if (noteSpan.mType == TYPE_BILL_INT) {
                    jsonObject = getBillJsonObject(noteSpan);
                } else if (noteSpan.mType == TYPE_IMAGE_INT) {
                    jsonObject = getImageJsonObject(source, noteSpan);
                }
                if (jsonObject != null) {
                    jsonArray.put(jsonObject);
                }
            }
            return jsonArray;
        }

        private JSONObject getBillJsonObject(NoteSpan noteSpan) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(DataConvert.SPAN_ITEM_START, noteSpan.mStart);
                jsonObject.put(DataConvert.SPAN_ITEM_END, noteSpan.mEnd);
                jsonObject.put(DataConvert.SPAN_ITEM_FLAG, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                jsonObject.put(DataConvert.SPAN_ITEM_TYPE, BillItem.class.getName());
                jsonObject.put(BillItem.CHECKED_KEY, noteSpan.mChecked);
                return jsonObject;
            } catch (JSONException e) {
                Log.w(TAG, "error", e);
            }
            return null;
        }

        private JSONObject getImageJsonObject(String source, NoteSpan noteSpan) {
            try {
                Bitmap rawBitmap = null;
                if (source.equals(FROM_APP)) {
                    rawBitmap = DecodeUtils.decodeRawBitmap(NoteAppImpl.getContext(), noteSpan.mFileName);
                } else if (source.equals(FROM_SYSTEM)) {
                    rawBitmap = DecodeUtils.decodeSystemBitmap(noteSpan.mFileName);
                }
                Uri originUri = null;
                Uri thumbUri = null;
                if (rawBitmap != null) {
                    File file = makeOriginFile(NoteAppImpl.getContext());
                    if (file != null) {
                        originUri = Uri.fromFile(file);
                    }
                    thumbUri = convertToThumbnail(NoteAppImpl.getContext(), rawBitmap);
                    if (file != null) {
                        NoteUtils.saveBitmap(rawBitmap, file);
                        rawBitmap.recycle();
                    }else {
                        Logger.printLog(TAG, "bitmap file null");
                    }
                }
                if (originUri != null) {
                    if (thumbUri == null) {
                        thumbUri = Uri.fromFile(new File(ERROR_FILE));
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DataConvert.SPAN_ITEM_START, noteSpan.mStart);
                    jsonObject.put(DataConvert.SPAN_ITEM_END, noteSpan.mEnd);
                    jsonObject.put(DataConvert.SPAN_ITEM_FLAG, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    jsonObject.put(DataConvert.SPAN_ITEM_TYPE, PhotoImageSpan.class.getName());
                    jsonObject.put(PhotoImageSpan.ORIGIN_URI, originUri.toString());
                    jsonObject.put(PhotoImageSpan.THUMB_URI, thumbUri.toString());
                    jsonObject.put(PhotoImageSpan.PIC_WIDTH, Config.EditPage.get(NoteAppImpl.getContext()).mImageWidth);
                    jsonObject.put(PhotoImageSpan.PIC_HEIGHT, Config.EditPage.get(NoteAppImpl.getContext()).mImageHeight);
                    return jsonObject;
                }else {
                    Logger.printLog(TAG, "originUri null");
                }
            } catch (JSONException e) {
                Log.w(TAG, "error", e);
            }
            return null;
        }
    }

    private static ArrayList<NoteInfo> getNoteInfoList() {
        InputStream is = getInitConfigInputStream();
        if (is == null) {
            return null;
        }
        ArrayList<NoteInfo> noteInfoList = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, null);

            int eventType = xpp.getEventType();
            HashMap<String, String> map = new HashMap<>();
            do {
                if (eventType == XmlPullParser.START_TAG && "note".equals(xpp.getName())) {
                    int count = xpp.getAttributeCount();
                    map.clear();
                    for (int x = 0; x < count; x++) {
                        map.put(xpp.getAttributeName(x), xpp.getAttributeValue(x));
                    }
                    NoteInfo noteInfo = new NoteInfo();
                    noteInfo.mTitle = map.get("title");
                    noteInfo.mContentText = map.get("contentText");
                    noteInfo.mContentSpan = map.get("contentSpan");
                    noteInfo.mLabels = map.get("labels");
                    noteInfo.mTime = map.get("time");
                    noteInfo.mSource = map.get("source");
                    noteInfoList.add(noteInfo);
                } else if (eventType == XmlPullParser.START_TAG && "displaymode".equals(xpp.getName())) {
                    int count = xpp.getAttributeCount();
                    map.clear();
                    for (int x = 0; x < count; x++) {
                        map.put(xpp.getAttributeName(x), xpp.getAttributeValue(x));
                    }
                    String displaymodeString = map.get("displaymode");
                    try {
                        int displaymode = Integer.valueOf(displaymodeString);
                        NoteShareDataManager.setNoteDisplayMode(NoteAppImpl.getContext(), displaymode);
                    } catch (NumberFormatException e) {
                        Log.i(TAG, "NumberFormatException e" + e);
                    }
                }
                eventType = xpp.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "error", e);
        } catch (IOException e) {
            Log.w(TAG, "error", e);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return noteInfoList;
    }

    private static void saveToDB(ArrayList<NoteInfo> noteInfoList) {
        ArrayList<ContentProviderOperation> insertOps = new ArrayList<>();
        Uri uri = NoteContract.NoteContent.CONTENT_URI;
        for (NoteInfo info : noteInfoList) {
            String label = getLabel(info.mLabels);
            ContentValues values = new ContentValues();
            values.put(NoteContract.NoteContent.COLUMN_TITLE, info.mTitle);
            values.put(NoteContract.NoteContent.COLUMN_CONTENT, info.mJsonContent);
            values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, Long.valueOf(info.mTime));
            values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, Long.valueOf(info.mTime));
            values.put(NoteContract.NoteContent.CLOUMN_ITEM_SOURCE, NoteItem.DEFAULT_DATE_SOURCE);
            if (!TextUtils.isEmpty(label)) {
                values.put(NoteContract.NoteContent.COLUMN_LABEL, label);
            }
            values.put(NoteContract.NoteContent.COLUMN_REMINDER, NoteItem.INVALID_REMINDER);
            ContentProviderOperation ops = ContentProviderOperation.newInsert(uri).withValues(values).build();
            insertOps.add(ops);
        }

        try {
            NoteAppImpl.getContext().getContentResolver().applyBatch(NoteContract.AUTHORITY, insertOps);
        } catch (Exception e) {
            Logger.printLog(TAG, "insert buildidnote fail : " + e.toString());
        }
    }

    private static String getLabel(String labels) {
        if (TextUtils.isEmpty(labels)) {
            return null;
        }
        ArrayList<Integer> labelList = new ArrayList<>();
        LabelManager labelManager = NoteAppImpl.getContext().getLabelManager();
        String[] labelArray = labels.split(LABEL_SPLIT);
        for (String l : labelArray) {
            int labelId = labelManager.getLabelId(l);
            labelList.add(labelId);
        }
        return NoteItem.convertToStringLabel(labelList);
    }

    private static InputStream getInitConfigInputStream() {
        File file = new File(Constants.SYSTEM_ETC_DIR + CONFIG_FILE_NAME);//NOSONAR
        InputStream is = null;
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                is = null;
            }
        }
        if (is != null) {
            return is;
        }
        try {
            is = NoteAppImpl.getContext().getAssets().open(CONFIG_FILE_NAME);
        } catch (IOException e) {
            is = null;
        }
        return is;
    }
}
