//
// Created by Abel on 2019/3/15.
//

#ifndef XMCAMERA32BIT_UVFILTERINTERFACE_H
#define XMCAMERA32BIT_UVFILTERINTERFACE_H

class UVFilterInterface {

public:
    UVFilterInterface();
    ~UVFilterInterface();

public:
    //filterEffectStyle : 0 Close filter;
    //                  : 1~9 (or 1~100) Stand for filter type;
    void onParameters(int filterEffectStyle) {mFilterEffectStyle = filterEffectStyle;}
    void onShotData(void* data, int width, int height, int format);

private:
    int mFilterEffectStyle;
};

#endif //XMCAMERA32BIT_UVFILTERINTERFACE_H
