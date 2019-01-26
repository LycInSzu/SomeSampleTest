package com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.TextToSpeech;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.lyc.newtestapplication.newtestapplication.BaseActivity;
import com.lyc.newtestapplication.newtestapplication.R;

import java.util.Locale;

public class TextToSpeechActivity extends BaseActivity implements TextToSpeech.OnInitListener{

    @BindView(R.id.speech_button)
    Button speechButton;
    @BindView(R.id.edittext_content)
    EditText edittextContent;

    private TextToSpeech tts;

    private boolean initSuccessFlag=false;

    @Override
    public Class getCurrentActivityName() {
        return TextToSpeechActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);
        ButterKnife.bind(this);

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
            }
        });

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
}
