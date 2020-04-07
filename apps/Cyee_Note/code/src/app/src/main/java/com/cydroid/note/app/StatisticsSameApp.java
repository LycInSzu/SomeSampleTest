package com.cydroid.note.app;

import android.app.Activity;
import android.content.res.Resources;
import com.cydroid.note.common.Log;

import com.cydroid.note.R;

/**
 * Created by wuguangjie on 16-3-30.
 */
public class StatisticsSameApp extends AbsCompetitiveAppCollector {

    @Override
    protected CompetitiveAppInfo[] createCompetitiveAppInfos() {
        Resources res = NoteAppImpl.getContext().getResources();
        String [] sameAppNames = res.getStringArray(R.array.same_app_name);
        String [] sameAppPackageNames = res.getStringArray(R.array.same_app_packagename);

        int size = sameAppNames.length;
        CompetitiveAppInfo[] competitiveAppInfos = new CompetitiveAppInfo[size];

        for (int i=0; i < size; i++){
            CompetitiveAppInfo competitiveAppInfo = CompetitiveAppInfo.create(sameAppNames[i],sameAppPackageNames[i]);
            competitiveAppInfos[i] = competitiveAppInfo;
        }
        return competitiveAppInfos;
    }

    @Override
    protected void sendEvent2Cloud(String event) {
    }

}
