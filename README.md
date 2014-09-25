Rocket Chip on Zynq FPGAs
=========================

This repository contains the files needed to run the RISC-V [rocket chip](https://github.com/ucb-bar/rocket-chip) on 
various Zynq FPGA boards ([Zybo](http://www.digilentinc.com/Products/Detail.cfm?NavPath=2,400,1198&Prod=ZYBO), [Zedboard](http://zedboard.org/product/zedboard), [ZC706](http://www.xilinx.com/products/boards-and-kits/EK-Z7-ZC706-G.htm)) with Vivado 2014.2. Efforts have been made to not only automate the process of generating files for these boards, but to also reduce duplication as well as the size of this repo. Prebuilt images are available in git submodules, and they are only shallowly cloned if requested.


###How to use this README

This README contains 3 major sets of instructions:

1) [Quick Instructions](#quickinst): This is the simplest way to get started - you'll download the relevant prebuilt images for your board and learn how to run binaries on the RISC-V Rocket Core. These instructions require only that you have a compatible board - neither Vivado nor the RISC-V Toolchain are necessary.

2) [How to Push Your Rocket Modifications to the FPGA](#bitstream): These instructions walk through what we believe is the common case - a user wanting to utilize a custom-generated Rocket Core.

3) [Building Everything from Scratch](#fromscratch): Here, we discuss how to build the full stack described above from scratch. It is unlikely that you'll need to use these instructions, unless you are intending to make changes to the configuration of the Zynq ARM Core or `u-boot`.

Finally, the bottom of the README contains a set of [Appendices](#appendices), which document some common operations that we believe are useful or provides more depth on commands described elsewhere in the documentation.


###Table of Contents

+ Overview of System Stack
+ [Quick Instructions](#quickinst)
+ [How to Push Your Rocket Modifications to the FPGA](#bitstream)
  + Setting Up Your Workspace
  + Configuring Rocket Chip
  + Propagating Changes to the Vivado Project
  + Repacking `boot.bin`
+ [Building Everything from Scratch](#fromscratch)
  + Project Setup
  + Generating a Bitstream
  + Building the FSBL
  + Building u-boot for the Zynq ARM Core
  + Creating `boot.bin`
  + Building linux for the ARM PS
  + Building riscv-linux
  + Booting Up and Interacting with the RISC-V Rocket Core
+ [Appendices](#appendices)
  + Connecting to the Board
  + Getting Files On & Off the Board
  + Working with Vivado
  + Changing the Processor's Clockrate
  + Contents of the SD Card



###Overview of System Stack
Our system will allow you to run a RISC-V binary on a rocket core instantiated on a supported Zynq FPGA. This section will outline the stack of all of the parts involved and by proxy, outline the rest of the documentation. Going top-down from the RISC-V binary to the development system:

**Target Application** (RISC-V binary)
 will run on top of whatever kernel the rocket chip is running. Compiled by [riscv-gcc](https://github.com/ucb-bar/riscv-gcc) or [riscv-llvm](https://github.com/ucb-bar/riscv-llvm).

**RISC-V Kernel** ([proxy kernel](https://github.com/ucb-bar/riscv-pk) or [RISC-V Linux](https://github.com/ucb-bar/riscv-linux))
 runs on top of the rocket chip. The proxy kernel is extremely lightweight and designed to be used with a single binary linked against newlib while RISC-V linux is appropriate for everything else.

**Rocket Chip** ([rocket core](https://github.com/ucb-bar/rocket) with L1 instruction and data caches)
 is instantiated on the FPGA. Many of its structures will typically map to various hard blocks including BRAMs and DSP slices. It communicates to the host ARM core on the Zynq via AXI.

**Front-end Server** ([riscv-fesvr](https://github.com/ucb-bar/riscv-fesvr))
 runs on the host ARM core and provides an interface to the rocket chip running on the FPGA (connected via AXI).

**Zynq ARM Core** (actually dual Cortex A9)
 runs Linux and simplifies interfacing with the FPGA.

**FPGA Board** (Zybo, Zedboard, or ZC706)
 contains the Zynq FPGA and several I/O devices. At power on, the contents of the SD card are used to configure the FPGA and boot linux on the ARM core.

**External Communication** (tty over serial on usb or telnet/ssh over ethernet)
 allows the development system to communicate with the FPGA board.

**Development System** (PC with SD card reader)
 generates the images to configure the FPGA.



1) <a name="quickinst"></a> Quick Instructions 
------------------
_Using prebuilt images, run hello world on rocket_

First, enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). From there, run the following to download all of the necessary images:

    $ make fetch-images

Next, insert the SD card on the development system and copy over the images:

    $ make load-sd SD=path_to_mounted_sdcard

Finally, eject the SD card, insert it into the board and power the board on. Connect to the board with an ethernet cable (password is _root_) and run hello world:

    $ ssh root@192.168.1.5
    root@zynq:~# ./fesvr-zedboard pk hello
    hello!

Awesome! You can now run RISC-V binaries on Rocket. If you'd like to boot linux on the Rocket core, see section N (TODO).



2) <a name="bitstream"></a> How to Push Your Rocket Modifications to the FPGA
-------------------------

####Setting Up Your Workspace
If you don't already have a copy of the rocket chip, get a new one:

    $ git clone git@github.com:ucb-bar/rocket-chip.git

Move move fpga-zynq (this repo) to be within rocket-chip:

    $ mv path_to_fpga-zynq/fpga-zynq rocket-chip/

_Note:_ If you like, you can have fpga-zynq and rocket-chip have any relative position as long as you change the symlink `fpga-zynq/rocket-chip` to point to rocket-chip (by default it is .., its parent directory).

Enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). Generate a Vivado project for the board:

    $ make project

####Configuring Rocket Chip

The verilog for the rocket chip is generated by [Chisel](https://chisel.eecs.berkeley.edu) and thus is not intended to be edited by humans. To change the rocket chip, you should modify its chisel code and regenerate the verilog. For information on changing rocket chip, consult its [documentation](https://github.com/ucb-bar/rocket-chip).

####Propagating Changes to the Vivado Project
_Requires a JVM that can run current Scala_

After making changes within `rocket-chip`, to run the rocket chip generator and copy the newly generated verilog back into the board's source, run:

     $ make rocket

The rocket chip will be configured by the configuration named `CHISEL_CONFIG` in the board's `Makefile`. If you wish to use a different configuration, you will need to change your vivado project to be aware of the new verilog source  or regenerate the project. For clarity, configuration names are included in the filename (e.g. _Top.DefaultConfig.v_).

####Repacking `boot.bin`
Once you have changed the design, you will need to generate a new bitstream and that will need to be packaged in `boot.bin`. `boot.bin` also contains the binaries needed for startup (`FSBL.elf` and `u-boot.elf`) but these can be reused. From within the board's directory (_zybo_ in this example), to repack `boot.bin`:

    $ make fpga-images-zybo/boot.bin

If you have modified the verilog for your project but not generated a new bitstream, `make` should generate a new bitstream automatically. To use the new `boot.bin`, copy it to the SD card, insert the SD card into the board, and power on the board.



3) <a name="fromscratch"></a> Building Everything from Scratch
-----------------------
This section describes how to build the entire project from scratch. Most likely, you will not need to perform all of these steps, however we keep them here for reference. Various other sections of this README may selectively refer to these sections. This section assumes that you've just pulled this repository and have sourced the settings file for Vivado 2014.2.

For ease of exposition, we will be describing all of the commands assuming that we are working with the `zybo`. Replacing references to the `zybo` with `zedboard` or `zc706` will allow you to use these instructions for those boards.

From here on, `$REPO` will refer to the location of the `fpga-zynq` repository.

### 1) Project Setup

First, we need to generate a Vivado project from the source files that are present in a particular board's directory. 

	$ cd $REPO/zybo
	$ make project
	
### 2) Generating a Bitstream
	
Next, let's open up the project in the Vivado GUI:

	$ make vivado
	# OR
	$ cd zybo_rocketchip
	$ vivado zybo_rocketchip.xpr

If you wish to make any modifications to the project, you may now do so. Once you've finished, let's move on:

Inside Vivado, select "Open Block Design" followed by "system.bd" in the dropdown. This will open a block diagram for the Zynq PS Configuration and is necessary for correct FSBL generation.

Next, select "Generate Bitstream." Vivado will now step through the usual Synthesis/Implementation steps. Upon completion, select "Open Implemented Design". This is again necessary to properly export the description of our Hardware for the Xilinx SDK to use.

At this point, select File -> Export -> Export Hardware. This will create the following directory:

`$REPO/zybo/zybo_rocketchip/zybo_rocketchip.sdk`

This directory contains a variety of files that provide information about the hardware to the SDK. If you're interested in only the bitstream, you can stop here; the file you want is in:

`$REPO/zybo/zybo_rocketchip/zybo_rocketchip.sdk/rocketchip_wrapper_hw_platform_0/rocketchip_wrapper.bit`

Otherwise, let's continue on to building the FSBL.

### 3) Building the FSBL

This step assumes that you have just generated the bitstream. Inside the Vivado GUI, select "Launch SDK". This will open up the Xilinx SDK preconfigured with the description of our hardware. In order to generate the FSBL, do the following:

1) Select File -> Project -> Application Project

2) In the new window, type "FSBL" as the Project name, and ensure that the rest of the properties are correctly set (disregarding the greyed out "Location" field):

<img src="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/FSBL.png" width="400"/>

3) Select "Next", at which point you should be given a set of options. Select "Zynq FSBL" and "Finish".

4) The SDK will proceed to automatically compile the FSBL. You can see the progress in the Console.

5) Once the build is finished, we need to build u-boot before returning to the SDK in order to create our BOOT.bin.

### 4) Building u-boot for the Zynq ARM Core

Returning to the command line, do the following from the directory corresponding to your board:

	$ make arm-uboot
	
This target performs a variety of commands. It will first pull the u-boot source from the Xilinx repositories (see the submodule in `$REPO/common/u-boot-xlnx`), patch it with the necessary files found in `$REPO/zybo/soft_config/`, compile u-boot, and place the resulting u-boot.elf file in `$REPO/zybo/soft_build/u-boot.elf`. 

### 5) Creating `boot.bin`

At this point, we have built up all of the necessary components to create our `boot.bin` file. Returning to the Xilinx SDK, select Xilinx Tools -> "Create Zynq Boot Image". 

First, you should fill in the "Output BIF file path" with `$REPO/zybo/deliver_output`. If this directory has not already been created, you may go ahead and create it (this is where we will place all of the items that we will ultimately transfer to the SD card). See the below for a sample path. Performing this step will also fill in the "Output path" field, which specifies the location of the `BOOT.bin` file that we desire. 

Next, we will add the individual files that make up `BOOT.bin`. Order is important, so follow these steps exactly:

1) Select "Add" and in the window that opens, click "Browse" and specify the following location:

`$REPO/zybo/zybo_rocketchip/zybo_rocketchip.sdk/FSBL/Debug/FSBL.elf`

Once you have done so select the dropdown next to "Partition type" and select "bootloader". You must perform this step *after* selecting the path, else the SDK will change it back to "datafile", and your `BOOT.bin` will not work.

At the conclusion of this step, the "Add partition" window will look something like:

<img src="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/selectFSBL.png" width="400" />

Click "OK" to return to the previous window.

2) Once more, click "Add". In the new "Add partition" window, click "Browse" and specify the following location:

`$REPO/zybo/zybo_rocketchip/zybo_rocketchip.sdk/rocketchip_wrapper_hw_platform_0/rocketchip_wrapper.bit`

Ensure that "Partition type" is set to datafile and click "OK".

3) Click "Add" a final time. Click "Browse" and this time select our compiled `u-boot.elf`:

`$REPO/zybo/soft_build/u-boot.elf`

Again, ensure that "Partition type" is set to datafile and click "OK".

4) At this point, the window should match the following (click the image to zoom in):

<a href="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/boot_image.png" target="_new"><img src="https://s3-us-west-1.amazonaws.com/riscv.org/fpga-zynq-guide/boot_image.png" width="400" /></a>

Select "Create Image". This will produce a `BOOT.bin` file in the `$REPO/zybo/deliver_output` directory.

If you make modifications to the project in the future, you can avoid having to perform this step manually and
instead may reuse the output.bif file that the SDK generates the first time you use "Create Zynq Boot Image."
Use the following make target to do so:

    $ TODO

### 6) Building linux for the ARM PS

As part of our bootstrapping process, we need to boot linux on the ARM core in the Zynq. We can build this copy of linux like so (again assuming that we are in `$REPO/zybo`):

	$ make arm-linux
	
We additionally need to produce the `devicetree.dtb` file that linux will use to setup peripherals of the ARM core. We can produce this like so:

	$ make arm-dtb
	
At this point, the `$REPO/zybo/deliver_output` directory contains the following files:

* `BOOT.bin` - (the filename is case insensitive, you may see `boot.bin`). This contains the FSBL, the bitstream with Rocket, and u-boot. 
* `uImage` - Linux for the ARM PS
* `devicetree.dtb` - Contains information about the ARM core's peripherals for linux.

The only remaining file that we are missing at this point is `uramdisk.image.gz`, the root filesystem for linux on the ARM Core. You can obtain that like so:

TODO: INFO HERE ABOUT RAMDISK

Take these four files, and place them on the root of the SD card that we will insert into the Zybo. The layout of your SD card should match the following:

	SD_ROOT/
	|-> boot.bin
	|-> devicetree.dtb
	|-> uImage
	|-> uramdisk.image.gz

At this point, you have performed the necessary steps to run binaries on Rocket. See the [TODO: INFO HERE] section for how to do so. If you are interested in running riscv-linux on Rocket, continue on:

### 7) Building riscv-linux

TODO (might want to put this in appendices)

Note: If you are working with the Zybo, you should not build `riscv-linux` from source. The Zybo cannot fit an FPU and thus uses a modified version of the kernel that ignores FPU instructions. Software floating point emulation support is planned but not yet available. The binary for this build can be obtained here. TODO FILL IN LINK

To build [riscv-linux](http://github.com/ucb-bar/riscv-linux) for Rocket, follow the instructions [here](https://github.com/ucb-bar/riscv-tools#linuxman). Upon completing the linked tutorial, you should have two files: `vmlinux` and `root.bin`. You should place them on your SD card in a directory called `riscv`. After doing so, your SD card layout should match the following:


	SD_ROOT/
	|-> riscv/
	    |-> root.bin
	    |-> vmlinux[_nofpu]
	|-> boot.bin
	|-> devicetree.dtb
	|-> uImage
	|-> uramdisk.image.gz

 
### 8) Booting Up and Interacting with the RISC-V Rocket Core

TODO (might want to put this in appendices)


<a name="appendices"></a> Appendices 
------------

###A) Connecting to the Board

####Serial-USB
On the Zybo and Zedboard a single serial-USB cable is needed but on the ZC706 you will also need a USB type A to type B cable (and possibly to install drivers)

    $ screen /dev/tty.usbmodem1411 115200,cs8,-parenb,-cstopb

_Note:_ The numbers following `tty.usbmodem` may vary slightly.

####Ethernet
The board has an IP of 192.168.1.5 and can be accessed by username/password of root/root on telnet and ssh. For example:

    $ ssh root@192.168.1.5

_Note:_ Make sure your development system ethernet interface is configured to be on the 192.168.1.x subnet. The default configuration intends for the board to be directly attached to the development system (single cable). If you want to place the board on a larger network, we recommend changing the root password to something stronger and changing the IP configuration to mesh well with your network.



###B) Getting Files On & Off the Board

####Copying Files over Ethernet
The easiest way to get a file onto the board is to copy it with scp over ethernet:

    $ scp file root@192.168.1.5:~/

_Note:_ Linux is running out of a RAMdisk, so to make a file available after a reboot, copy it to the SD card or modify the RAMdisk.

####Mounting the SD Card on the Board
You can mount the SD card on the board by:

    root@zynq:~# mkdir /sdcard
    root@zynq:~# mount /dev/mmcblk0p1 /sdcard

When you are done, don't forget to unmount it:

    root@zynq:~# umount /sdcard

####Changing the RAMDisk
_Requires: [u-boot](http://www.denx.de/wiki/U-Boot/) and sudo_

The RAMDisk that holds linux (`uramdisk.image.gz`) is a gzipped cpio archive with a u-boot header for the board. To open the RAMdisk:

    $ make ramdisk-open

When changing or adding files, be sure to keep track of owners, groups, and permissions. When you are done, to package it back up:

    $ make ramdisk-close

A useful application of this is to add your SSH public key to `.ssh/authorized_keys` so you can have passwordless login to the board.

_Note:_ Since these ramdisk operations use sudo on files, they may not work on a network mounted filesystem. To get around this limitation, it is easiest to just copy it to a local filesystem when modifying the ramdisk.



###C) Working with Vivado

_Requires: Vivado 2014.2 and its settings64.sh sourced_

First, enter into the directory for your board (current options are `zybo`, `zedboard`, and `zc706`). To generate a Vivado project from scratch:

    $ make project

To generate a bitstream from the command-line:

    $ make bitstream

To launch Vivado in GUI mode:

    $ make vivado


###D) Changing the Processor's Clockrate
You can change the clockrate for the rocket chip by changing `RC_CLK_MULT` and `RC_CLK_DIVIDE` within a board's `src/verilog/clocking.vh`.

Although rarely needed, it is possible to change the input clockrate to the FPGA by changing it within the block design, `src/constrs/base.xdc`, and `ZYNQ_CLK_PERIOD` within `src/verilog/clocking.vh`. This will also require regenerating `FSBL.elf`, the bitstream, and of course `boot.bin`.


###E) Contents of the SD Card
There are 3 major software components used by the ARM core found on the SD card:

1. First Stage Bootloader (FSBL) - This bootloader configures the Zynq processing system based on the block design in the Vivado project. The FSBL will hand-off to `u-boot` once the processing system is setup. We build the FSBL using the Xilinx SDK and hardware information exported from Vivado. (see section N TODO Link)
2. u-boot - This bootloader takes configuration information and prepares the ARM processing system for booting linux. Once configuration is complete, `u-boot` will hand-off execution to the ARM linux kernel. We build `u-boot` directly from the [Xilinx u-boot repository](https://github.com/Xilinx/u-boot-xlnx), with some configuration modifications to support Rocket. (see section N TODO Link)
3. ARM Linux - This is a copy of linux designed to run on the ARM processing system. From within this linux environment, we will be able to run tools (like `fesvr-zedboard`) to interact with the RISC-V Rocket Core. We build directly from the [Xilinx linux repository](https://github.com/Xilinx/linux-xlnx), with a custom device tree file to support Rocket. (see section N TODO Link)



Acknowledgments 
---------------
In addition to those that [contributed](https://github.com/ucb-bar/rocket-chip#contributors) to rocket chip, this repository is based on internal repositories contributed by:
+ Rimas Avizienis
+ Jonathan Bachrach 
+ Scott Beamer
+ Sagar Karandikar
+ Andrew Waterman
