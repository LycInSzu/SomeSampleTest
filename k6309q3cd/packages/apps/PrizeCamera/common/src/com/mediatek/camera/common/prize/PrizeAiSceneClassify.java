package com.mediatek.camera.common.prize;

import java.util.HashMap;
import java.util.Map;

public class PrizeAiSceneClassify {
   public final static int AISD_SCENE_AUTO = 0;
   /**风景*/
   public final static int AISD_SCENE_LANDSCAPE = 1;
   /**人像*/
   public final static int AISD_SCENE_PORTRAIT = 2;
   /**夜景*/
   public final static int AISD_SCENE_NIGHT = 4;
   /**背光*/
   public final static int AISD_SCENE_BACKLIT = 6;
   /**日落*/
   public final static int AISD_SCENE_SUNSET = 8;
   /**海滩*/
   public final static int AISD_SCENE_BEACH = 9;
   /**美食*/
   public final static int AISD_SCENE_GOURMET = 19;
   /**蓝天*/
   public final static int AISD_SCENE_BLUESKY = 21;
   /**花朵*/
   public final static int AISD_SCENE_GREENERY = 29;
   /**都市*/
   public final static int AISD_SCENE_URBAN = 46;


   /**关闭AI*/
   public final static int PRIZE_AI_OFF = -100;
   /**开启AI*/
   public final static int PRIZE_AI_ON = 100;
   /**美食*/
   public final static int PRIZE_SCENE_GOURMET = 1;
   /**花朵*/
   public final static int PRIZE_SCENE_GREENERY = 2;
   /**人像*/
   public final static int PRIZE_SCENE_PORTRAIT = 3;
   /**风景*/
   public final static int PRIZE_SCENE_LANDSCAPE = 4;
   /**夜景*/
   public final static int PRIZE_SCENE_NIGHT = 5;
   /**背光*/
   public final static int PRIZE_SCENE_BACKLIT = 6;
   /**日落*/
   public final static int PRIZE_SCENE_SUNSET = 7;
   /**海滩*/
   public final static int PRIZE_SCENE_BEACH = 8;
   /**蓝天*/
   public final static int PRIZE_SCENE_BLUESKY = 9;
   /**都市*/
   public final static int PRIZE_SCENE_URBAN = 10;
   /**照片*/
   public final static int PRIZE_SCENE_PHOTO = 11;



   private static int[] mSceneIds = {0,1,2,4,6,8,9,19,21,29,46};
   private Map<Integer,String> mSceneMap = new HashMap<>();

   private static String[] mSceneTitles = {"无","风景","人像","夜景","背光","日落","海滩","美食","蓝天","绿植","都市"};

   public String getTitle(int id){
      String Title = mSceneMap.get(id);
      if (Title == null){
         return mSceneMap.get(0);
      }
      return Title;
   }

   public void init(){
      for (int i = 0; i < mSceneIds.length; i++) {
         mSceneMap.put(mSceneIds[i], mSceneTitles[i]);
      }
   }

}
