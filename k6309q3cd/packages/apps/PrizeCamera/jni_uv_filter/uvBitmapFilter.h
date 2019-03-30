/*----------------------------------------------------------------------------------------------
*
* This file is UnionVision's property. It contains UnionVision's trade secret, proprietary and 		
* confidential information. 
* 
* The information and code contained in this file is only for authorized UnionVision employees 
* to design, create, modify, or review.
* 
* DO NOT DISTRIBUTE, DO NOT DUPLICATE OR TRANSMIT IN ANY FORM WITHOUT PROPER AUTHORIZATION.
* 
* If you are not an intended recipient of this file, you must not copy, distribute, modify, 
* or take any action in reliance on it. 
* 
* If you have received this file in error, please immediately notify UnionVision and 
* permanently delete the original and any copy of any file and any printout thereof.
*
*-------------------------------------------------------------------------------------------------*/
#ifndef __UV_CAM_FILTER_FILTER_H_
#define __UV_CAM_FILTER_FILTER_H_

#include "uvBaseDefine.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
the parameters and structs would be defined here
*/
#define UNSUPPORTED_IMAGE_TYPE  (-10)
#define UNSUPPORTED_FILTER_TYPE (-11)


#define MAX_CAM_FILTER_SUPPORTED_NUM    100

typedef enum
{
	UV_CAM_FILTER_SUMMER    = 0,  /*0-9*/
	UV_CAM_FILTER_JAPAN     = 10, /*10-19*/
	UV_CAM_FILTER_BEAUTY    = 20, /*20-29*/
	UV_CAM_FILTER_FLOWER    = 30, /*30-39*/
	UV_CAM_FILTER_FILM      = 40, /*40-49*/
	UV_CAM_FILTER_FOOD      = 50, /*50-59*/
	UV_CAM_FILTER_PANTI     = 60, /*60-69*/
	UV_CAM_FILTER_PANDORA   = 70, /*70-79*/
	UV_CAM_FILTER_SWEET     = 80, /*80-89*/
	UV_CAM_FILTER_GRAY      = 90, /*90-99*/
	UV_CAM_FILTER_NULL      = 100 ,
}EMUVCAMFILTER;



/************************************************************************
* The functions is used to perform CAM FILTER SHOT
************************************************************************/
uvInt32  uvBitmapFilterPreviewProcess(     // return UVOK if success, otherwise fail
    uvImageInfo *pImgInfo,         // [in/OUT]   INPUT/OUTPUT  IMAGE BUFFER
    EMUVCAMFILTER emFilter	       // [in]       FILTER INDEX
);

#ifdef __cplusplus
}
#endif

#endif /*__UV_CAM_FILTER_FILTER_H_*/
