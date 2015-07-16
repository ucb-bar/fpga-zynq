
################################################################
# This is a generated script based on design: system
#
# Though there are limitations about the generated script,
# the main purpose of this utility is to make learning
# IP Integrator Tcl commands easier.
################################################################

################################################################
# Check if script is running in correct Vivado version.
################################################################
set scripts_vivado_version 2015.2
set current_vivado_version [version -short]

if { [string first $scripts_vivado_version $current_vivado_version] == -1 } {
   puts ""
   puts "ERROR: This script was generated using Vivado <$scripts_vivado_version> and is being run in <$current_vivado_version> of Vivado. Please run the script in Vivado <$scripts_vivado_version> then open the design in Vivado <$current_vivado_version>. Upgrade the design by running \"Tools => Report => Report IP Status...\", then run write_bd_tcl to create an updated script."

   return 1
}

################################################################
# START
################################################################

# To test this script, run the following commands from Vivado Tcl console:
# source system_script.tcl

# If you do not already have a project created,
# you can create a project using the following command:
#    create_project project_1 myproj -part xc7z045ffg900-2
#    set_property BOARD_PART xilinx.com:zc706:part0:1.0 [current_project]

# CHECKING IF PROJECT EXISTS
if { [get_projects -quiet] eq "" } {
   puts "ERROR: Please open or create a project!"
   return 1
}



# CHANGE DESIGN NAME HERE
set design_name system

# If you do not already have an existing IP Integrator design open,
# you can create a design using the following command:
#    create_bd_design $design_name

# Creating design if needed
set errMsg ""
set nRet 0

set cur_design [current_bd_design -quiet]
set list_cells [get_bd_cells -quiet]

if { ${design_name} eq "" } {
   # USE CASES:
   #    1) Design_name not set

   set errMsg "ERROR: Please set the variable <design_name> to a non-empty value."
   set nRet 1

} elseif { ${cur_design} ne "" && ${list_cells} eq "" } {
   # USE CASES:
   #    2): Current design opened AND is empty AND names same.
   #    3): Current design opened AND is empty AND names diff; design_name NOT in project.
   #    4): Current design opened AND is empty AND names diff; design_name exists in project.

   if { $cur_design ne $design_name } {
      puts "INFO: Changing value of <design_name> from <$design_name> to <$cur_design> since current design is empty."
      set design_name [get_property NAME $cur_design]
   }
   puts "INFO: Constructing design in IPI design <$cur_design>..."

} elseif { ${cur_design} ne "" && $list_cells ne "" && $cur_design eq $design_name } {
   # USE CASES:
   #    5) Current design opened AND has components AND same names.

   set errMsg "ERROR: Design <$design_name> already exists in your project, please set the variable <design_name> to another value."
   set nRet 1
} elseif { [get_files -quiet ${design_name}.bd] ne "" } {
   # USE CASES: 
   #    6) Current opened design, has components, but diff names, design_name exists in project.
   #    7) No opened design, design_name exists in project.

   set errMsg "ERROR: Design <$design_name> already exists in your project, please set the variable <design_name> to another value."
   set nRet 2

} else {
   # USE CASES:
   #    8) No opened design, design_name not in project.
   #    9) Current opened design, has components, but diff names, design_name not in project.

   puts "INFO: Currently there is no design <$design_name> in project, so creating one..."

   create_bd_design $design_name

   puts "INFO: Making design <$design_name> as current_bd_design."
   current_bd_design $design_name

}

puts "INFO: Currently the variable <design_name> is equal to \"$design_name\"."

if { $nRet != 0 } {
   puts $errMsg
   return $nRet
}

##################################################################
# DESIGN PROCs
##################################################################



# Procedure to create entire design; Provide argument to make
# procedure reusable. If parentCell is "", will use root.
proc create_root_design { parentCell } {

  if { $parentCell eq "" } {
     set parentCell [get_bd_cells /]
  }

  # Get object for parentCell
  set parentObj [get_bd_cells $parentCell]
  if { $parentObj == "" } {
     puts "ERROR: Unable to find parent cell <$parentCell>!"
     return
  }

  # Make sure parentObj is hier blk
  set parentType [get_property TYPE $parentObj]
  if { $parentType ne "hier" } {
     puts "ERROR: Parent <$parentObj> has TYPE = <$parentType>. Expected to be <hier>."
     return
  }

  # Save current instance; Restore later
  set oldCurInst [current_bd_instance .]

  # Set parent object as current
  current_bd_instance $parentObj


  # Create interface ports
  set DDR [ create_bd_intf_port -mode Master -vlnv xilinx.com:interface:ddrx_rtl:1.0 DDR ]
  set FIXED_IO [ create_bd_intf_port -mode Master -vlnv xilinx.com:display_processing_system7:fixedio_rtl:1.0 FIXED_IO ]
  set M_AXI [ create_bd_intf_port -mode Master -vlnv xilinx.com:interface:aximm_rtl:1.0 M_AXI ]
  set_property -dict [ list CONFIG.ADDR_WIDTH {32} CONFIG.DATA_WIDTH {32} CONFIG.FREQ_HZ {50000000} CONFIG.PROTOCOL {AXI4}  ] $M_AXI
  set S_AXI [ create_bd_intf_port -mode Slave -vlnv xilinx.com:interface:aximm_rtl:1.0 S_AXI ]
  set_property -dict [ list CONFIG.ADDR_WIDTH {32} CONFIG.ARUSER_WIDTH {0} CONFIG.AWUSER_WIDTH {0} CONFIG.BUSER_WIDTH {0} CONFIG.CLK_DOMAIN {} CONFIG.DATA_WIDTH {64} CONFIG.FREQ_HZ {50000000} CONFIG.ID_WIDTH {6} CONFIG.MAX_BURST_LENGTH {16} CONFIG.NUM_READ_OUTSTANDING {1} CONFIG.NUM_WRITE_OUTSTANDING {1} CONFIG.PHASE {0.000} CONFIG.PROTOCOL {AXI4} CONFIG.READ_WRITE_MODE {READ_WRITE} CONFIG.RUSER_WIDTH {0} CONFIG.SUPPORTS_NARROW_BURST {1} CONFIG.WUSER_WIDTH {0}  ] $S_AXI

  # Create ports
  set FCLK_RESET0_N [ create_bd_port -dir O -type rst FCLK_RESET0_N ]
  set ext_clk_in [ create_bd_port -dir I -type clk ext_clk_in ]
  set_property -dict [ list CONFIG.ASSOCIATED_BUSIF {S_AXI:M_AXI} CONFIG.FREQ_HZ {50000000}  ] $ext_clk_in

  # Create instance: axi_interconnect_0, and set properties
  set axi_interconnect_0 [ create_bd_cell -type ip -vlnv xilinx.com:ip:axi_interconnect:2.1 axi_interconnect_0 ]
  set_property -dict [ list CONFIG.NUM_MI {1}  ] $axi_interconnect_0

  # Create instance: axi_interconnect_1, and set properties
  set axi_interconnect_1 [ create_bd_cell -type ip -vlnv xilinx.com:ip:axi_interconnect:2.1 axi_interconnect_1 ]
  set_property -dict [ list CONFIG.NUM_MI {1}  ] $axi_interconnect_1

  # Create instance: proc_sys_reset_0, and set properties
  set proc_sys_reset_0 [ create_bd_cell -type ip -vlnv xilinx.com:ip:proc_sys_reset:5.0 proc_sys_reset_0 ]
  set_property -dict [ list CONFIG.C_AUX_RESET_HIGH {0}  ] $proc_sys_reset_0

  # Create instance: processing_system7_0, and set properties
  set processing_system7_0 [ create_bd_cell -type ip -vlnv xilinx.com:ip:processing_system7:5.5 processing_system7_0 ]
  set_property -dict [ list CONFIG.PCW_ACT_ENET0_PERIPHERAL_FREQMHZ {25.000000} \
CONFIG.PCW_ACT_FPGA0_PERIPHERAL_FREQMHZ {100.000000} CONFIG.PCW_ACT_QSPI_PERIPHERAL_FREQMHZ {200.000000} \
CONFIG.PCW_ACT_SDIO_PERIPHERAL_FREQMHZ {50.000000} CONFIG.PCW_ACT_UART_PERIPHERAL_FREQMHZ {50.000000} \
CONFIG.PCW_APU_PERIPHERAL_FREQMHZ {667.000000} CONFIG.PCW_CLK0_FREQ {100000000} \
CONFIG.PCW_DCI_PERIPHERAL_CLKSRC {1} CONFIG.PCW_DDR_RAM_HIGHADDR {0x3FFFFFFF} \
CONFIG.PCW_ENET0_ENET0_IO {MIO 16 .. 27} CONFIG.PCW_ENET0_GRP_MDIO_ENABLE {1} \
CONFIG.PCW_ENET0_GRP_MDIO_IO {MIO 52 .. 53} CONFIG.PCW_ENET0_PERIPHERAL_ENABLE {1} \
CONFIG.PCW_ENET0_PERIPHERAL_FREQMHZ {100 Mbps} CONFIG.PCW_EN_ENET0 {1} \
CONFIG.PCW_EN_QSPI {1} CONFIG.PCW_EN_SDIO0 {1} \
CONFIG.PCW_EN_UART1 {1} CONFIG.PCW_EN_USB0 {1} \
CONFIG.PCW_FPGA0_PERIPHERAL_FREQMHZ {100} CONFIG.PCW_GPIO_MIO_GPIO_ENABLE {0} \
CONFIG.PCW_I2C0_PERIPHERAL_ENABLE {0} CONFIG.PCW_MIO_0_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_0_PULLUP {enabled} CONFIG.PCW_MIO_0_SLEW {slow} \
CONFIG.PCW_MIO_10_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_10_PULLUP {enabled} \
CONFIG.PCW_MIO_10_SLEW {slow} CONFIG.PCW_MIO_11_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_11_PULLUP {enabled} CONFIG.PCW_MIO_11_SLEW {slow} \
CONFIG.PCW_MIO_12_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_12_PULLUP {enabled} \
CONFIG.PCW_MIO_12_SLEW {slow} CONFIG.PCW_MIO_13_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_13_PULLUP {enabled} CONFIG.PCW_MIO_13_SLEW {slow} \
CONFIG.PCW_MIO_14_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_14_PULLUP {enabled} \
CONFIG.PCW_MIO_14_SLEW {slow} CONFIG.PCW_MIO_15_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_15_PULLUP {enabled} CONFIG.PCW_MIO_15_SLEW {slow} \
CONFIG.PCW_MIO_16_IOTYPE {HSTL 1.8V} CONFIG.PCW_MIO_16_PULLUP {disabled} \
CONFIG.PCW_MIO_16_SLEW {slow} CONFIG.PCW_MIO_17_IOTYPE {HSTL 1.8V} \
CONFIG.PCW_MIO_17_PULLUP {disabled} CONFIG.PCW_MIO_17_SLEW {slow} \
CONFIG.PCW_MIO_18_IOTYPE {HSTL 1.8V} CONFIG.PCW_MIO_18_PULLUP {disabled} \
CONFIG.PCW_MIO_18_SLEW {slow} CONFIG.PCW_MIO_19_IOTYPE {HSTL 1.8V} \
CONFIG.PCW_MIO_19_PULLUP {disabled} CONFIG.PCW_MIO_19_SLEW {slow} \
CONFIG.PCW_MIO_1_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_1_PULLUP {enabled} \
CONFIG.PCW_MIO_1_SLEW {slow} CONFIG.PCW_MIO_20_IOTYPE {HSTL 1.8V} \
CONFIG.PCW_MIO_20_PULLUP {disabled} CONFIG.PCW_MIO_20_SLEW {slow} \
CONFIG.PCW_MIO_21_IOTYPE {HSTL 1.8V} CONFIG.PCW_MIO_21_PULLUP {disabled} \
CONFIG.PCW_MIO_21_SLEW {slow} CONFIG.PCW_MIO_22_IOTYPE {HSTL 1.8V} \
CONFIG.PCW_MIO_22_PULLUP {disabled} CONFIG.PCW_MIO_22_SLEW {slow} \
CONFIG.PCW_MIO_23_IOTYPE {HSTL 1.8V} CONFIG.PCW_MIO_23_PULLUP {disabled} \
CONFIG.PCW_MIO_23_SLEW {slow} CONFIG.PCW_MIO_24_IOTYPE {HSTL 1.8V} \
CONFIG.PCW_MIO_24_PULLUP {disabled} CONFIG.PCW_MIO_24_SLEW {slow} \
CONFIG.PCW_MIO_25_IOTYPE {HSTL 1.8V} CONFIG.PCW_MIO_25_PULLUP {disabled} \
CONFIG.PCW_MIO_25_SLEW {slow} CONFIG.PCW_MIO_26_IOTYPE {HSTL 1.8V} \
CONFIG.PCW_MIO_26_PULLUP {disabled} CONFIG.PCW_MIO_26_SLEW {slow} \
CONFIG.PCW_MIO_27_IOTYPE {HSTL 1.8V} CONFIG.PCW_MIO_27_PULLUP {disabled} \
CONFIG.PCW_MIO_27_SLEW {slow} CONFIG.PCW_MIO_28_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_28_PULLUP {disabled} CONFIG.PCW_MIO_28_SLEW {slow} \
CONFIG.PCW_MIO_29_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_29_PULLUP {disabled} \
CONFIG.PCW_MIO_29_SLEW {slow} CONFIG.PCW_MIO_2_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_2_SLEW {slow} CONFIG.PCW_MIO_30_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_30_PULLUP {disabled} CONFIG.PCW_MIO_30_SLEW {slow} \
CONFIG.PCW_MIO_31_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_31_PULLUP {disabled} \
CONFIG.PCW_MIO_31_SLEW {slow} CONFIG.PCW_MIO_32_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_32_PULLUP {disabled} CONFIG.PCW_MIO_32_SLEW {slow} \
CONFIG.PCW_MIO_33_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_33_PULLUP {disabled} \
CONFIG.PCW_MIO_33_SLEW {slow} CONFIG.PCW_MIO_34_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_34_PULLUP {disabled} CONFIG.PCW_MIO_34_SLEW {slow} \
CONFIG.PCW_MIO_35_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_35_PULLUP {disabled} \
CONFIG.PCW_MIO_35_SLEW {slow} CONFIG.PCW_MIO_36_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_36_PULLUP {disabled} CONFIG.PCW_MIO_36_SLEW {slow} \
CONFIG.PCW_MIO_37_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_37_PULLUP {disabled} \
CONFIG.PCW_MIO_37_SLEW {slow} CONFIG.PCW_MIO_38_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_38_PULLUP {disabled} CONFIG.PCW_MIO_38_SLEW {slow} \
CONFIG.PCW_MIO_39_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_39_PULLUP {disabled} \
CONFIG.PCW_MIO_39_SLEW {slow} CONFIG.PCW_MIO_3_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_3_SLEW {slow} CONFIG.PCW_MIO_40_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_40_PULLUP {disabled} CONFIG.PCW_MIO_40_SLEW {slow} \
CONFIG.PCW_MIO_41_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_41_PULLUP {disabled} \
CONFIG.PCW_MIO_41_SLEW {slow} CONFIG.PCW_MIO_42_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_42_PULLUP {disabled} CONFIG.PCW_MIO_42_SLEW {slow} \
CONFIG.PCW_MIO_43_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_43_PULLUP {disabled} \
CONFIG.PCW_MIO_43_SLEW {slow} CONFIG.PCW_MIO_44_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_44_PULLUP {disabled} CONFIG.PCW_MIO_44_SLEW {slow} \
CONFIG.PCW_MIO_45_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_45_PULLUP {disabled} \
CONFIG.PCW_MIO_45_SLEW {slow} CONFIG.PCW_MIO_48_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_48_PULLUP {disabled} CONFIG.PCW_MIO_48_SLEW {slow} \
CONFIG.PCW_MIO_49_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_49_PULLUP {disabled} \
CONFIG.PCW_MIO_49_SLEW {slow} CONFIG.PCW_MIO_4_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_4_SLEW {slow} CONFIG.PCW_MIO_52_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_52_PULLUP {disabled} CONFIG.PCW_MIO_52_SLEW {slow} \
CONFIG.PCW_MIO_53_IOTYPE {LVCMOS 1.8V} CONFIG.PCW_MIO_53_PULLUP {disabled} \
CONFIG.PCW_MIO_53_SLEW {slow} CONFIG.PCW_MIO_5_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_5_SLEW {slow} CONFIG.PCW_MIO_6_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_6_SLEW {slow} CONFIG.PCW_MIO_8_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_8_SLEW {slow} CONFIG.PCW_MIO_9_IOTYPE {LVCMOS 1.8V} \
CONFIG.PCW_MIO_9_PULLUP {enabled} CONFIG.PCW_MIO_9_SLEW {slow} \
CONFIG.PCW_MIO_TREE_PERIPHERALS {Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#unassigned#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#SD 0#SD 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#Enet 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#USB 0#SD 0#SD 0#SD 0#SD 0#SD 0#SD 0#unassigned#unassigned#UART 1#UART 1#unassigned#unassigned#Enet 0#Enet 0} CONFIG.PCW_MIO_TREE_SIGNALS {qspi1_ss_b#qspi0_ss_b#qspi0_io[0]#qspi0_io[1]#qspi0_io[2]#qspi0_io[3]#qspi0_sclk#unassigned#qspi_fbclk#qspi1_sclk#qspi1_io[0]#qspi1_io[1]#qspi1_io[2]#qspi1_io[3]#cd#wp#tx_clk#txd[0]#txd[1]#txd[2]#txd[3]#tx_ctl#rx_clk#rxd[0]#rxd[1]#rxd[2]#rxd[3]#rx_ctl#data[4]#dir#stp#nxt#data[0]#data[1]#data[2]#data[3]#clk#data[5]#data[6]#data[7]#clk#cmd#data[0]#data[1]#data[2]#data[3]#unassigned#unassigned#tx#rx#unassigned#unassigned#mdc#mdio} \
CONFIG.PCW_M_AXI_GP0_FREQMHZ {50} CONFIG.PCW_PCAP_PERIPHERAL_CLKSRC {1} \
CONFIG.PCW_PERIPHERAL_BOARD_PRESET {part0} CONFIG.PCW_PRESET_BANK0_VOLTAGE {LVCMOS 1.8V} \
CONFIG.PCW_PRESET_BANK1_VOLTAGE {LVCMOS 1.8V} CONFIG.PCW_QSPI_GRP_FBCLK_ENABLE {1} \
CONFIG.PCW_QSPI_GRP_FBCLK_IO {MIO 8} CONFIG.PCW_QSPI_GRP_IO1_ENABLE {1} \
CONFIG.PCW_QSPI_GRP_IO1_IO {MIO 0 9 .. 13} CONFIG.PCW_QSPI_PERIPHERAL_ENABLE {1} \
CONFIG.PCW_QSPI_QSPI_IO {MIO 1 .. 6} CONFIG.PCW_SD0_GRP_CD_ENABLE {1} \
CONFIG.PCW_SD0_GRP_CD_IO {MIO 14} CONFIG.PCW_SD0_GRP_WP_ENABLE {1} \
CONFIG.PCW_SD0_GRP_WP_IO {MIO 15} CONFIG.PCW_SD0_PERIPHERAL_ENABLE {1} \
CONFIG.PCW_SD0_SD0_IO {MIO 40 .. 45} CONFIG.PCW_SDIO_PERIPHERAL_FREQMHZ {50} \
CONFIG.PCW_SDIO_PERIPHERAL_VALID {1} CONFIG.PCW_S_AXI_HP0_FREQMHZ {50} \
CONFIG.PCW_TTC0_PERIPHERAL_ENABLE {0} CONFIG.PCW_UART1_PERIPHERAL_ENABLE {1} \
CONFIG.PCW_UART1_UART1_IO {MIO 48 .. 49} CONFIG.PCW_UART_PERIPHERAL_FREQMHZ {50} \
CONFIG.PCW_UIPARAM_DDR_BOARD_DELAY0 {0.521} CONFIG.PCW_UIPARAM_DDR_BOARD_DELAY1 {0.636} \
CONFIG.PCW_UIPARAM_DDR_BOARD_DELAY2 {0.54} CONFIG.PCW_UIPARAM_DDR_BOARD_DELAY3 {0.621} \
CONFIG.PCW_UIPARAM_DDR_CWL {5.000000} CONFIG.PCW_UIPARAM_DDR_DEVICE_CAPACITY {2048 MBits} \
CONFIG.PCW_UIPARAM_DDR_DQS_TO_CLK_DELAY_0 {0.226} CONFIG.PCW_UIPARAM_DDR_DQS_TO_CLK_DELAY_1 {0.278} \
CONFIG.PCW_UIPARAM_DDR_DQS_TO_CLK_DELAY_2 {0.184} CONFIG.PCW_UIPARAM_DDR_DQS_TO_CLK_DELAY_3 {0.309} \
CONFIG.PCW_UIPARAM_DDR_FREQ_MHZ {533.333313} CONFIG.PCW_UIPARAM_DDR_PARTNO {Custom} \
CONFIG.PCW_UIPARAM_DDR_ROW_ADDR_COUNT {15} CONFIG.PCW_UIPARAM_DDR_TRAIN_WRITE_LEVEL {1} \
CONFIG.PCW_UIPARAM_DDR_T_FAW {25} CONFIG.PCW_UIPARAM_DDR_T_RAS_MIN {36.0} \
CONFIG.PCW_UIPARAM_DDR_T_RC {49.5} CONFIG.PCW_UIPARAM_DDR_USE_INTERNAL_VREF {1} \
CONFIG.PCW_USB0_PERIPHERAL_ENABLE {1} CONFIG.PCW_USB0_USB0_IO {MIO 28 .. 39} \
CONFIG.PCW_USE_S_AXI_HP0 {1} CONFIG.preset {ZC706} \
 ] $processing_system7_0

  # Create interface connections
  connect_bd_intf_net -intf_net S_AXI_1 [get_bd_intf_ports S_AXI] [get_bd_intf_pins axi_interconnect_1/S00_AXI]
  connect_bd_intf_net -intf_net axi_interconnect_0_M00_AXI [get_bd_intf_ports M_AXI] [get_bd_intf_pins axi_interconnect_0/M00_AXI]
  connect_bd_intf_net -intf_net axi_interconnect_1_M00_AXI [get_bd_intf_pins axi_interconnect_1/M00_AXI] [get_bd_intf_pins processing_system7_0/S_AXI_HP0]
  connect_bd_intf_net -intf_net processing_system7_0_DDR [get_bd_intf_ports DDR] [get_bd_intf_pins processing_system7_0/DDR]
  connect_bd_intf_net -intf_net processing_system7_0_FIXED_IO [get_bd_intf_ports FIXED_IO] [get_bd_intf_pins processing_system7_0/FIXED_IO]
  connect_bd_intf_net -intf_net processing_system7_0_M_AXI_GP0 [get_bd_intf_pins axi_interconnect_0/S00_AXI] [get_bd_intf_pins processing_system7_0/M_AXI_GP0]

  # Create port connections
  connect_bd_net -net ext_clk_in_1 [get_bd_ports ext_clk_in] [get_bd_pins axi_interconnect_0/ACLK] [get_bd_pins axi_interconnect_0/M00_ACLK] [get_bd_pins axi_interconnect_0/S00_ACLK] [get_bd_pins axi_interconnect_1/ACLK] [get_bd_pins axi_interconnect_1/M00_ACLK] [get_bd_pins axi_interconnect_1/S00_ACLK] [get_bd_pins proc_sys_reset_0/slowest_sync_clk] [get_bd_pins processing_system7_0/M_AXI_GP0_ACLK] [get_bd_pins processing_system7_0/S_AXI_HP0_ACLK]
  connect_bd_net -net proc_sys_reset_0_interconnect_aresetn [get_bd_pins axi_interconnect_0/ARESETN] [get_bd_pins axi_interconnect_1/ARESETN] [get_bd_pins proc_sys_reset_0/interconnect_aresetn]
  connect_bd_net -net proc_sys_reset_0_peripheral_aresetn [get_bd_pins axi_interconnect_0/M00_ARESETN] [get_bd_pins axi_interconnect_0/S00_ARESETN] [get_bd_pins axi_interconnect_1/M00_ARESETN] [get_bd_pins axi_interconnect_1/S00_ARESETN] [get_bd_pins proc_sys_reset_0/peripheral_aresetn]
  connect_bd_net -net processing_system7_0_FCLK_RESET0_N [get_bd_ports FCLK_RESET0_N] [get_bd_pins proc_sys_reset_0/ext_reset_in] [get_bd_pins processing_system7_0/FCLK_RESET0_N]

  # Create address segments
  create_bd_addr_seg -range 0x1000 -offset 0x43C00000 [get_bd_addr_spaces processing_system7_0/Data] [get_bd_addr_segs M_AXI/Reg] SEG_system_Reg
  create_bd_addr_seg -range 0x20000000 -offset 0x0 [get_bd_addr_spaces S_AXI] [get_bd_addr_segs processing_system7_0/S_AXI_HP0/HP0_DDR_LOWOCM] SEG_processing_system7_0_HP0_DDR_LOWOCM
  

  # Restore current instance
  current_bd_instance $oldCurInst

  save_bd_design
}
# End of create_root_design()


##################################################################
# MAIN FLOW
##################################################################

create_root_design ""


