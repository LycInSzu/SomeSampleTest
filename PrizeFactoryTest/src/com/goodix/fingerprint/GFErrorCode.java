/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

public class GFErrorCode {
    // please change this string to your error message in Error enum
    private static final String UNKOWN_ERROR_MESSAGE = "错误";

    private enum Error {
        GF_SUCCESS("成功", 0),

        GF_ERROR_BASE(UNKOWN_ERROR_MESSAGE, 1000),
        GF_ERROR_OUT_OF_MEMORY("内存溢出", 1000 + 1),
        GF_ERROR_OPEN_TA_FAILED("打开TA失败", 1000 + 2),
        GF_ERROR_BAD_PARAMS("参数错误", 1000 + 3),
        GF_ERROR_NO_SPACE("空间不足", 1000 + 4),
        GF_ERROR_REACH_FINGERS_UPLIMIT("指纹已满", 1000 + 5),
        GF_ERROR_NOT_MATCH("匹配失败", 1000 + 6),
        GF_ERROR_CANCELED("已取消", 1000 + 7),
        GF_ERROR_TIMEOUT("超时", 1000 + 8),
        GF_ERROR_PREPROCESS_FAILED("预处理失败", 1000 + 9),
        GF_ERROR_GENERIC(UNKOWN_ERROR_MESSAGE, 1000 + 10),
        GF_ERROR_ACQUIRED_PARTIAL("有效面积过少", 1000 + 11),
        GF_ERROR_ACQUIRED_IMAGER_DIRTY("图像质量太差", 1000 + 12),
        GF_ERROR_DUPLICATE_FINGER("重复手指", 1000 + 13),
        GF_ERROR_OPEN_DEVICE_FAILED("打开设备失败", 1000 + 14),
        GF_ERROR_HAL_GENERAL_ERROR("hal层错误", 1000 + 15),
        GF_ERROR_HAL_FILE_DESCRIPTION_NULL("文件描述空", 1000 + 16),
        GF_ERROR_HAL_IOCTL_FAILED("ioctl调用失败", 1000 + 17),
        GF_ERROR_HAL_TIMER_FUNC(UNKOWN_ERROR_MESSAGE, 1000 + 18),
        GF_ERROR_CORRUPT_CONTENT("文件内容损坏", 1000 + 19),
        GF_ERROR_INCORRECT_VERSION("错误的版本", 1000 + 20),
        GF_ERROR_CORRUPT_OBJECT(UNKOWN_ERROR_MESSAGE, 1000 + 21),
        GF_ERROR_INVALID_DATA("无效数据", 1000 + 22),
        GF_ERROR_SPI_TRANSFER_ERROR("SPI传输错误", 1000 + 23),
        GF_ERROR_SPI_GENERAL_ERROR("SPI错误", 1000 + 24),
        GF_ERROR_SPI_IRQ_HANDLE("中断处理错误", 1000 + 25),
        GF_ERROR_SPI_RAW_DATA_CRC_FAILED("SPI数据crc错误", 1000 + 26),
        GF_ERROR_SPI_RAW_DATA_BUF_BUSY(UNKOWN_ERROR_MESSAGE, 1000 + 27),
        GF_ERROR_SPI_FW_CFG_DATA_ERROR("固件配置文件错误", 1000 + 28),
        GF_ERROR_SPI_FW_DOWNLOAD_FAILED("下固件失败", 1000 + 29),
        GF_ERROR_SPI_CFG_DOWNLOAD_FAILED("下配置失败", 1000 + 30),
        GF_ERROR_SAVE_FP_TEMPLATE("保存模版失败", 1000 + 31),
        GF_ERROR_FP_BUSY(UNKOWN_ERROR_MESSAGE, 1000 + 32),
        GF_ERROR_OPEN_SECURE_OBJECT_FAILED("打开安全文件失败", 1000 + 33),
        GF_ERROR_READ_SECURE_OBJECT_FAILED("读取安全文件失败", 1000 + 34),
        GF_ERROR_WRITE_SECURE_OBJECT_FAILED("写安全文件失败", 1000 + 35),
        GF_ERROR_SECURE_OBJECT_NOT_EXIST("安全文件不存在", 1000 + 36),

        GF_ERROR_WRITE_CONFIG_FAILED("写配置失败", 1000 + 39),
        GF_ERROR_TEST_SENSOR_FAILED(UNKOWN_ERROR_MESSAGE, 1000 + 40),
        GF_ERROR_SET_MODE_FAILED("设模式失败", 1000 + 41),
        GF_ERROR_CHIP_ID_NOT_CORRECT("chip ID错误", 1000 + 42),
        GF_ERROR_MAX_NUM(UNKOWN_ERROR_MESSAGE, 1000 + 43),
        GF_ERROR_TEST_BAD_POINT_FAILED("坏点测试失败", 1000 + 44),
        GF_ERROR_TEST_FRR_FAR_ENROLL_DIFFERENT_FINGER("...", 1000 + 45),
        GF_ERROR_DUPLICATE_AREA("重叠率过高", 1000 + 46),
        GF_ERROR_SPI_COMMUNICATION("SPI通信失败", 1000 + 47),
        GF_ERROR_FINGER_NOT_EXIST("指纹不存在", 1000 + 48),
        GF_ERROR_INVALID_PREPROCESS_VERSION("错误的预处理版本", 1000 + 49),
        GF_ERROR_TA_DEAD("TA crash", 1000 + 50),
        GF_ERROR_NAV_TOO_FAST("手指移动太快", 1000 + 51),
        GF_ERROR_UNSUPPORT_CHIP("不支持的芯片类型", 1000 + 52),
        GF_ERROR_INVALID_FINGER_PRESS(UNKOWN_ERROR_MESSAGE, 1000 + 53),
        GF_ERROR_TA_GENERATE_RANDOM(UNKOWN_ERROR_MESSAGE, 1000 + 54),
        GF_ERROR_BIO_ASSAY_FAIL("活体认证失败", 1000 + 55),
        GF_ERROR_INVALID_HAT_VERSION(UNKOWN_ERROR_MESSAGE, 1000 + 56),
        GF_ERROR_INVALID_CHALLENGE(UNKOWN_ERROR_MESSAGE, 1000 + 57),
        GF_ERROR_UNTRUSTED_ENROLL("不受信任的注册", 1000 + 58),
        GF_ERROR_INVALID_BASE("无效的基帧", 1000 + 59),
        GF_ERROR_SENSOR_BROKEN_CHECK_NEXT_FRAME(UNKOWN_ERROR_MESSAGE, 1000 + 60),
        GF_ERROR_SENSOR_BROKEN_CHECK_ALGO_ERROR(UNKOWN_ERROR_MESSAGE, 1000 + 61),
        GF_ERROR_SENSOR_IS_BROKEN("芯片损坏", 1000 + 62),
        GF_ERROR_SENSOR_NOT_AVAILABLE("芯片不可用", 1000 + 63),
        GF_ERROR_SENSOR_TEST_FAILED("芯片测试失败", 1000 + 64),

        GF_ERROR_NATIVE_SERVICE_BASE("native服务错误", 2000),
        GF_ERROR_NATIVE_SERVICE_GETSERVICE("native服务获取失败", 2000 + 1),
        GF_ERROR_NATIVE_SERVICE_INIT_HAL("native服务初始化hal层失败", 2000 + 2),

        GF_ERROR_MAX("错误", 3000);

        private String mMessage;
        private int mErrorCode;

        private Error(String name, int code) {
            this.mMessage = name;
            this.mErrorCode = code;
        }
    }

    public static String getErrorMessage(int errorCode) {
        for (Error e : Error.values()) {
            if (e.mErrorCode == errorCode) {
                return e.mMessage;
            }
        }
        return null;
    }
}
