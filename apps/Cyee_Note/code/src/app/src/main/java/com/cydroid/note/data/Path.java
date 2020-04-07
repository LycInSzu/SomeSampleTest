/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cydroid.note.data;

import com.cydroid.note.common.IdentityCache;
import com.cydroid.note.common.NoteUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Path {
    private static final String TAG = "Path";
    private static Path sRoot = new Path(null, "ROOT");

    private final Path mParent;
    private final String mSegment;
    private WeakReference<NoteObject> mObject;
    private IdentityCache<String, Path> mChildren;

    private Path(Path parent, String segment) {
        mParent = parent;
        mSegment = segment;
    }

    public static Path fromString(String s) {
        synchronized (Path.class) {
            String[] segments = split(s);
            Path current = sRoot;
            for (int i = 0; i < segments.length; i++) {
                current = current.getChild(segments[i]);
            }
            return current;
        }
    }

    public static String[] split(String s) {
        int n = s.length();
        if (n == 0) return new String[0];
        if (s.charAt(0) != '/') {
            throw new RuntimeException("malformed path:" + s);
        }
        ArrayList<String> segments = new ArrayList<String>();
        int i = 1;
        while (i < n) {
            int brace = 0;
            int j;
            for (j = i; j < n; j++) {
                char c = s.charAt(j);
                if (c == '{') ++brace;
                else if (c == '}') --brace;
                else if (brace == 0 && c == '/') break;
            }
            if (brace != 0) {
                throw new RuntimeException("unbalanced brace in path:" + s);
            }
            segments.add(s.substring(i, j));
            i = j + 1;
        }
        String[] result = new String[segments.size()];
        segments.toArray(result);
        return result;
    }

    // Splits a string to an array of strings.
    // For example, "{foo,bar,baz}" -> {"foo","bar","baz"}.
    public static String[] splitSequence(String s) {
        int n = s.length();
        if (s.charAt(0) != '{' || s.charAt(n - 1) != '}') {
            throw new RuntimeException("bad sequence: " + s);
        }
        ArrayList<String> segments = new ArrayList<String>();
        int i = 1;
        while (i < n - 1) {
            int brace = 0;
            int j;
            for (j = i; j < n - 1; j++) {
                char c = s.charAt(j);
                if (c == '{') ++brace;
                else if (c == '}') --brace;
                else if (brace == 0 && c == ',') break;
            }
            if (brace != 0) {
                throw new RuntimeException("unbalanced brace in path:" + s);
            }
            segments.add(s.substring(i, j));
            i = j + 1;
        }
        String[] result = new String[segments.size()];
        segments.toArray(result);
        return result;
    }

    static void dumpAll(Path p, String prefix1, String prefix2) {
        synchronized (Path.class) {
            if (p.mChildren != null) {
                ArrayList<String> childrenKeys = p.mChildren.keys();
                int i = 0, n = childrenKeys.size();
                for (String key : childrenKeys) {
                    Path child = p.mChildren.get(key);
                    if (child == null) {
                        ++i;
                        continue;
                    }

                    if (++i < n) {
                        dumpAll(child, prefix2 + "+-- ", prefix2 + "|   ");
                    } else {
                        dumpAll(child, prefix2 + "+-- ", prefix2 + "    ");
                    }
                }
            }
        }
    }

    public Path getChild(String segment) {
        synchronized (Path.class) {
            if (mChildren == null) {
                mChildren = new IdentityCache<String, Path>();
            } else {
                Path p = mChildren.get(segment);
                if (p != null) return p;
            }

            Path p = new Path(this, segment);
            mChildren.put(segment, p);
            return p;
        }
    }

    public Path getChild(int segment) {
        return getChild(String.valueOf(segment));
    }

    public Path getChild(long segment) {
        return getChild(String.valueOf(segment));
    }

    NoteObject getObject() {
        synchronized (Path.class) {
            return (mObject == null) ? null : mObject.get();
        }
    }

    public void setObject(NoteObject object) {
        synchronized (Path.class) {
            NoteUtils.assertTrue(mObject == null || mObject.get() == null);
            mObject = new WeakReference<NoteObject>(object);
        }
    }

    @Override
    public String toString() {
        synchronized (Path.class) {
            StringBuilder sb = new StringBuilder();
            String[] segments = split();
            for (int i = 0; i < segments.length; i++) {
                sb.append("/");
                sb.append(segments[i]);
            }
            return sb.toString();
        }
    }

    public String[] split() {
        synchronized (Path.class) {
            int n = 0;
            for (Path p = this; p != sRoot; p = p.mParent) {
                n++;
            }
            String[] segments = new String[n];
            int i = n - 1;
            for (Path p = this; p != sRoot; p = p.mParent) {
                segments[i--] = p.mSegment;
            }
            return segments;
        }
    }

    public String getPrefix() {
        if (this == sRoot) return "";
        return getPrefixPath().mSegment;
    }

    public Path getPrefixPath() {
        synchronized (Path.class) {
            Path current = this;
            if (current == sRoot) {
                throw new IllegalStateException();
            }
            while (current.mParent != sRoot) {
                current = current.mParent;
            }
            return current;
        }
    }

}