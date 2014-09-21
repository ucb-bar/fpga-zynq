/*
 * (C) Copyright 2012 Xilinx
 * (C) Copyright 2014 Digilent Inc.
 * Modified by Sagar Karandikar for RISC-V Rocket Support + Support for 
 * upstream u-boot-xlnx
 *
 * Originally taken from 
 * https://github.com/Digilent/u-boot-Digilent-Dev/commit/e7773ea2067c224f178a40a97d003ff418f47e12
 *
 * u-boot Configuration for Zynq Development Board - ZYBO
 * See zynq-common.h for Zynq common configs
 *
 * SPDX-License-Identifier:	GPL-2.0+
 */

#ifndef __CONFIG_ZYNQ_ZYBO_H
#define __CONFIG_ZYNQ_ZYBO_H

// originally 512 * ..., but Rocket gets upper half of RAM
#define CONFIG_SYS_SDRAM_SIZE (256 * 1024 * 1024) 

#define CONFIG_ZYNQ_SERIAL_UART1
#define CONFIG_ZYNQ_GEM0
#define CONFIG_ZYNQ_GEM_PHY_ADDR0	0

#define CONFIG_SYS_NO_FLASH

#define CONFIG_ZYNQ_SDHCI0
#define CONFIG_ZYNQ_QSPI
#define CONFIG_ZYNQ_BOOT_FREEBSD

/* Define ZYBO PS Clock Frequency to 50MHz */
#define CONFIG_ZYNQ_PS_CLK_FREQ	50000000UL

#include <configs/zynq-common.h>

/* Overwrite Default environment */
#ifdef CONFIG_EXTRA_ENV_SETTINGS
	#undef CONFIG_EXTRA_ENV_SETTINGS
#endif

#define CONFIG_EXTRA_ENV_SETTINGS \
	"kernel_image=uImage\0"	\
	"ramdisk_image=uramdisk.image.gz\0"	\
	"devicetree_image=devicetree.dtb\0"	\
	"bitstream_image=system.bit.bin\0"	\
	"boot_image=BOOT.bin\0"	\
	"loadbit_addr=0x100000\0"	\
	"loadbootenv_addr=0x2000000\0" \
	"kernel_size=0x500000\0"	\
	"devicetree_size=0x20000\0"	\
	"ramdisk_size=0x5E0000\0"	\
	"boot_size=0xF00000\0"	\
	"fdt_high=0x10000000\0"	\
	"initrd_high=0x10000000\0"	\
	"bootenv=uEnv.txt\0" \
	"loadbootenv=fatload mmc 0 ${loadbootenv_addr} ${bootenv}\0" \
	"importbootenv=echo Importing environment from SD ...; " \
		"env import -t ${loadbootenv_addr} $filesize\0" \
	"mmc_loadbit_fat=echo Loading bitstream from SD/MMC/eMMC to RAM.. && " \
		"mmcinfo && " \
		"fatload mmc 0 ${loadbit_addr} ${bitstream_image} && " \
		"fpga load 0 ${loadbit_addr} ${filesize}\0" \
	"norboot=echo Copying Linux from NOR flash to RAM... && " \
		"cp.b 0xE2100000 0x3000000 ${kernel_size} && " \
		"cp.b 0xE2600000 0x2A00000 ${devicetree_size} && " \
		"echo Copying ramdisk... && " \
		"cp.b 0xE2620000 0x2000000 ${ramdisk_size} && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"qspiboot=echo Copying Linux from QSPI flash to RAM... && " \
		"sf probe 0 0 0 && " \
		"sf read 0x3000000 0x100000 ${kernel_size} && " \
		"sf read 0x2A00000 0x600000 ${devicetree_size} && " \
		"echo Copying ramdisk... && " \
		"sf read 0x2000000 0x620000 ${ramdisk_size} && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"uenvboot=" \
		"if run loadbootenv; then " \
			"echo Loaded environment from ${bootenv}; " \
			"run importbootenv; " \
		"fi; " \
		"if test -n $uenvcmd; then " \
			"echo Running uenvcmd ...; " \
			"run uenvcmd; " \
		"fi\0" \
	"sdboot=if mmcinfo; then " \
			"run uenvboot; " \
			"echo Copying Linux from SD to RAM... && " \
			"fatload mmc 0 0x3000000 ${kernel_image} && " \
			"fatload mmc 0 0x2A00000 ${devicetree_image} && " \
			"fatload mmc 0 0x2000000 ${ramdisk_image} && " \
			"bootm 0x3000000 0x2000000 0x2A00000; " \
		"fi\0" \
	"nandboot=echo Copying Linux from NAND flash to RAM... && " \
		"nand read 0x3000000 0x100000 ${kernel_size} && " \
		"nand read 0x2A00000 0x600000 ${devicetree_size} && " \
		"echo Copying ramdisk... && " \
		"nand read 0x2000000 0x620000 ${ramdisk_size} && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"jtagboot=echo TFTPing Linux to RAM... && " \
		"tftp 0x3000000 ${kernel_image} && " \
		"tftp 0x2A00000 ${devicetree_image} && " \
		"tftp 0x2000000 ${ramdisk_image} && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"rsa_norboot=echo Copying Image from NOR flash to RAM... && " \
		"cp.b 0xE2100000 0x100000 ${boot_size} && " \
		"zynqrsa 0x100000 && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"rsa_nandboot=echo Copying Image from NAND flash to RAM... && " \
		"nand read 0x100000 0x0 ${boot_size} && " \
		"zynqrsa 0x100000 && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"rsa_qspiboot=echo Copying Image from QSPI flash to RAM... && " \
		"sf probe 0 0 0 && " \
		"sf read 0x100000 0x0 ${boot_size} && " \
		"zynqrsa 0x100000 && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"rsa_sdboot=echo Copying Image from SD to RAM... && " \
		"fatload mmc 0 0x100000 ${boot_image} && " \
		"zynqrsa 0x100000 && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0" \
	"rsa_jtagboot=echo TFTPing Image to RAM... && " \
		"tftp 0x100000 ${boot_image} && " \
		"zynqrsa 0x100000 && " \
		"bootm 0x3000000 0x2000000 0x2A00000\0"

#endif /* __CONFIG_ZYNQ_ZYBO_H */
