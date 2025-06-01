#!/bin/bash
# 获取位置参数
arr=(1 2 "小敏")
arr[3]="小明" # 添加一个元素
echo ${arr[0]}
echo ${arr[*]}
for item in ${arr[@]};do
    echo $item
done

