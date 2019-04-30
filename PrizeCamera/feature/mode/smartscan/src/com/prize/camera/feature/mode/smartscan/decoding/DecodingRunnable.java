package com.prize.camera.feature.mode.smartscan.decoding;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;

import java.util.Hashtable;
import java.util.Vector;

public class DecodingRunnable implements Runnable {

    private static final String TAG = "DecordingRunnable";
    private Bitmap mCurrentDecodeBitmap = null;
    private boolean canDecode = false;
    private boolean canRun = true;
    private DecodingResultListener mDecodingResultListener;
    private Vector<BarcodeFormat> decodeFormats;
    private MultiFormatReader multiFormatReader;

    public void addDecodeBitmap(Bitmap bitmap){
        Log.i(TAG,"addDecodeBitmap bitmap = " + bitmap );
        if (multiFormatReader == null){
            multiFormatReader = new MultiFormatReader();
        }
        mCurrentDecodeBitmap = bitmap;
        canDecode = true;
        canRun = true;
    }
    @Override
    public void run() {
        while (canRun){
            if (mCurrentDecodeBitmap != null&&canDecode){
                Result result = scanningImage(mCurrentDecodeBitmap);
                if (result == null){
                    if (mDecodingResultListener != null){
                        mDecodingResultListener.onDecodeFail();
                        Log.i(TAG,"decode onDecodeFail continue ");
                        mCurrentDecodeBitmap = null;
                        canDecode = false;
                        continue;
                    }
                }else {
                    if (mDecodingResultListener != null){
                        mDecodingResultListener.onDecodeSuccess(result);
                        Log.i(TAG,"decode onDecodeSuccess  ");
                        mCurrentDecodeBitmap = null;
                        canDecode = false;
                        return;
                    }
                }
            }
        }
    }

    public Result scanningImage(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        //Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        //hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码
        Hashtable<DecodeHintType, Object>  hints = new Hashtable<DecodeHintType, Object>(3);

        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        multiFormatReader.setHints(hints);
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        //QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            //return reader.decode(bitmap1, hints);
            result = multiFormatReader.decodeWithState(bitmap1);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }finally {
            multiFormatReader.reset();
            /*catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }*/
        }
        return result;
    }

    public void setDecodingResultListener(DecodingResultListener listener){
        mDecodingResultListener = listener;
    }

    public void stopScan(){
        mCurrentDecodeBitmap = null;
        canDecode = false;
        canRun = false;
    }

    public interface DecodingResultListener{
        void onDecodeFail();
        void onDecodeSuccess(Result result);
    }
}
