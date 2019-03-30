#ifndef _UV_PANO_INTERFACE_H_
#define _UV_PANO_INTERFACE_H_

#include "uvBaseDefine.h"

#define UV_PANO_OK                          0
#define UV_PANO_PREVIEW                     1
#define UV_PANO_CAPTURE                     2
// Error Code
#define UV_PANO_ERR_BASE					0X1000
#define UV_PANO_ERR_ATCH_DIR				(UV_PANO_ERR_BASE + 1)
#define UV_PANO_ERR_ATCH_LACK_FEA			(UV_PANO_ERR_BASE + 2)
#define UV_PANO_ERR_ATCH_LESS_FEA			(UV_PANO_ERR_BASE + 3)
#define UV_PANO_ERR_ATCH_TOO_QUICK			(UV_PANO_ERR_BASE + 4)
#define	UV_PANO_ERR_ATCH_TOO_SLOW			(UV_PANO_ERR_BASE + 5)
#define UV_PANO_ERR_BLEND_DIR        		(UV_PANO_ERR_BASE + 6)
#define UV_PANO_ERR_LARGE_SHAKE	        	(UV_PANO_ERR_BASE + 7)
#define UV_PANO_ERR_BLEND_SIZE              (UV_PANO_ERR_BASE + 8)
#define UV_PANO_ERR_PREVIEW_LARGE_WIDTH     (UV_PANO_ERR_BASE + 9)
#define UV_PANO_ERR_PREVIEW_LARGE_HEIGHT    (UV_PANO_ERR_BASE + 10)
#define UV_PANO_ERR_PREVIEW_FINISHED        (UV_PANO_ERR_BASE + 11)
#define UV_PANO_ERR_REVERSE_MOTION          (UV_PANO_ERR_BASE + 12)
#define UV_PANO_ERR_INPUT_PARAM				(UV_PANO_ERR_BASE + 13)

// Warning Code
#define UV_PANO_WARN_BASE					0X2000
#define	UV_PANO_WARN_MOVE_BIAS				(UV_PANO_WARN_BASE + 1)
#define	UV_PANO_WARN_MOVE_BIAS_UP			(UV_PANO_WARN_BASE + 1)
#define	UV_PANO_WARN_MOVE_BIAS_DOWN			(UV_PANO_WARN_BASE + (1<<1))
#define	UV_PANO_WARN_MOVE_BIAS_LEFT			(UV_PANO_WARN_BASE + (1<<2))
#define	UV_PANO_WARN_MOVE_BIAS_RIGHT		(UV_PANO_WARN_BASE + (1<<3))
#define	UV_PANO_WARN_LITTLE_SHAKE			(UV_PANO_WARN_BASE + (1<<4))
#define UV_PANO_WARN_BIG_SHAKE	        	(UV_PANO_WARN_BASE + (1<<5))
#define UV_PANO_WARN_ATCH_LITTLE_QUICK		(UV_PANO_WARN_BASE + (1<<7))

// Direction
#define UV_PANO_DIR_UNKNOW				-1
#define UV_PANO_DIR_LEFT2RIGHT			0
#define UV_PANO_DIR_RIGHT2LEFT			1
#define UV_PANO_DIR_TOP2BOTTOM			2
#define UV_PANO_DIR_BOTTOM2TOP			3

typedef enum __tagPanoState
{
	UV_PANO_STATE_ATTATCH = 0,        
	UV_PANO_STATE_BLEND   = 1,        
	UV_PANO_STATE_FINISH  = 2,        
}uvPanostate;

// INIT_PARAM
typedef struct __tagPanoInitParam
{
	uvUInt8	 *pu8Buffer;
	uvInt32  s32BufferLen;
	uvInt32	 s32Direction;
	uvUInt32 u32SrcSmallImageFormat;
	uvInt32  s32SrcSmallImageWidth;
	uvInt32  s32SrcSmallImageHeight;
	uvUInt32 u32SrcFullImageFormat;
	uvInt32  s32SrcFullImageWidth;
	uvInt32  s32SrcFullImageHeight;
	uvInt32	 s32FullResultLength;
	uvUInt32 u32ThumbnailFormat;
	uvInt32  s32ThumbnailResultWidthH;
	uvInt32  s32ThumbnailResultHeightH;
	uvInt32	 s32ThumbnailResultWidthV;
	uvInt32	 s32ThumbnailResultHeightV;
	uvInt32	s32SensorOrientation;
} uvPanoInitParam;

uvInt32 uvPanoInit(uvVoid **ppPanoHandle, uvPanoInitParam *pInitParam);

uvInt32 uvPanoDeInit(uvVoid **ppPanoHandle);

uvInt32 uvPanoProcess(uvVoid *pPanoHandle,
                      uvImageInfo *pSmallSrcImage,
                      uvImageInfo *pFullSrcImage,
                      uvPanostate panoState,
                      uvImageInfo *pSmallResultImage,
                      uvImageInfo *pSmallResultMask,
                      uvRect *prcUpdateSmallImage,
                      uvImageInfo *pFullResultImage,
                      uvPoint *pptOutputOffset,
                      uvInt32 *ps32Direction,
                      uvInt32 *ps32Progress);

#endif