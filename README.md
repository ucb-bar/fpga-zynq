fpga-zynq
=========

This repository contains the files needed to run the RISC-V [Rocket Chip](https://github.com/ucb-bar/rocket-chip) on 
various Zynq FPGA boards ([Zybo](http://www.digilentinc.com/Products/Detail.cfm?NavPath=2,400,1198&Prod=ZYBO), [Zedboard](http://zedboard.org/product/zedboard), [zc706](http://www.xilinx.com/products/boards-and-kits/EK-Z7-ZC706-G.htm)). Efforts have been made to not only automate the process of generating files for these boards, but to also reduce duplication as well as the size of this repo. Prebuilt images are available in git submodules, and they are only cloned if requested.



Quick Instructions
--------------

First, enter into the directory for your board (current options are `zybo`, 
`zc706`, and `zedboard`). From there:

    $ make vivado_gui


TODO: merge remaining instructions from current fpga-zynq repo
