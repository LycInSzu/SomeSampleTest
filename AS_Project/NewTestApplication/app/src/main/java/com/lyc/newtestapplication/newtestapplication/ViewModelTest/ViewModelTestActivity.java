package com.lyc.newtestapplication.newtestapplication.ViewModelTest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.lyc.newtestapplication.newtestapplication.R;
import com.lyc.newtestapplication.newtestapplication.ViewModelTest.ui.viewmodeltest.ViewModelTestFragment;

public class ViewModelTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_model_test_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ViewModelTestFragment.newInstance())
                    .commitNow();
        }
    }
}
