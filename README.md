Rocket Chip on Zynq FPGAs
=========================
### Warning: This repository is deprecated and does not track Rocket Chip master.
#### Those looking for an FPGA prototype of Rocket Chip should checkout SiFive’s [Freedom platform](https://github.com/sifive/freedom). Those looking for an FPGA-accelerated simulation environment should see [FireSim](https://fires.im/) ([repo, TBR](https://github.com/firesim/firesim)) which will be released in the Summer

This repository contains the files needed to run the RISC-V [rocket chip](https://github.com/ucb-bar/rocket-chip) on 
various Zynq FPGA boards ([Zybo](http://www.digilentinc.com/Products/Detail.cfm?NavPath=2,400,1198&Prod=ZYBO), [Zedboard](http://zedboard.org/product/zedboard), [ZC706](http://www.xilinx.com/products/boards-and-kits/EK-Z7-ZC706-G.htm)) with Vivado 2016.2. Efforts have been made to not only automate the process of generating files for these boards, but to also reduce duplication as well as the size of this repo. Prebuilt images are available in git submodules, and they are only shallowly cloned if requested.


### How to use this README

This README contains 3 major sets of instructions:

1) [Quick Instructions](#quickinst): This is the simplest way to get started - you'll download the relevant prebuilt images for your board and learn how to run binaries on the RISC-V Rocket Core. These instructions require only that you have a compatible board - neither Vivado nor the RISC-V Toolchain are necessary.

2) [Pushing Your Rocket Modifications to the FPGA](#bitstream): These instructions walk through what we believe is the common case - a user wanting to utilize a custom-generated Rocket Core.

3) [Building Everything from Scratch](#fromscratch): Here, we discuss how to build the full stack from scratch. It is unlikely that you'll need to use these instructions, unless you are intending to make changes to the configuration of the Zynq ARM Core or `u-boot`.

Finally, the bottom of the README contains a set of [Appendices](#appendices), which document some common operations that we believe are useful or provides more depth on commands described elsewhere in the documentation.

To guide you through the rest of the documentation, we have provide both a [Table of Contents](#toc) and an [Overview](#overview).

_Note_: If you are seeking to test your own modifications to rocket chip (RC) using this repository, it must be derived from RC commit [fb476d1](https://github.com/ucb-bar/rocket-chip/tree/fb476d193cc21dd66d81cc5890883bd466f889f7) or later, as this project expects both a debug module to be present, and links directly against RC to build a top-level project directly in chisel. Otherwise, you should use an older version of this repository [bf6d00c2](https://github.com/ucb-bar/fpga-zynq/tree/bf6d00c211c46d60662a9ef6e9460a405cc9b0d5) or earlier, which has support for HTIF-based rocket chip instances.

### <a name="toc"></a> Table of Contents
+ [Overview of System Stack](#overview)
+ [1 - Quick Instructions](#quickinst)
+ [2 - Pushing Your Rocket Modifications to the FPGA](#bitstream)
    + [Setting Up Your Workspace](#workspace)
    + [Configuring Rocket Chip](#configRC)
    + [Generating Verilog for Rocket Chip](#genRC)
    + [Generating Project for Configuration](#projRC)
    + [Repacking `boot.bin`](#repack)
+ [3 - Building Everything from Scratch](#fromscratch)
    + [Project Setup](#setup)
    + [Generating a Bitstream](#bitstream)
    + [Building the FSBL](#fsbl)
    + [Building u-boot for the Zynq ARM Core](#u-boot)
    + [Creating `boot.bin`](#boot.bin)
    + [Building linux for the ARM PS](#arm-linux)
    + [Building riscv-linux](#riscv-linux)
    + [Booting Up and Interacting with the RISC-V Rocket Core](#booting)
+ [Appendices](#appendices)
    + [Connecting to the Board](#connecting)
    + [Getting Files On & Off the Board](#transferring)
    + [Working with Vivado](#vivado)
    + [Changing the Processor's Clockrate](#clockrate)
    + [Contents of the SD Card](#sdcard)
    + [Building fesvr-zynq](#fesvr)
    + [Building riscv-tools for Zybo](#zybotools)
+ [Acknowledgements](#ack)



### <a name="overview"></a> Overview of System Stack
Our system will allow you to run a RISC-V binary on a rocket core instantiated on a supported Zynq FPGA. This section will outline the stack of all of the parts involved and by proxy, outline the rest of the documentation. Going top-down from the RISC-V binary to the development system:

**Target Application** (RISC-V binary)
 will run on top of whatever kernel the rocket chip is running. Compiled by [riscv-gcc](https://github.com/riscv/riscv-gcc) or [riscv-llvm](https://github.com/riscv/riscv-llvm).

**RISC-V Kernel** ([proxy kernel](https://github.com/riscv/riscv-pk) or [RISC-V Linux](https://github.com/riscv/riscv-linux))
 runs on top of the rocket chip. The proxy kernel is extremely lightweight and designed to be used with a single binary linked against Newlib while RISC-V Linux is appropriate for everything else.

**Rocket Chip** ([rocket core](https://github.com/ucb-bar/rocket) with L1 instruction and data caches)
 is instantiated on the FPGA. Many of its structures will typically map to various hard blocks including BRAMs and DSP slices. It communicates to the host ARM core on the Zynq via AXI.

**Front-end Server** ([riscv-fesvr](https://github.com/riscv/riscv-fesvr))
 runs on the host ARM core and provides an interface to the rocket chip running on the FPGA (connected via AXI).

**Zynq ARM Core** (actually dual Cortex A9)
 runs Linux and simplifies interfacing with the FPGA.

**FPGA Board** (Zybo, Zedboard, or ZC706)
 contains the Zynq FPGA and several I/O devices. At power on, the contents of the SD card are used to configure the FPGA and boot Linux on the ARM core.

**External Communication** (TTY over serial on USB or telnet/ssh over ethernet)
 allows the development system to communicate with the FPGA board.

**Development System** (PC with SD card reader)
 generates the images to configure the FPGA.



1)<a name="quickinst"></a> Quick Instructions 
------------------
_Using prebuilt images, run hello world and/or linux on rocket_

First, enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). From there, run the following to download all of the necessary images:

    $ make fetch-images

Next, insert the SD card on the development system and copy over the images:

    $ make load-sd SD=path_to_mounted_sdcard

Finally, eject the SD card, insert it into the board, set the board's boot jumper to "SD", and power the board on. Connect to the board with an ethernet cable (password is _root_) and run hello world:

    $ ssh root@192.168.1.5
    root@zynq:~# ./fesvr-zynq pk hello
    hello!

Awesome! You can now run RISC-V binaries on Rocket. If you'd like to boot linux on the Rocket core, see _[Booting Up and Interacting with the RISC-V Rocket Core](#booting)_.



2)<a name="bitstream"></a> Pushing Your Rocket Modifications to the FPGA
-------------------------

#### <a name="workspace"></a> Setting Up Your Workspace
_Requires: Vivado 2016.2 and its settings64.sh and a JVM that can run Scala_

After you clone the repository for the first time, you must initialize
the submodules rocket-chip and testchipip, as well as the first-level
submodules of rocket-chip itself.

    $ make init-submodules

If you have your own working rocket-chip directory that you would like to use, override the `ROCKET_DIR` make variable set in common/Makefrag.

#### <a name="configRC"></a> Configuring Rocket Chip

The verilog for the rocket chip is generated by [Chisel](https://chisel.eecs.berkeley.edu) and thus is not intended to be edited by humans. This project instantiates rocket chip as module larger top level chisel project, that includes an adapter to interface the ARM core with rocket chip's debug module.

The configuration used to generate the rocket chip comes from the `CONFIG` environment variables. If `CONFIG` isn't set by the environment, it is taken from the `Makefile` for the current board. For this example, we use the Zybo which has a default configuration of `ZynqSmallConfig`.

#### <a name="genRC"></a> Generating Verilog for Rocket Chip

Enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). After making changes within `rocket-chip` and/or `common/src/main/scala`, you can run the rocket chip generator and copy the newly generated verilog back into the board's src/verilog directory with:

     $ make rocket

You can also explicitly set the `CONFIG` variable from the command-line (can do this for any command):

     $ make rocket CONFIG=MyFPGAConfig

By default this will look up a configuration specified in the rocket chip library. You may define a custom one without recompiling rocketchip, by defining in the zynq chisel sources at `common/src/main/scala`, and instead calling:

     $ make rocket CONFIG_PROJECT=zynq CONFIG=MyCustomZynqConfig

The generator will instead look for the configuration definition in the local project instead of the rocket chip library.


#### <a name="projRC"></a> Generating Project for Configuration
To generate a Vivado project specific to the board and the configuration (one project per configuration):

    $ make project

This step only needs to be done once per configuration.


#### <a name="repack"></a> Repacking `boot.bin`

Once you have changed the design, you will need to generate a new bitstream and that will need to be packaged in `boot.bin`. `boot.bin` also contains the binaries needed for startup (`FSBL.elf` and `u-boot.elf`) but these can be reused. From within the board's directory (_zybo_ in this example), to repack `boot.bin`:

    $ make fpga-images-zybo/boot.bin

If you have modified the verilog for your project but not generated a new bitstream, `make` should generate a new bitstream automatically. To use the new `boot.bin`, copy it to the SD card, insert the SD card into the board, and power on the board.



3)<a name="fromscratch"></a> Building Everything from Scratch
-----------------------
This section describes how to build the entire project from scratch. Most likely, you will not need to perform all of these steps, however we keep them here for reference. Various other sections of this README may selectively refer to these sections. This section assumes that you've just pulled this repository and have sourced the settings file for Vivado 2016.2.

For ease of exposition, we will be describing all of the commands assuming that we are working with the `zybo` and its default configuration `ZynqSmallConfig`. Replacing references to the `zybo` with `zedboard` or `zc706` will allow you to use these instructions for those boards.

From here on, `$REPO` will refer to the location of the `fpga-zynq` repository.

### 3.1) <a name="setup"></a> Project Setup

First, we need to generate a Vivado project from the source files that are present in a particular board's directory. 

	$ cd $REPO/zybo
	$ make project
	
### 3.2) <a name="bitstream"></a> Generating a Bitstream
	
Next, let's open up the project in the Vivado GUI:

	$ make vivado
	# OR
	$ cd zybo_rocketchip_ZynqSmallConfig
	$ vivado zybo_rocketchip_ZynqSmallConfig.xpr

If you wish to make any modifications to the project, you may now do so. Once you've finished, let's move on:

Inside Vivado, select _Open Block Design_ followed by _system.bd_ in the dropdown. This will open a block diagram for the Zynq PS Configuration and is necessary for correct FSBL generation.

Next, select _Generate Bitstream_. Vivado will now step through the usual Synthesis/Implementation steps. Upon completion, if you're interested in only the bitstream, you can stop here; the file you want is in:

`$REPO/zybo/zybo_rocketchip_ZynqSmallConfig/zybo_rocketchip_ZynqSmallConfig.runs/impl_1/rocketchip_wrapper.bit`

Otherwise, let's continue on to select _Open Implemented Design_. This is again necessary to properly export the description of our Hardware for the Xilinx SDK to use.

At this point, select _File -> Export -> Export Hardware_. This will create the following directory:

`$REPO/zybo/zybo_rocketchip_ZynqSmallConfig/zybo_rocketchip_ZynqSmallConfig.sdk`

This directory contains a variety of files that provide information about the hardware to the SDK. Let's continue on to building the FSBL.


### 3.3) <a name="fsbl"></a> Building the FSBL

This step assumes that you have just generated the bitstream. Inside the Vivado GUI, select "Launch SDK". This will open up the Xilinx SDK preconfigured with the description of our hardware. In order to generate the FSBL, do the following:

1) Select _File -> New -> Application Project_

2) In the new window, type "FSBL" as the Project name, and ensure that the rest of the properties are correctly set (disregarding the greyed out _Location_ field):

<img src="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/FSBL.png" width="400"/>

3) Select _Next_, at which point you should be given a set of options. Select _Zynq FSBL_ and _Finish_.

4) The SDK will proceed to automatically compile the FSBL. You can see the progress in the Console.

5) Once the build is finished, we need to build u-boot before returning to the SDK in order to create our BOOT.bin.

### 3.4) <a name="u-boot"></a> Building u-boot for the Zynq ARM Core

Returning to the command line, do the following from the directory corresponding to your board:

	$ make arm-uboot
	
This target performs a variety of commands. It will first pull the u-boot source from the Xilinx repositories (see the submodule in `$REPO/common/u-boot-xlnx`), patch it with the necessary files found in `$REPO/zybo/soft_config/`, compile u-boot, and place the resulting u-boot.elf file in `$REPO/zybo/soft_build/u-boot.elf`. 

### 3.5) <a name="boot.bin"></a> Creating `boot.bin`

At this point, we have built up all of the necessary components to create our `boot.bin` file. Returning to the Xilinx SDK, select _Xilinx Tools -> Create Zynq Boot Image_. 

First, you should fill in the _Output BIF file path_ with `$REPO/zybo/deliver_output`. If this directory has not already been created, you may go ahead and create it (this is where we will place all of the items that we will ultimately transfer to the SD card). See the below for a sample path. Performing this step will also fill in the _Output path_ field, which specifies the location of the `BOOT.bin` file that we desire. 

Next, we will add the individual files that make up `BOOT.bin`. Order is important, so follow these steps exactly:

1) Select _Add_ and in the window that opens, click _Browse_ and specify the following location:

`$REPO/zybo/zybo_rocketchip_ZynqSmallConfig/zybo_rocketchip_ZynqSmallConfig.sdk/FSBL/Debug/FSBL.elf`

Once you have done so select the dropdown next to _Partition type_ and select _bootloader_. You must perform this step **after** selecting the path, else the SDK will change it back to _datafile_, and your `BOOT.bin` will not work.

At the conclusion of this step, the _Add partition_ window will look something like:

<img src="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/selectFSBL.png" width="400" />

Click _OK_to return to the previous window.

2) Once more, click _Add_. In the new _Add partition_ window, click _Browse_ and specify the following location:

`$REPO/zybo/zybo_rocketchip_ZynqSmallConfig/zybo_rocketchip_ZynqSmallConfig.runs/impl_1/rocketchip_wrapper.bit`

Ensure that _Partition type_ is set to datafile and click _OK_.

3) Click _Add_ a final time. Click _Browse_ and this time select our compiled `u-boot.elf`:

`$REPO/zybo/soft_build/u-boot.elf`

Again, ensure that _Partition type_ is set to datafile and click _OK_.

4) At this point, the window should match the following (click the image to zoom in):

<a href="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/boot_image.png" target="_new"><img src="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/boot_image.png" width="400" /></a>

Select _Create Image_. This will produce a `BOOT.bin` file in the `$REPO/zybo/deliver_output` directory.

If you make modifications to the project in the future, you can avoid having to perform this step manually and
instead may reuse the output.bif file that the SDK generates the first time you use _Create Zynq Boot Image._
Use the following make target to do so:

    $ make deliver_output/boot.bin

### 3.6) <a name="arm-linux"></a> Building linux for the ARM PS

As part of our bootstrapping process, we need to boot linux on the ARM core in the Zynq. We can build this copy of linux like so (again assuming that we are in `$REPO/zybo`):

	$ make arm-linux
	
We additionally need to produce the `devicetree.dtb` file that linux will use to setup peripherals of the ARM core. We can produce this like so:

	$ make arm-dtb
	
At this point, the `$REPO/zybo/deliver_output` directory contains the following files:

* `BOOT.bin` - (the filename is case insensitive, you may see `boot.bin`). This contains the FSBL, the bitstream with Rocket, and u-boot. 
* `uImage` - Linux for the ARM PS
* `devicetree.dtb` - Contains information about the ARM core's peripherals for linux.

The only remaining file that we are missing at this point is `uramdisk.image.gz`, the root filesystem for linux on the ARM Core. You can obtain it like so (it will be placed in `$REPO/zybo/deliver_output`):

    $ make fetch-ramdisk

Now, take the four files in `deliver_output/`, and place them on the root of the SD card that we will insert into the Zybo. The layout of your SD card should match the following:

	SD_ROOT/
	|-> boot.bin
	|-> devicetree.dtb
	|-> uImage
	|-> uramdisk.image.gz

At this point, you have performed the necessary steps to run binaries on Rocket. See [Section 3.8](#booting) for how to do so. If you are interested in running riscv-linux on Rocket, continue on to Section 3.7:

### 3.7) <a name="riscv-linux"></a> Building/Obtaining riscv-linux

There are two options to obtain riscv-linux:

#### Method 1) Build from Source

To build [riscv-linux](http://github.com/riscv/riscv-linux) for Rocket, follow the instructions [here](https://github.com/riscv/riscv-tools#linuxman).
These instructions will show you how to create a linux image that boots from an initramfs.
We also now have support for block devices, so you can also boot from an ext2 image created by [buildroot](https://github.com/sifive/buildroot).
To configure linux to boot from a block device, instead of selecting "Initial RAM filesystem and RAM disk",
add the arguments "root=/dev/generic-blkdev rw" to the kernel command line
under "Kernel Hacking" -> "Built-in Kernel Command String". To use the block
device, you will need to use the ucbbar-all branch of riscv-linux.

Next, you'll need to build an instance of the Berkeley Bootloader(BBL) that contains your linux image as a payload. BBL is provided alongside the proxy kernel at [this repository](https://github.com/riscv/riscv-pk).

Finally, drop your bbl image into SD_ROOT/, which will be mounted as `/mnt/boot/` in the ARM core's filesystem.

Warning: If you are working with the Zybo, you need to make sure you compile with a version of the riscv-gnu-toolchain that targets RV64IMA, as the zybo configuration does not possess an FPU.

#### Method 2) Use the provided BBL instance

Included in the home directory of the ARM core's ramdisk we've provided an instance of bbl preloaded with a miniminal linux image. All you have to do is follow the instructions in the [next](#booting) section.
 
### 3.8) <a name="booting"></a> Booting Up and Interacting with the RISC-V Rocket Core

First, insert the SD card and follow the instructions in [Appendix A](#connecting) to connect to your board. You can login to the board with username _root_ and password _root_. Once you're at the prompt, you can run a basic hello world program on rocket like so:

    root@zynq:~# ./fesvr-zynq pk hello
    hello!

To boot riscv-linux, run:

    root@zynq:~# ./fesvr-zynq bbl
                  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                      vvvvvvvvvvvvvvvvvvvvvvvvvvvv
    rrrrrrrrrrrrr       vvvvvvvvvvvvvvvvvvvvvvvvvv
    rrrrrrrrrrrrrrrr      vvvvvvvvvvvvvvvvvvvvvvvv
    rrrrrrrrrrrrrrrrrr    vvvvvvvvvvvvvvvvvvvvvvvv
    rrrrrrrrrrrrrrrrrr    vvvvvvvvvvvvvvvvvvvvvvvv
    rrrrrrrrrrrrrrrrrr    vvvvvvvvvvvvvvvvvvvvvvvv
    rrrrrrrrrrrrrrrr      vvvvvvvvvvvvvvvvvvvvvv  
    rrrrrrrrrrrrr       vvvvvvvvvvvvvvvvvvvvvv    
    rr                vvvvvvvvvvvvvvvvvvvvvv      
    rr            vvvvvvvvvvvvvvvvvvvvvvvv      rr
    rrrr      vvvvvvvvvvvvvvvvvvvvvvvvvv      rrrr
    rrrrrr      vvvvvvvvvvvvvvvvvvvvvv      rrrrrr
    rrrrrrrr      vvvvvvvvvvvvvvvvvv      rrrrrrrr
    rrrrrrrrrr      vvvvvvvvvvvvvv      rrrrrrrrrr
    rrrrrrrrrrrr      vvvvvvvvvv      rrrrrrrrrrrr
    rrrrrrrrrrrrrr      vvvvvv      rrrrrrrrrrrrrr
    rrrrrrrrrrrrrrrr      vv      rrrrrrrrrrrrrrrr
    rrrrrrrrrrrrrrrrrr          rrrrrrrrrrrrrrrrrr
    rrrrrrrrrrrrrrrrrrrr      rrrrrrrrrrrrrrrrrrrr
    rrrrrrrrrrrrrrrrrrrrrr  rrrrrrrrrrrrrrrrrrrrrr

           INSTRUCTION SETS WANT TO BE FREE
    [    0.000000] Linux version 4.6.2 <more messages follow>

After linux boots you'll be presented with a busybox prompt from riscv-linux running on rocket!

If you are using a root filesystem on a seperate filesystem image, you can
boot linux by running

    root@zynq:~# ./fesvr-zynq +blkdev=rootfs.ext2 bbl

<a name="appendices"></a> Appendices 
------------

### A) <a name="connecting"></a> Connecting to the Board

#### Serial-USB
On the Zybo and Zedboard a single serial-USB cable is needed but on the ZC706 you will also need a USB type A to type B cable (and possibly some drivers). To connect:

    $ screen /dev/tty.usbmodem1411 115200,cs8,-parenb,-cstopb

_Note:_ The numbers following `tty.usbmodem` may vary slightly. On the Zybo, 
replace `usbmodem` with `usbserial-` and on the ZC706, replace it with 
`SLAB_USBtoUART`.

#### Ethernet
The board has an IP of 192.168.1.5 and can be accessed by username/password of root/root on telnet and ssh. For example:

    $ ssh root@192.168.1.5

_Note:_ Make sure your development system ethernet interface is configured to be on the 192.168.1.x subnet. The default configuration intends for the board to be directly attached to the development system (single cable). If you want to place the board on a larger network, we recommend changing the root password to something stronger and changing the IP configuration to mesh well with your network.


### B) <a name="transferring"></a> Getting Files On & Off the Board

#### Copying Files over Ethernet
The easiest way to get a file onto the board is to copy it with scp over ethernet:

    $ scp file root@192.168.1.5:~/

_Note:_ Linux is running out of a RAMdisk, so to make a file available after a reboot, copy it to the SD card or modify the RAMdisk.

#### Changing the RAMDisk
_Requires: [u-boot](http://www.denx.de/wiki/U-Boot/) and sudo_

The RAMDisk (`uramdisk.image.gz`) that holds Linux for the ARM cores is a gzipped cpio archive with a u-boot header for the board. To open the RAMdisk:

    $ make ramdisk-open

When changing or adding files, be sure to keep track of owners, groups, and permissions. When you are done, to package it back up:

    $ make ramdisk-close

A useful application of this is to add your SSH public key to `.ssh/authorized_keys` so you can have passwordless login to the board.

_Note:_ Since these ramdisk operations use sudo on files, they may not work on a network mounted filesystem. To get around this limitation, it is easiest to just copy it to a local filesystem when modifying the ramdisk.


### C) <a name="vivado"></a> Working with Vivado

_Requires: Vivado 2016.2 and its settings64.sh sourced_

First, enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). To generate a bitstream, you will need a Vivado project. You should only need to generate it once, but the automation this repo provides makes it easy to generate again if you delete the project. To generate a Vivado project from scratch:

    $ make project

To generate a bitstream from the command-line:

    $ make bitstream

To launch Vivado in GUI mode:

    $ make vivado


### D) <a name="clockrate"></a> Changing the Processor's Clockrate
You can change the clockrate for the rocket chip by changing `RC_CLK_MULT` and `RC_CLK_DIVIDE` within a board's `src/verilog/clocking.vh`. After that change, you will need to generate a new bitstream (and `boot.bin`).

_Note:_ Although rarely needed, it is possible to change the input clockrate to the FPGA by changing it within the block design, `src/constrs/base.xdc`, and `ZYNQ_CLK_PERIOD` within `src/verilog/clocking.vh`. This will also require regenerating `FSBL.elf`, the bitstream, and of course `boot.bin`.


### E) <a name="sdcard"></a> Contents of the SD Card
The SD card is used by the board to configure the FPGA and boot up the ARM core. All of these files are available within a board's fpga-images submodule, but they can also be built from scratch. Here is a summary of the files and their purposes:

* `boot.bin` is generated by the Xilinx SDK and is actually three files. To generate it from scratch, follow the instructions from Section 3 up through [Section 3.5 Creating boot.bin](#boot.bin). To repack it from existing components, follow [Repacking boot.bin](#repack). `boot.bin` contains:
  * Bitstream (`rocketchip_wrapper.bit`) configures the FPGA with the rocket chip design. To build it with the GUI, see [Section 3.2 Generating a Bitstream](#bitstream) and to build it with the command-line, see: [Working with Vivado](#vivado).
  * First Stage Bootloader (`FSBL.elf`) - This bootloader configures the Zynq processing system based on the block design in the Vivado project. The FSBL will hand-off to `u-boot` once the processing system is setup. We build the FSBL using the Xilinx SDK and hardware information exported from Vivado. (see [Section 3.3](#fsbl))
  * u-boot (`u-boot.elf`) - This bootloader takes configuration information and prepares the ARM processing system for booting linux. Once configuration is complete, `u-boot` will hand-off execution to the ARM linux kernel. We build `u-boot` directly from the [Xilinx u-boot repository](https://github.com/Xilinx/u-boot-xlnx), with some configuration modifications to support Rocket. (see [Section 3.4](#u-boot))
* ARM Linux (`uImage`) - This is a copy of linux designed to run on the ARM processing system. From within this linux environment, we will be able to run tools (like `fesvr-zedboard`) to interact with the RISC-V Rocket Core. We build directly from the [Xilinx linux repository](https://github.com/Xilinx/linux-xlnx), with a custom device tree file to support Rocket. (see [Section 3.6](#arm-linux))
* ARM RAMDisk (`uramdisk.image.gz`) - The RAMDisk is mounted by ARM Linux and contains the root filesystem. For obtaining it, see [Section 3.6](#arm-linux), and for modifying it, see [Appendix B](#transferring).
* `devicetree.dtb` - Contains information about the ARM core's peripherals for Linux. (See [Section 3.6](#arm-linux))


### F) <a name="fesvr"></a> Building fesvr-zynq

The [riscv-fesvr repo](http://github.com/riscv/riscv-fesvr) provides against which the zynq-fesvr is linked. Additionally, `common/csrc` includes source for main, and a simple driver, which hands off debug module requests and reponses between the ARM core and rocket chip. Before building, make sure the 2016.2 version of settings64.sh is sourced. To build the riscv-fesvr binary for Linux ARM target (to run on Zynq board), type:

    $ make fesvr-zynq

and make sure you have the Xilinx SDK in your PATH, and the riscv-tools/riscv-fesvr submodule initialized in your rocket chip directory. When installing fesvr-zynq, don't forget to copy the library as well (`common/build/libfesvr.so` to `/usr/local/lib` on the board).


### G) <a name="zybotools"></a> Building riscv-tools for Zybo

The Zybo build was last tested with [this version of the toolchain](https://github.com/ucb-bar/rocket-chip/commit/2f71a3da5a7d41b4aa2c7a617902f2aee8f2cbe1).

Because the Zybo board uses `ZynqSmallConfig`, [riscv-tools](https://github.com/riscv/riscv-tools) must be recompiled to omit floating point instructions. Add the `--with-arch=RV64IMA` tag to the line in `build.sh` that builds [riscv-gnu-toolchain](https://github.com/riscv/riscv-gnu-toolchain). It should read as follows:

    build_project riscv-gnu-toolchain --prefix=$RISCV --with-arch=RV64IMA

Then run `./build.sh` as normal.

When testing on spike, run spike with the `--isa=RV64IMA` flag.

If [pk](https://github.com/riscv/riscv-pk) does not work, make sure it is being built using this version of the toolchain, since it is specifically generated to not have floating point instructions. Also make sure any binaries you want to run on the Zybo are compiled using this toolchain.

<a name="ack"></a> Acknowledgments 
---------------
In addition to those that [contributed](https://github.com/ucb-bar/rocket-chip#contributors) to rocket chip, this repository is based on internal repositories contributed by:

- Rimas Avizienis
- Jonathan Bachrach
- David Biancolin
- Scott Beamer
- Sagar Karandikar
- Deborah Soung
- Andrew Waterman

