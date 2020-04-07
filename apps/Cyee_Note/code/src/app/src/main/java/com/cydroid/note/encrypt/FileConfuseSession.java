package com.cydroid.note.encrypt;

import com.cydroid.note.common.Log;

import com.cydroid.note.common.NoteUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "seems no problem")
public class FileConfuseSession {
    private static final int BUFFER_SIZE = 1024 * 1024;
    private final static String TAG = "FileConfuseSession";
    private final static byte[] SECURE_IMAGE_ID = "AMI-NOTE-LOCK-FILE".getBytes(Charset.defaultCharset());
    private final ByteBuffer mCorrectHeader;
    protected static final byte[] AMI_NOTE_KEYS = "Ami_Note_Safe_Key,Developer,2016".getBytes();//NOSONAR

    private FileConfuseSession() {
        mCorrectHeader = ByteBuffer.allocate(SECURE_IMAGE_ID.length);
        mCorrectHeader.put(SECURE_IMAGE_ID);
    }

    public static FileConfuseSession open() {
        return new FileConfuseSession();
    }

    public boolean confuse(String srcPath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcPath);
            FileChannel fc = fis.getChannel();
            return confuse(srcPath, fc);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "insert to safebox error", e);
        } finally {
            NoteUtils.closeSilently(fis);
        }
        return false;
    }

    public InputStream backupConfuse(String filePath) {
        SafeFileBlock block = new SafeFileBlock(filePath);
        return block.openInputStream();
    }

    private boolean confuse(String srcPath, FileChannel in) {
        File temptImageFile = null;
        RandomAccessFile outF = null;
        try {
            temptImageFile = createNewSafeImageFile(srcPath);
            outF = new RandomAccessFile(temptImageFile, "rw");
            FileChannel out = outF.getChannel();
            writeHeader(out);
            writeEncryptContent(in, out);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "insert safebox failed", e);
            if (temptImageFile != null) {
                deleteInValidFile(temptImageFile);
            }
        } finally {
            closeRandomStream(outF);
            File src = new File(srcPath);
            if (src.exists()) {
                src.delete();
            }
            if (temptImageFile != null) {
                temptImageFile.renameTo(src);
            }
        }
        return false;
    }

    private void writeHeader(FileChannel out) throws IOException {
        synchronized (mCorrectHeader) {
            mCorrectHeader.clear();
            out.write(mCorrectHeader);
        }
    }

    private void deleteInValidFile(File safeImageFile) {
        safeImageFile.delete();
    }

    private File createNewSafeImageFile(String path) throws IOException {
        File safeFile = new File(path + ".tmp");
        if (safeFile.exists()) {
            safeFile = new File(path + System.currentTimeMillis() + ".tmp");
        }
        safeFile.createNewFile();
        return safeFile;
    }

    private void writeEncryptContent(FileChannel in, FileChannel out) throws IOException {
        byte[] amiKeys = AMI_NOTE_KEYS;
        ByteBuffer copy = ByteBuffer.allocate(BUFFER_SIZE);
        int count = in.read(copy);
        while (count != -1) {
            byte[] data = copy.array();
            MD5Util.encrypt(data, 0, count, amiKeys);
            copy.flip();
            out.write(copy);
            copy.clear();
            count = in.read(copy);
        }
    }

    private boolean verifyHeader(FileChannel fc) throws IOException {
        int limit = SECURE_IMAGE_ID.length;
        ByteBuffer buffer = ByteBuffer.allocate(limit);
        fc.read(buffer);
        buffer.clear();
        synchronized (mCorrectHeader) {
            mCorrectHeader.clear();
            return mCorrectHeader.compareTo(buffer) == 0;
        }
    }

    private void closeRandomStream(RandomAccessFile outF) {
        if (outF != null) {
            try {
                outF.close();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    private class SafeFileBlock implements FileBlock {
        private final static String TAG = "SafeFileBlock";
        private String mFilePath;

        public SafeFileBlock(String filePath) {
            mFilePath = filePath;
        }

        @Override
        public InputStream openInputStream() {
            RandomAccessFile randomStream = null;
            try {
                File cipherFile = new File(mFilePath);
                if (!cipherFile.exists()) {
                    return null;
                }
                randomStream = new RandomAccessFile(cipherFile, "rw");
                FileChannel fc = randomStream.getChannel();
                if (!verifyHeader(fc)) {
                    throw new IOException();
                }
                return new NoteDecryptInputStream(fc, randomStream);
            } catch (Exception e) {
                Log.w(TAG, "openInputStream error", e);
                closeRandomStream(randomStream);
            }
            return null;
        }

        private class NoteDecryptInputStream extends InputStream {
            private final FileChannel mChannel;
            private final ByteBuffer cachedBytes = ByteBuffer.wrap(new byte[1024 * 1024]);
            private long totalBytes = 0;
            private long position = 0;
            private long mOriginPos;
            private RandomAccessFile mRandomStream = null;

            public NoteDecryptInputStream(FileChannel channel, RandomAccessFile randomStream) {
                mChannel = channel;
                mRandomStream = randomStream;
                emptyCache();
                try {
                    mOriginPos = channel.position();
                    totalBytes = channel.size() - mOriginPos;
                } catch (Exception e) {
                    totalBytes = -1;
                }

            }

            private void emptyCache() {
                cachedBytes.limit(0);
            }

            private int decryptTail(ByteBuffer buffer, int count) {
                byte[] innerArr = buffer.array();
                int innerOffset = buffer.arrayOffset();
                int position = buffer.position();
                int from = innerOffset + position - count;
                MD5Util.decrypt(innerArr, from, count, AMI_NOTE_KEYS);
                return count;
            }

            @Override
            public int read() throws IOException {
                if (!isCacheEmpty()) {
                    return readCachedByte();
                }
                fillCache();
                if (!isCacheEmpty()) {
                    return readCachedByte();
                }
                return -1;
            }

            @Override
            public int available() throws IOException {
                return (int) (totalBytes - position);
            }

            @Override
            public void close() throws IOException {
                closeRandomStream(mRandomStream);
                mRandomStream = null;
            }

            @Override
            public void mark(int readlimit) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean markSupported() {
                return false;
            }

            @Override
            public int read(byte[] buffer) throws IOException {
                return read(buffer, 0, buffer.length);
            }

            @Override
            public synchronized void reset() throws IOException {
                mChannel.position(mOriginPos);
                emptyCache();
            }

            @Override
            public long skip(long byteCount) throws IOException {
                int readCount = 0;
                while (readCount < byteCount) {
                    if (read() == -1) {
                        break;
                    }
                    ++readCount;
                }
                return readCount;
            }

            private void fillCache() throws IOException {
                cachedBytes.clear();
                mChannel.read(cachedBytes);
                decrypt(cachedBytes);
                cachedBytes.flip();
            }

            private void decrypt(ByteBuffer buffer) {
                decryptTail(buffer, buffer.position());
            }

            private boolean isCacheEmpty() {
                return !cachedBytes.hasRemaining();
            }

            private int readCachedByte() {
                ++position;
                byte b = cachedBytes.get();
                return unsigned2Int(b);
            }

            private int unsigned2Int(byte b) {
                return ((int) b) & 0xFF;
            }
        }
    }
}