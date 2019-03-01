/*----------------------------------------------------

Copyright (c) FotoNation
All rights reserved

----------------------------------------------------*/

#ifndef __PRIZE_WATERMARK_SFB_H__
#define __PRIZE_WATERMARK_SFB_H__

#include <utils/RefBase.h>
//#include <Log.h>	
//#include <mtkcam/common.h>

//#define TRUE        1
//#define FALSE       0

#define ALIGN(a,n)  ( ((size_t)(a) + ((n)-1)) & ~((n)-1) )
#define ALIGN_SZ 128

#include "data_pix/koobee_f1_7024_9360_h.h"
#include "data_pix/koobee_f1_3120_4160_h.h"
#include "data_pix/koobee_f1_2448_3264_h.h"
#include "data_pix/koobee_f1_2304_4096_h.h"
#include "data_pix/koobee_f1_2592_4608_h.h"
#include "data_pix/koobee_f1_1920_2560_h.h"
#include "data_pix/koobee_f1_1872_3328_h.h"
#include "data_pix/koobee_f1_1440_2560_h.h"
#include "data_pix/koobee_f1_3456_4608_h.h"
#include "data_pix/koobee_f1_3072_4096_h.h"
#include "data_pix/koobee_f1_3024_5376_h.h"
#include "data_pix/koobee_f1_720_1280_h.h"
#include "data_pix/koobee_f1_1088_1920_h.h"
#include "data_pix/koobee_f1_1200_1600_h.h"
#include "data_pix/koobee_f1_4896_6528_h.h"
#include "data_pix/koobee_f1_1728_3072_h.h"
#include "data_pix/koobee_f1_6240_8320_h.h"
#include "data_pix/koobee_f1_1968_4160_h.h"
#include "data_pix/koobee_f1_2160_3840_h.h"
#include "data_pix/koobee_f1_6912_9216_h.h"
//namespace android 



/*class PrizeWaterMark
{
public:		////    Constructor/Destructor.
    				PrizeWaterMark();
    virtual			~PrizeWaterMark();

public:     ////    Operations.
    

};
}*/
static void	 add_watermark(int w,int h,int format,unsigned char *frame_buffer, unsigned char transform){
        
		int margin_bottom = 0;
		int margin_left = 0;
		int dg_width;
		int dg_height;
		//for yuyv,y stride=2;for nv12,y stride=1;
		int y_stride=0;
		if (format == 0x14 || format == 8201)
		{
		  y_stride=2;
		}
		else if(format == 0x11)
		 {
		 y_stride=1;
		 }
		CAM_LOGD("add watermark In format=%d stride=%d",format,y_stride);

		//char value[PROPERTY_VALUE_MAX];
		
	   //property_get("ro.config.mct.watermark.enabled", value, "0");
        int32_t enable = 1;//atoi(value);
				
//		ALOGD("**** get product.name is: value is:%s",value);
		

		if(enable){
				

		int mPictureWidth = w;
		int mPictureHeight = h;
		

		unsigned char *_ptr = (unsigned char *)frame_buffer;   
		CAM_LOGD("add watermark width=%d height=%d addr=0x%x %p, transform = %d",mPictureWidth,mPictureHeight,_ptr,_ptr,transform);

		const unsigned char* str = NULL;	
		
	    if((2560 == mPictureWidth && 1440 == mPictureHeight) || (1440 == mPictureWidth && 2560 == mPictureHeight)){
			dg_width = digital_width_1440_2560_H;
			dg_height = digital_height_1440_2560_H; 				
			margin_bottom= mPictureHeight - dg_height - digital_height_1440_2560_bottom_margin;  
			margin_left = digital_width_1440_2560_left_margin;
			str = &koobee_f1_1440_2560_h[0];			
		}
		else if((720 == mPictureWidth && 1280 == mPictureHeight) || (1280 == mPictureWidth && 720 == mPictureHeight)){
			dg_width = digital_width_720_1280_H;
			dg_height = digital_height_720_1280_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_720_1280_bottom_margin;  
			margin_left = digital_width_720_1280_left_margin;
			str = &koobee_f1_720_1280_h[0];
		}
		else if((1088 == mPictureWidth && 1920 == mPictureHeight) || (1920 == mPictureWidth && 1088 == mPictureHeight)){
			dg_width = digital_width_1088_1920_H;
			dg_height = digital_height_1088_1920_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_1088_1920_bottom_margin;  
			margin_left = digital_width_1088_1920_left_margin;
			str = &koobee_f1_1088_1920_h[0];
		}
		else if((1200 == mPictureWidth && 1600 == mPictureHeight) || (1600 == mPictureWidth && 1200 == mPictureHeight)){
			dg_width = digital_width_1200_1600_H;
			dg_height = digital_height_1200_1600_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_1200_1600_bottom_margin;  
			margin_left = digital_width_1200_1600_left_margin;
			str = &koobee_f1_1200_1600_h[0];
		}
		else if((4896 == mPictureWidth && 6528 == mPictureHeight) || (6528 == mPictureWidth && 4896 == mPictureHeight)){
			dg_width = digital_width_4896_6528_H;
			dg_height = digital_height_4896_6528_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_4896_6528_bottom_margin;  
			margin_left = digital_width_4896_6528_left_margin;
			str = &koobee_f1_4896_6528_h[0];
		}
		else if((3328 == mPictureWidth && 1872 == mPictureHeight) || (1872 == mPictureWidth && 3328 == mPictureHeight) ){
			dg_width = digital_width_1872_3328_H;
			dg_height = digital_height_1872_3328_H; 				
			margin_bottom= mPictureHeight - dg_height - digital_height_1872_3328_bottom_margin;  
			margin_left = digital_width_1872_3328_left_margin;
			str = &koobee_f1_1872_3328_h[0];

		}
        else if((2560 == mPictureWidth && 1920 == mPictureHeight) || (1920 == mPictureWidth && 2560 == mPictureHeight) ){
			dg_width = digital_width_1920_2560_H;
			dg_height = digital_height_1920_2560_H; 				
			margin_bottom= mPictureHeight - dg_height - digital_height_1920_2560_bottom_margin;  
			margin_left = digital_width_1920_2560_left_margin;
			str = &koobee_f1_1920_2560_h[0];

		}
		 else if((4096 == mPictureWidth && 2304 == mPictureHeight) || (2304 == mPictureWidth && 4096 == mPictureHeight) ){
			dg_width = digital_width_2304_4096_H;
			dg_height = digital_height_2304_4096_H; 				
			margin_bottom= mPictureHeight - dg_height - digital_height_2304_4096_bottom_margin;  
			margin_left = digital_width_2304_4096_left_margin;
			str = &koobee_f1_2304_4096_h[0];

		}
		 else if((4608 == mPictureWidth && 2592 == mPictureHeight) || (2592 == mPictureWidth && 4608 == mPictureHeight) ){
			dg_width = digital_width_2592_4608_H;
			dg_height = digital_height_2592_4608_H; 				
			margin_bottom= mPictureHeight - dg_height - digital_height_2592_4608_bottom_margin;  
			margin_left = digital_width_2592_4608_left_margin;
			str = &koobee_f1_2592_4608_h[0];

		}		
		else if((2448 == mPictureWidth && 3264 == mPictureHeight) || (3264 == mPictureWidth && 2448 == mPictureHeight)){
			dg_width = digital_width_2448_3264_H;
			dg_height = digital_height_2448_3264_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_2448_3264_bottom_margin;  
			margin_left = digital_width_2448_3264_left_margin;
			str = &koobee_f1_2448_3264_h[0];
		}		
		else if((3120 == mPictureWidth && 4160 == mPictureHeight) || (4160 == mPictureWidth && 3120 == mPictureHeight)){
			dg_width = digital_width_3120_4160_H;
			dg_height = digital_height_3120_4160_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_3120_4160_bottom_margin;  
			margin_left = digital_width_3120_4160_left_margin;
			str = &koobee_f1_3120_4160_h[0];

		}
		else if((7020 == mPictureWidth && 9360 == mPictureHeight) || (9360 == mPictureWidth && 7020 == mPictureHeight)){
			dg_width = digital_width_7024_9360_H;
			dg_height = digital_height_7024_9360_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_7024_9360_bottom_margin;  
			margin_left = digital_width_7024_9360_left_margin;
			str = &koobee_f1_7024_9360_h[0];
		}
		else if((3456 == mPictureWidth && 4608 == mPictureHeight) || (4608 == mPictureWidth && 3456 == mPictureHeight)){
			dg_width = digital_width_3456_4608_H;
			dg_height = digital_height_3456_4608_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_3456_4608_bottom_margin;  
			margin_left = digital_width_3456_4608_left_margin;
			str = &koobee_f1_3456_4608_h[0];
		}
		else if((4096 == mPictureWidth && 3072 == mPictureHeight) || (3072 == mPictureWidth && 4096 == mPictureHeight)){
			dg_width = digital_width_3072_4096_H;
			dg_height = digital_height_3072_4096_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_3072_4096_bottom_margin;  
			margin_left = digital_width_3072_4096_left_margin;
			str = &koobee_f1_3072_4096_h[0];
		}
		else if((5376 == mPictureWidth && 3024 == mPictureHeight) || (3024 == mPictureWidth && 5376 == mPictureHeight)){
			dg_width = digital_width_3024_5376_H;
			dg_height = digital_height_3024_5376_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_3024_5376_bottom_margin;  
			margin_left = digital_width_3024_5376_left_margin;
			str = &koobee_f1_3024_5376_h[0];
		}
		else if((1728 == mPictureWidth && 3072 == mPictureHeight) || (3072 == mPictureWidth && 1728 == mPictureHeight)){
			dg_width = digital_width_1728_3072_H;
			dg_height = digital_height_1728_3072_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_1728_3072_bottom_margin;  
			margin_left = digital_width_1728_3072_left_margin;
			str = &koobee_f1_1728_3072_h[0];
		}
		else if((6240 == mPictureWidth && 8320 == mPictureHeight) || (8320 == mPictureWidth && 6240 == mPictureHeight)){
			dg_width = digital_width_6240_8320_H;
			dg_height = digital_height_6240_8320_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_6240_8320_bottom_margin;  
			margin_left = digital_width_6240_8320_left_margin;
			str = &koobee_f1_6240_8320_h[0];
		}
		else if((1968 == mPictureWidth && 4160 == mPictureHeight) || (4160 == mPictureWidth && 1968 == mPictureHeight)){
			dg_width = digital_width_1968_4160_H;
			dg_height = digital_height_1968_4160_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_1968_4160_bottom_margin;  
			margin_left = digital_width_1968_4160_left_margin;
			str = &koobee_f1_1968_4160_h[0];
		}
		else if((2160 == mPictureWidth && 3840 == mPictureHeight) || (3840 == mPictureWidth && 2160 == mPictureHeight)){
			dg_width = digital_width_2160_3840_H;
			dg_height = digital_height_2160_3840_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_2160_3840_bottom_margin;  
			margin_left = digital_width_2160_3840_left_margin;
			str = &koobee_f1_2160_3840_h[0];
		}
		else if((6912 == mPictureWidth && 9216 == mPictureHeight) || (9216 == mPictureWidth && 6912 == mPictureHeight)){
			dg_width = digital_width_6912_9216_H;
			dg_height = digital_height_6912_9216_H;					
			margin_bottom= mPictureHeight - dg_height - digital_height_6912_9216_bottom_margin;  
			margin_left = digital_width_6912_9216_left_margin;
			str = &koobee_f1_6912_9216_h[0];
		}
		else
		{
			
			CAM_LOGD("add watermark NO SUITABLE RESOLUTION!!");
			return ;
		 }

		if ((NULL != _ptr) && (NULL != str)) {
			//unsigned char* p;
			//p = (unsigned char*)malloc(size);
			//for(size_t i=0;i < size ;i++){
			//	*(p+i)=*(_ptr+i);
			//}
			
			for(int j = 0; j<dg_height; j++){  					
		
						for(int h=0; h<dg_width; h++){ 
							//CAM_LOGD("width count=%d %d 0x%x",h,dg_width,*(str+(j)*dg_width + h));
							if(*(str+(j)*dg_width + h) != 0X39) {  //(dg_height - 1 - j)
							//CAM_LOGD("..=%d",h);
								*(_ptr+(margin_bottom*(mPictureWidth)+ margin_left+ j*(mPictureWidth) +  h)*y_stride) =0xff;//*(str+(j)*dg_width + h);//0xff;// *(str+(dg_height - 1 - j)*dg_width + h);  
												
					}  
				}  
			}
			//memcpy(_ptr, p, size);
			//free(p);
		}  
		}
		CAM_LOGD("add watermark Out");

}		

#endif
