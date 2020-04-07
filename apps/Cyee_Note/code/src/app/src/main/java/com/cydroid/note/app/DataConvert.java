package com.cydroid.note.app;

import android.app.Activity;
import android.text.Editable;
import android.text.SpannableStringBuilder;

import com.gionee.framework.log.Logger;
import com.cydroid.note.app.span.BillItem;
import com.cydroid.note.app.span.JsonableSpan;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.app.view.NoteContentEditText;
import com.cydroid.note.common.BadJsonableException;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.NoteParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.lang.reflect.Field;

/**
 * {
 * "text"  : "this is a note",
 * "spans" : [
 * {
 * "type" : "ImageSpan",
 * "start" : "6",
 * "end"   : "8"
 * "filepath" : "storage/sdcard0/image/note.jpg"
 * }
 * ]
 * }
 */
public class DataConvert {
    public static final String TAG = "DataConvert";
    public static final String JSON_CONTENT_KEY = "text";
    public static final String JSON_SPANS_KEY = "spans";
    //span common key
    public static final String SPAN_ITEM_TYPE = "class";
    public static final String SPAN_ITEM_START = "start";
    public static final String SPAN_ITEM_END = "end";
    public static final String SPAN_ITEM_FLAG = "flag";

    public static void applySpanToEditableFromJson(Activity activity, String string, NoteContentEditText editText,
                                                   boolean isEncrypt) {
        SpannableStringBuilder builder = (SpannableStringBuilder) editText.getText();
        try {
            JSONTokener jsonParser = new JSONTokener(string);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            String text = jsonObject.getString(JSON_CONTENT_KEY);
            builder.append(text);

            JSONArray spans = jsonObject.optJSONArray(JSON_SPANS_KEY);
            if (spans == null) {
                return;
            }
            int length = spans.length();
            for (int i = 0; i < length; i++) {
                JSONObject json = spans.getJSONObject(i);
                JsonableSpan.Applyer applyer = getJsonableApplyer(json.getString(SPAN_ITEM_TYPE));
                Object span = applyer.applyFromJson(json, builder, activity, isEncrypt);
                if (span instanceof PhotoImageSpan) {
                    ((PhotoImageSpan) span).setOnImageSpanChangeListener(editText);
                } else if (span instanceof BillItem) {
                    ((BillItem) span).setOnImageSpanChangeListener(editText);
                }
            }
        } catch (JSONException e) {
            Logger.printLog(TAG, "applySpanToEditableFromJson fail : " + e + " ,,, editText ,,," + editText);
        }
    }

    public static String editableConvertToJson(Editable editable) {
        try {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object();

            String text = editable.toString();
            jsonStringer.key(JSON_CONTENT_KEY).value(text);

            JSONArray spans = convertSpansToJson(editable);
            if (!(spans == null || spans.length() == 0)) {
                jsonStringer.key(JSON_SPANS_KEY).value(spans);
            }

            jsonStringer.endObject();

            return jsonStringer.toString();
        } catch (Exception e) {
            Logger.printLog(TAG, "editableConvertToJson fail : " + e + " ,,,editable,,," + editable.toString());
        }
        return null;
    }

    private static JsonableSpan.Applyer getJsonableApplyer(String clzName) throws JSONException {
        JsonableSpan.Applyer applyer;
        try {
            Class c = Class.forName(clzName);
            Field f = c.getField("APPLYER");
            applyer = (JsonableSpan.Applyer) f.get(null);
        } catch (IllegalAccessException e) {
            Logger.printLog(TAG, "Illegal access when unmarshalling: "
                    + clzName + " :" + e);
            throw new BadJsonableException(
                    "IllegalAccessException when unmarshalling: " + clzName);
        } catch (ClassNotFoundException e) {
            Logger.printLog(TAG, "Class not found when unmarshalling: "
                    + clzName + e);
            throw new BadJsonableException(
                    "ClassNotFoundException when unmarshalling: " + clzName);
        } catch (ClassCastException e) {
            throw new BadJsonableException("JsonableSpan protocol requires a "
                    + "JsonableSpan.Creator object called "
                    + " CREATOR on class " + clzName);
        } catch (NoSuchFieldException e) {
            throw new BadJsonableException("JsonableSpan protocol requires a "
                    + "JsonableSpan.Creator object called "
                    + " CREATOR on class " + clzName);
        } catch (NullPointerException e) {
            throw new BadJsonableException("JsonableSpan protocol requires "
                    + "the CREATOR object to be static on class " + clzName);
        }
        if (applyer == null) {
            throw new BadJsonableException("JsonableSpan protocol requires a "
                    + "JsonableSpan.Creator object called "
                    + " CREATOR on class " + clzName);
        }
        return applyer;
    }

    private static JSONArray convertSpansToJson(Editable editable) {
        JsonableSpan[] jsonableSpans = editable.getSpans(0, editable.length(), JsonableSpan.class);
        JSONArray jsonArray = new JSONArray();
        for (JsonableSpan jsonableSpan : jsonableSpans) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonableSpan.writeToJson(jsonObject);
                //make sure jsonObject has SPAN_ITEM_TYPE
                NoteUtils.assertTrue(jsonObject.getString(SPAN_ITEM_TYPE) != null, "jsonable should be put in SPAN_ITEM_TYPE field. ");
            } catch (JSONException e) {
                Logger.printLog(TAG, "convert to json error:" + e + " ,,,,editable = " + editable.toString());
            }

            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static String getContent(String json) {
        JSONTokener jsonParser = new JSONTokener(json);
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.nextValue();
            String text = jsonObject.getString(JSON_CONTENT_KEY);
            text = NoteParser.replaceMediaString(text);
            return text;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
