#!/usr/bin/env bash

find $1 -name '*.png' | grep -v analyze | grep -v node_modules | grep -v .9 | xargs -n1 file -F ' : ' | grep RGBA | awk '{print $1}' | xargs -n1 pngquant -f --skip-if-larger

sum=0
for file in `find . -name '*-fs8.png'`;
do
    dst=`echo $file | awk -F '-fs8' '{print $1.$2}'`

    source_png_size=`ls -l $dst | awk '{printf "%.2f",$5/1024}'`
    opti_png_size=`ls -l $file | awk '{printf "%.2f", $5/1024}'`

    echo
    echo 压缩${dst}...
    percent=`echo "scale=2; ($source_png_size - $opti_png_size) / $source_png_size * 100" | bc -l`

    echo 原始大小 $source_png_size KB, 优化后 $opti_png_size KB, 压缩百分比: $percent%
    sum=`echo "scale=2; $sum + ${source_png_size} - ${opti_png_size}" | bc -l`

    mv -f $file $dst
done

echo 共压缩: $sum KB