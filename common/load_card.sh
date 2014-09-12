#!/bin/bash

if [ -z "$1" ]
  then
    echo "Please provide a path to a SD card"
    exit 1
fi

fpga_images_dir=`ls -d fpga-images-*`

set -x
cp $fpga_images_dir/boot.bin $1
cp $fpga_images_dir/devicetree.dtb $1
cp $fpga_images_dir/uImage $1
cp $fpga_images_dir/uramdisk.image.gz $1
