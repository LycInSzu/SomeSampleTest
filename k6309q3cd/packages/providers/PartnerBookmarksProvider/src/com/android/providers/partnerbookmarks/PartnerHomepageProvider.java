// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// Package path can be changed, but should match <manifest package="..."> in AndroidManifest.xml.
package com.android.providers.partnerbookmarks;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

// Class name can be changed, but should match <provider android:name="..."> in AndroidManifest.xml.
public class PartnerHomepageProvider extends ContentProvider {
    // "http://www.android.com/" is just an example. Please replace this to actual homepage.
    // Other strings in this class must remain as it is.
    private static String HOMEPAGE_URI = "http://www.condor.dz/";
    private static final int URI_MATCH_HOMEPAGE = 0;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI("com.android.partnerbrowsercustomizations", "homepage",
                URI_MATCH_HOMEPAGE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // In fact, Chrome does not call this.
        // Just a recommaned ContentProvider practice in general.
        switch (URI_MATCHER.match(uri)) {
            case URI_MATCH_HOMEPAGE:
                return "vnd.android.cursor.item/partnerhomepage";
            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        switch (URI_MATCHER.match(uri)) {
            case URI_MATCH_HOMEPAGE:
                MatrixCursor cursor = new MatrixCursor(new String[] { "homepage" }, 1);
                cursor.addRow(new Object[] { HOMEPAGE_URI });
                return cursor;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}
