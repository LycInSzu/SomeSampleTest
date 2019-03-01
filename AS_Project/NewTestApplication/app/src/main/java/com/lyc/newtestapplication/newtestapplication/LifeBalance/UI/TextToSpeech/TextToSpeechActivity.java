package com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.TextToSpeech;

import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.lyc.newtestapplication.newtestapplication.AboutSurfaceView.SurfaceViewTestActivity;
import com.lyc.newtestapplication.newtestapplication.BaseActivity;
import com.lyc.newtestapplication.newtestapplication.R;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class TextToSpeechActivity extends BaseActivity implements TextToSpeech.OnInitListener{

    @BindView(R.id.speech_button)
    Button speechButton;
    @BindView(R.id.edittext_content)
    EditText edittextContent;

    private TextToSpeech tts;

    private boolean initSuccessFlag=false;

    private static final String THEME_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Condor Theme/Theme/";
    //private static final String DEFAULT_THEME_PATH = "/system//media//kb_resources/DefaultTheme/";
    private Bitmap mMaskBitmap=null;
    private Bitmap mShadowBitmap=null;

    private String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE"};

    @Override
    public Class getCurrentActivityName() {
        return TextToSpeechActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);
        ButterKnife.bind(this);

//        private IntentFilter filter = new IntentFilter();
//        filter.addAction();
//        filter.addCategory();

        tts = new TextToSpeech(this, this);

        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!initSuccessFlag)
                    return;
                if (!TextUtils.isEmpty(edittextContent.getText())){
                    Log.d(TAG,"---------------------------go to speech--------------------");
                    tts.speak(edittextContent.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }


//                if (checkDangerousPermissions(TextToSpeechActivity.this, permissions)) {
//                    initOtherIconDrawable();
//                } else {
//
//                    requestNeedPermissions(TextToSpeechActivity.this, permissions, 348);
//                }


                Intent intent = new Intent();
                intent.setAction("com.cyee.intent.action.theme.change");
                intent.addCategory("com.cyee.intent.category.theme.V2");
                TextToSpeechActivity.this.sendBroadcast(intent);


                PackageManager pm = getPackageManager();
                List<ResolveInfo> matches = pm.queryBroadcastReceivers(intent, 0);

                for (ResolveInfo resolveInfo : matches) {
                    Intent explicit = new Intent(intent);
                    ComponentName cn = new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name);
                    Log.d(TAG,"the receiver componentname is "+cn);

                }




            }
        });

//        TextView indexTextview=new TextView(this);
//        indexTextview.setText();
//        indexTextview.setTextSize(15);



    }
    //for icons not in the theme
    private void initOtherIconDrawable() {
        Log.d(TAG, "------ initOtherIconDrawable  ---------------");
        mMaskBitmap = getBitmapNoResize("mask.jpg");
        mShadowBitmap = getBitmapNoResize("shadow.jpg");
        if (mMaskBitmap == null) {
            Log.d(TAG, "------ mMaskBitmap == null  ---------------");
            mMaskBitmap = mShadowBitmap;
        }
    }

    private Bitmap getBitmapNoResize(String file) {
        Log.d(TAG, "------ getBitmapNoResize  ---------------  file  is  "+file);
//        Bitmap bitmap = getThemeIconBitmap(THEME_PATH, file);
        Bitmap bitmap = getBitmapFromComponentDrawable(file);
        if (bitmap == null) {
//            bitmap = getBitmapNoResize(DEFAULT_THEME_PATH, file);
            Log.d(TAG, "------ getBitmapNoResize  --------------- the file  is  "+file+ " is  null");
        }
        Log.d(TAG, "------ getBitmapNoResize  ---------------  end  ");
        return bitmap;
    }

    private Bitmap getThemeIconBitmap(String path, String file) {
        Log.d(TAG, "------ getThemeIconBitmap  ---------------  ");
        String pathName = THEME_PATH + file;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public Bitmap getBitmapFromComponentDrawable(String file) {
        Log.d(TAG, "------ getBitmapFromComponentDrawable" );
        String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Condor Theme/" + "Theme/" + file;
//        String imagePathPreinstall = "/system//media//kb_resources//DefaultTheme/" + this.getImageName(componentName) + ".jpg";
        File imageFile = new File(imagePath);
//        File imageFilePreinstall = new File(imagePathPreinstall);

        Log.d(TAG, "------ getBitmapFromComponentDrawable  --------------- imagePath  is  "+imagePath);
        Bitmap myBitmap = null;
        if (imageFile.exists()) {
            Log.d(TAG, "------ getBitmapFromComponentDrawable   ---------------  got the bitmap");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            myBitmap = BitmapFactory.decodeFile(imagePath, options);
            if (myBitmap == null){
                Log.d(TAG, "------ getBitmapFromComponentDrawable   ---------------  myBitmap is null ");
            }
            Log.d(TAG, "------ getBitmapFromComponentDrawable   -------------  end");
            return myBitmap;
        } /*else if(imageFilePreinstall.exists()) {
            myBitmap = BitmapFactory.decodeFile(imageFilePreinstall.getAbsolutePath());
        }*/
        Log.d(TAG, "------ getBitmapFromComponentDrawable   -------------  the bitmap  is   null------");
        Log.d(TAG, "------ getBitmapFromComponentDrawable   -------------  end");
        return null;
    }

    @Override
    public void onInit(int status) {
        // 判断是否转化成功
        if (status == TextToSpeech.SUCCESS){
            Log.d(TAG,"---------------------------init success--------------------");
            initSuccessFlag=true;
            //默认设定语言为中文，原生的android貌似不支持中文。
//            int result = tts.setLanguage(Locale.CHINESE);
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
//                Log.d(TAG,"---------------------------do not support--------------------");
//                Toast.makeText(TextToSpeechActivity.this, "初始化中文失败，使用英文。。。", Toast.LENGTH_SHORT).show();
//                //不支持中文就将语言设置为英文
//                tts.setLanguage(Locale.US);
//            }
            tts.setLanguage(Locale.US);
        }
    }










//    class ThemeChangeReceiver extends BroadcastReceiver {
//        private static final String TAG = "ThemeChangeReceiver";
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            Log.d(TAG, "----------ThemeChangeReceiver   onReceive=" + action + ", " + intent.getCategories());
//
//        }
//
//    }
}
