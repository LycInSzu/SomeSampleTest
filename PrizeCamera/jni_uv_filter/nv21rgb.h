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
#ifndef __UV_NV_21_RGB_H_
#define __UV_NV_21_RGB_H_

#include "uvBaseDefine.h"

#ifdef __cplusplus
extern "C" {
#endif

uvInt32 trans_NV21_argb8888(
			const uvImageInfo *pImgInfo,  // [int]  Input  Buffer
			unsigned char* argb8888,      // [out]  Output  Buffer，malloc by user
			uvInt32 argbStride            // Output Buffer stride
);

#ifdef __cplusplus
}
#endif

#endif
