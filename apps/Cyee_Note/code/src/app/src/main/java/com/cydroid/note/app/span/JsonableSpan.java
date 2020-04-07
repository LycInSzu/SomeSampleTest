package com.cydroid.note.app.span;

import android.content.Context;
import android.text.SpannableStringBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonableSpan {

    void updateSpanEditableText(SpannableStringBuilder stringBuilder);

    void writeToJson(JSONObject jsonObject) throws JSONException;

    void recycle();

    interface Applyer<T> {
        /**
         * Create a new instance of the Parcelable class, instantiating it
         * from the given Parcel whose data had previously been written by
         * {@link JsonableSpan#writeToJson Parcelable.writeToParcel()}.
         *
         * @param source The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        T applyFromJson(JSONObject source, SpannableStringBuilder builder, Context context,
                        boolean isEncrypt) throws JSONException;
    }
}
