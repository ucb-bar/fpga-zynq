fpga-zynq
=========

This repository contains the files needed to run the RISC-V [rocket chip](https://github.com/ucb-bar/rocket-chip) on 
various Zynq FPGA boards ([Zybo](http://www.digilentinc.com/Products/Detail.cfm?NavPath=2,400,1198&Prod=ZYBO), [Zedboard](http://zedboard.org/product/zedboard), [ZC706](http://www.xilinx.com/products/boards-and-kits/EK-Z7-ZC706-G.htm)) with Vivado 2014.2. Efforts have been made to not only automate the process of generating files for these boards, but to also reduce duplication as well as the size of this repo. Prebuilt images are available in git submodules, and they are only shallowly cloned if requested.


####Overview of System Stack
Our system will allow you to run a RISC-V binary on a rocket core instantiated on a supported Zynq FPGA. This section will outline the stack of all of the parts involved and by proxy, outline the rest of the documentation. Going top-down from the RISC-V binary to the development system:

**Target Application** (RISC-V binary)
 will run on top of whatever kernel the rocket chip is running. Compiled by [riscv-gcc](https://github.com/ucb-bar/riscv-gcc) or [riscv-llvm](https://github.com/ucb-bar/riscv-llvm).

**RISC-V Kernel** ([Proxy Kernel](https://github.com/ucb-bar/riscv-pk) or [RISC-V Linux](https://github.com/ucb-bar/riscv-linux))
 runs on top of the rocket chip. The proxy kernel is extremely lightweight and designed to be used with a single binary linked against newlib while RISC-V linux is appropriate for everything else.

**Rocket Chip** ([rocket core](https://github.com/ucb-bar/rocket) with L1 instruction and data caches)
 is instantiated on the FPGA. Many of its structures will typically map to various hard blocks including BRAMs and DSP slices. It communicates to the host ARM core on the Zynq via AXI.

**Front-end Server** ([riscv-fesvr](https://github.com/ucb-bar/riscv-fesvr))
 runs on the host ARM core and provides an interface to the rocket chip running on the FPGA (connected via AXI).

**Zynq ARM Core** (acutally dual Cortex A9)
 runs linux and simplifies interfacing with the FPGA.

**FPGA Board** (zybo, zedboard, or zc706)
 contains the Zynq FPGA and several I/O devices. At power on, the contents of the SD card are used to configure the FPGA and boot linux on the ARM core.

**External Communication** (tty over serial on usb or telnet/ssh over ethernet)
 allows the development system to communicate with the FPGA board.



Quick Instructions
------------------
_Using prebuilt images get hello world on board_

First, enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). From there:

    $ make fetch-images

Insert the SD card on the development system and copy over the images:

    $ make load-sd SD=path_to_mounted_sdcard

Eject the SD card, insert it into the board and power the board on. Connect to the board with an ethernet cable (password is _root_) and run hello world:

    $ ssh root@192.168.1.5
    root@zynq:~# ./fesvr-zedboard pk hello



Connecting to the Board
-----------------------
#####Ethernet
The board has an IP of 192.168.1.5 and can be accessed by username/password of root/root on telnet and ssh. For example:

    $ ssh root@192.168.1.5

_Note:_ Make sure your development system ethernet interface is configured to be on the 192.168.1.x subnet.

#####Serial-USB
On the zybo and zedboard a single serial-USB cable is needed but on the zc706 you will also need a USB type A to type B cable (and possibly to install drivers)

    $ screen /dev/tty.usbmodem1411 115200,cs8,-parenb,-cstopb

_Note:_ The numbers following `tty.usbmodem` may vary slightly.



Working with Vivado
-------------------
_Requires: Vivado 2014.2 and its settings64.sh sourced_

First, enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). To generate a Vivado project from scratch:

    $ make project

To generate a bitstream from the command-line:

    $ make bitstream

To launch Vivado in GUI mode:

    $ make vivado



TODO: merge remaining instructions from current fpga-zynq repo
