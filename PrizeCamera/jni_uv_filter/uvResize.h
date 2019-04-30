#ifndef _UNION_VISION_RESIZE_RESIZE_H_
#define _UNION_VISION_RESIZE_RESIZE_H_

#include "uvBaseDefine.h"



#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************
* The functions is used to resize nv21 buffer
************************************************************************/
uvVoid uv_resize_nv21(
	const uvImageInfo *src_buf,  // [int]  Input  Buffer
	uvImageInfo *dst_buf         // [Out]  Output Buffer
);

#ifdef __cplusplus
}
#endif

#endif