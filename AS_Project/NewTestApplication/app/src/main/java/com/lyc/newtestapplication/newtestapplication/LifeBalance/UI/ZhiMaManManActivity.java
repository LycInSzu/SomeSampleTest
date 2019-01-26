package com.lyc.newtestapplication.newtestapplication.LifeBalance.UI;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.lyc.newtestapplication.newtestapplication.*;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Adapter.FunctionItemsListAdapter;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.MyRecycleviewItemClickListener;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.Countdown.CountdownActivity;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.TextToSpeech.TextToSpeechActivity;


import java.util.ArrayList;
import java.util.Collections;

public class ZhiMaManManActivity extends BaseActivity {

    @BindView(R.id.functionitemsrecycleview)
    RecyclerView functionitemsrecycleview;

    private RecyclerView.LayoutManager layoutManager;
    private FunctionItemsListAdapter adapter;
    private ArrayList<String> functionlist=new ArrayList<>();

    private final int toPayActivity=0;
    private final int toIncomeActivity=1;
    private final int toCountdownActivity=2;
    private final int textToSpeech=3;

    @Override
    public Class getCurrentActivityName() {
        return ZhiMaManManActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhi_ma_man_man);
        ButterKnife.bind(this);


        Resources res=getResources();
        Collections.addAll(functionlist,res.getStringArray(R.array.functions));

        Log.i(TAG,"length of functionitemlist is "+functionlist.size());
        layoutManager=new GridLayoutManager(ZhiMaManManActivity.this,2,RecyclerView.VERTICAL,false);
        functionitemsrecycleview.setLayoutManager(layoutManager);
        adapter=new FunctionItemsListAdapter(functionlist);

        functionitemsrecycleview.setAdapter(adapter);
        adapter.setItemClickListener(new MyRecycleviewItemClickListener() {
            @Override
            public void onItemClick(View child, int position) {
                Log.i(TAG," you clicked position "+ position);
                Toast.makeText(ZhiMaManManActivity.this, position+"", Toast.LENGTH_SHORT).show();
                startToNewActivity(position);

//                startDetermindActivity();
            }
        });

    }

    private void startToNewActivity(int position) {
        switch (position){
            case    toPayActivity:

                break;
            case    toIncomeActivity:
                break;
            case    toCountdownActivity:
                startDetermindActivity(CountdownActivity.class,null);
                break;
            case textToSpeech:
                startDetermindActivity(TextToSpeechActivity.class,null);
                break;
            default:
                break;
        }
    }


}
