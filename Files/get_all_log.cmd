@echo "Pull important information from hang phone"
adb devices

adb root

adb remount

@echo "attach sdcard/mtklog"
adb pull /sdcard/mtklog E:mtklog/

@echo ¡°attach data/mtklog¡±
adb pull /data/mtklog E:mtklog/data

@echo "attach trace"
adb pull /data/anr E:mtklog/anr

@echo "attach rtt dump for surfaceflinger"
adb pull /data/rtt_dump* E:mtklog/sf_dump
adb pull /data/anr/sf_rtt E:mtklog/sf_rtt_1

@echo "attach data aee db"
adb pull /data/aee_exp E:mtklog/data_aee_exp
adb pull /data/vendor/mtklog/aee_exp E:mtklog/data_aee_exp2

@echo "attach data mobilelog"
adb pull /data/mobilelog E:mtklog/data_mobilelog

@echo "attach NE core"
adb pull /data/core E:mtklog/data_core

@echo "attach tombstones"
adb pull /data/tombstones E:mtklog/tombstones

@echo ¡°attach phone state¡±
adb shell ps -t > E:mtklog/ps
adb shell top -t -m 5 -n 3 > E:mtklog/top
adb shell cat /proc/meminfo > E:mtklog/meminfo
adb shell cat /proc/cpuinfo > E:mtklog/cpuinfo
adb shell cat /proc/buddyinfo > E:mtklog/buddyinfo
adb shell cat /proc/sched_debug > E:mtklog/sched_debug
adb shell cat /proc/interrupts > E:mtklog/interrupts
adb shell procrank > E:mtklog/procrank
adb shell service list > E:mtklog/serviceList
adb shell dumpstate > E:mtklog/dumpstate


@echo "finish."

pause
