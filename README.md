fpga-zynq
=========

This repository contains the files needed to run the RISC-V Rocket core on 
various Zynq FPGAs from scratch.

Quick Instructions
--------------

First, enter into the directory for your board (current options are zybo, 
zc706, and zedboard). From there:

    $ make
    $ make vivadoproject
    $ cd hw/zynq_rocketchip
    $ vivado zynq_rocketchip.xpr


TODO: merge remaining instructions from current fpga-zynq repo
