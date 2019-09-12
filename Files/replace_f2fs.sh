#!/bin/bash

cd mtk/alps


allBranch=`git branch -a | grep "remotes" | grep -v "master" | grep -v "_patch" | awk -F "/" '{print$NF}'`
for i in $allBranch
do
    echo "current branch is $i"
    git checkout $i
    git pull

    if [ `find device -name *.mk | xargs grep -Fnr "ro.mtk_f2fs_enable=1" | wc -l` -gt 0 ];then
        find device -name *.mk | xargs sed -i "s/ro.mtk_f2fs_enable=1/ro.vendor.mtk_f2fs_enable=1/g"
        git add device/*
        git commit -m "[ALL] [NONE] Replace ro.mtk_f2fs_enable=1 to ro.vendor.mtk_f2fs_enable=1"
        git push origin HEAD:refs/for/$i
    fi
done



删除匹配行：sed -i '/^MTK_WAPI_SUPPORT = yes/d' `grep "MTK_WAPI_SUPPORT = yes" -rl device/mediateksample`
在某行前一行插入：sed -i '/2222222222/i\3333333333' `grep "MTK_WAPI_SUPPORT = yes" -rl device/mediateksample`
在某行后一行插入：sed -i '/2222222222/a\3333333333' `grep "MTK_WAPI_SUPPORT = yes" -rl device/mediateksample`
添加空行：sed -i '$a\string' $mkpath/$PROJECT_NEW/ProjectConfig.mk
