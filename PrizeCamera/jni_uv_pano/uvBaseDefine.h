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
#ifndef _UV_BASE_DEFINE_H_
#define _UV_BASE_DEFINE_H_

#ifndef UVFALSE
#define  UVFALSE   0
#endif
#ifndef UVTRUE
#define UVTRUE    1
#endif

#ifndef UVOK
#define UVOK      0
#endif

typedef char                    uvBool;
typedef char                    uvChar;
typedef void                    uvVoid;
typedef	signed      char        uvInt8;
typedef	unsigned    char        uvUInt8;
typedef	signed      short       uvInt16;
typedef	unsigned    short       uvUInt16;
typedef signed      int         uvInt32;
typedef unsigned    int         uvUInt32;
typedef long long               uvInt64;
typedef unsigned long long      uvUInt64;
typedef unsigned    int         uvHANDLE;
typedef float                   uvFloat32;
typedef double                  uvFloat64;

#define UVERR_NULL_POINT (-2)
#define UVERR_ERROR_PARS (-3)

#define UV_NULL  (0)

/*RECT*/
typedef struct __tagRect
{
	uvInt32 s32Left;
	uvInt32 s32Top;
	uvInt32 s32Right;
	uvInt32	s32Bottom;
} uvRect;

/*SIZE*/
typedef struct __tagSize
{
	uvInt32 s32Width;
	uvInt32 s32Height;
} uvSize;


/*PONIT---INT32*/
typedef struct __tagPoint
{
	uvInt32 s32X;
	uvInt32 s32Y;
} uvPoint;

/*SUPPORTED IMAGE TYPE*/
typedef enum __tagImageType
{
	/*8 bit Y plane only*/
	UV_IMAGE_TYPE_GREY = 0,
	/*8 bit Y plane followed by 8 bit 2x2 subsampled U and V planes*/
	UV_IMAGE_TYPE_I420,
	/*8 bit Y plane followed by 8 bit 1x2 subsampled U and V planes*/
	UV_IMAGE_TYPE_I422V,
	/*8 bit Y plane followed by 8 bit 2x1 subsampled U and V planes*/
	UV_IMAGE_TYPE_I422H,
	/*8 bit Y plane followed by 8 bit U and V planes*/
	UV_IMAGE_TYPE_I444,
	/*8 bit Y plane followed by 8 bit 2x2 subsampled V and U planes*/
	UV_IMAGE_TYPE_YV12,
	/*8 bit Y plane followed by 8 bit 1x2 subsampled V and U planes*/
	UV_IMAGE_TYPE_YV16V,
	/*8 bit Y plane followed by 8 bit 2x1 subsampled V and U planes*/
	UV_IMAGE_TYPE_YV16H,
	/*8 bit Y plane followed by 8 bit V and U planes*/
	UV_IMAGE_TYPE_YV24,	

	/*8 bit Y plane followed by 8 bit 2x2 subsampled UV planes*/
	UV_IMAGE_TYPE_NV12,
	/*8 bit Y plane followed by 8 bit 2x2 subsampled VU planes*/
	UV_IMAGE_TYPE_NV21,
	/*8 bit Y plane followed by 8 bit 2x1 subsampled UV planes*/
	UV_IMAGE_TYPE_LPI422H,
	/*8 bit Y plane followed by 8 bit 2x1 subsampled VU planes*/
	UV_IMAGE_TYPE_LPI422H2,

	/*Y0, U0, Y1, V0*/
	UV_IMAGE_TYPE_YUYV,

	/*B, G, R*/
	UV_IMAGE_TYPE_B8G8R8,

	UV_IMAGE_TYPE_BAYER_RAW,

	UV_IMAGE_TYPE_NULL
}uvImageType;

/*Define the image format space*/
typedef struct __tagImageInfo
{
	uvImageType	enImageType;
	uvInt32	    s32Width;
	uvInt32 	s32Height;
	uvUInt8*	pau8Plane[4];
	uvInt32	    as32Pitch[4];
}uvImageInfo;

#endif /*_UVBASE_DEFINE_H_*/
