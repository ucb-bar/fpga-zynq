fpga-zynq
=========

This repository contains the files needed to run the RISC-V [rocket chip](https://github.com/ucb-bar/rocket-chip) on 
various Zynq FPGA boards ([Zybo](http://www.digilentinc.com/Products/Detail.cfm?NavPath=2,400,1198&Prod=ZYBO), [Zedboard](http://zedboard.org/product/zedboard), [ZC706](http://www.xilinx.com/products/boards-and-kits/EK-Z7-ZC706-G.htm)) with Vivado 2014.2. Efforts have been made to not only automate the process of generating files for these boards, but to also reduce duplication as well as the size of this repo. Prebuilt images are available in git submodules, and they are only cloned if requested.


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

**External Communication** (tty over uart on usb or telnet/ssh over ethernet)
 allows the development system to communicate with the FPGA board.



Quick Instructions
------------------

First, enter into the directory for your board (current options are `zybo`, 
`zc706`, and `zedboard`). From there:

    $ make vivado_gui


TODO: merge remaining instructions from current fpga-zynq repo
