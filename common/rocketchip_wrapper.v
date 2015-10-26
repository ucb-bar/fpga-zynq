`timescale 1 ps / 1 ps
`include "clocking.vh"

module rocketchip_wrapper
   (DDR_addr,
    DDR_ba,
    DDR_cas_n,
    DDR_ck_n,
    DDR_ck_p,
    DDR_cke,
    DDR_cs_n,
    DDR_dm,
    DDR_dq,
    DDR_dqs_n,
    DDR_dqs_p,
    DDR_odt,
    DDR_ras_n,
    DDR_reset_n,
    DDR_we_n,
    FIXED_IO_ddr_vrn,
    FIXED_IO_ddr_vrp,
    FIXED_IO_mio,
    FIXED_IO_ps_clk,
    FIXED_IO_ps_porb,
    FIXED_IO_ps_srstb,
`ifndef differential_clock
    clk);
`else
    SYSCLK_P,
    SYSCLK_N);
`endif

  inout [14:0]DDR_addr;
  inout [2:0]DDR_ba;
  inout DDR_cas_n;
  inout DDR_ck_n;
  inout DDR_ck_p;
  inout DDR_cke;
  inout DDR_cs_n;
  inout [3:0]DDR_dm;
  inout [31:0]DDR_dq;
  inout [3:0]DDR_dqs_n;
  inout [3:0]DDR_dqs_p;
  inout DDR_odt;
  inout DDR_ras_n;
  inout DDR_reset_n;
  inout DDR_we_n;

  inout FIXED_IO_ddr_vrn;
  inout FIXED_IO_ddr_vrp;
  inout [53:0]FIXED_IO_mio;
  inout FIXED_IO_ps_clk;
  inout FIXED_IO_ps_porb;
  inout FIXED_IO_ps_srstb;

`ifndef differential_clock
  input clk;
`else
  input SYSCLK_P;
  input SYSCLK_N;
`endif

  wire FCLK_RESET0_N;
  
  wire [31:0]M_AXI_araddr;
  wire [1:0]M_AXI_arburst;
  wire [7:0]M_AXI_arlen;
  wire M_AXI_arready;
  wire [2:0]M_AXI_arsize;
  wire M_AXI_arvalid;
  wire [31:0]M_AXI_awaddr;
  wire [1:0]M_AXI_awburst;
  wire [7:0]M_AXI_awlen;
  wire [3:0]M_AXI_wstrb;
  wire M_AXI_awready;
  wire [2:0]M_AXI_awsize;
  wire M_AXI_awvalid;
  wire M_AXI_bready;
  wire M_AXI_bvalid;
  wire [31:0]M_AXI_rdata;
  wire M_AXI_rlast;
  wire M_AXI_rready;
  wire M_AXI_rvalid;
  wire [31:0]M_AXI_wdata;
  wire M_AXI_wlast;
  wire M_AXI_wready;
  wire M_AXI_wvalid;
  wire [11:0] M_AXI_arid, M_AXI_awid; // outputs from ARM core
  wire [11:0] M_AXI_bid, M_AXI_rid;   // inputs to ARM core

  wire [4:0] raddr, waddr;
  reg  [4:0] raddr_r, waddr_r;
  reg [11:0] arid_r, awid_r;
  reg [15:0] host_out_bits_r;

  wire host_in_fifo_full, host_in_fifo_empty, host_in_fifo_rden, host_in_fifo_wren;
  wire host_out_fifo_full, host_out_fifo_empty, host_out_fifo_wren, host_out_fifo_rden;
  wire [31:0] host_in_fifo_dout, host_out_fifo_dout;
  wire [4:0] host_out_fifo_count;
  reg host_out_count, host_in_count;

  wire S_AXI_arready;
  wire S_AXI_arvalid;
  wire [31:0] S_AXI_araddr;
  wire [5:0]  S_AXI_arid;
  wire [2:0]  S_AXI_arsize;
  wire [7:0]  S_AXI_arlen;
  wire [1:0]  S_AXI_arburst;
  wire S_AXI_arlock;
  wire [3:0]  S_AXI_arcache;
  wire [2:0]  S_AXI_arprot;
  wire [3:0]  S_AXI_arqos;
  wire [3:0]  S_AXI_arregion;

  wire S_AXI_awready;
  wire S_AXI_awvalid;
  wire [31:0] S_AXI_awaddr;
  wire [5:0]  S_AXI_awid;
  wire [2:0]  S_AXI_awsize;
  wire [7:0]  S_AXI_awlen;
  wire [1:0]  S_AXI_awburst;
  wire S_AXI_awlock;
  wire [3:0]  S_AXI_awcache;
  wire [2:0]  S_AXI_awprot;
  wire [3:0]  S_AXI_awqos;
  wire [3:0]  S_AXI_awregion;

  wire S_AXI_wready;
  wire S_AXI_wvalid;
  wire [7:0]  S_AXI_wstrb;
  wire [63:0] S_AXI_wdata;
  wire S_AXI_wlast;

  wire S_AXI_bready;
  wire S_AXI_bvalid;
  wire [1:0] S_AXI_bresp;
  wire [5:0] S_AXI_bid;

  wire S_AXI_rready;
  wire S_AXI_rvalid;
  wire [1:0]  S_AXI_rresp;
  wire [5:0]  S_AXI_rid;
  wire [63:0] S_AXI_rdata;
  wire S_AXI_rlast;

  wire reset, reset_cpu;

  wire host_in_valid, host_in_ready, host_out_ready, host_out_valid;
  wire [15:0] host_in_bits, host_out_bits;
  wire host_clk;
  wire gclk_i, gclk_fbout, host_clk_i, mmcm_locked;

  system system_i
       (.DDR_addr(DDR_addr),
        .DDR_ba(DDR_ba),
        .DDR_cas_n(DDR_cas_n),
        .DDR_ck_n(DDR_ck_n),
        .DDR_ck_p(DDR_ck_p),
        .DDR_cke(DDR_cke),
        .DDR_cs_n(DDR_cs_n),
        .DDR_dm(DDR_dm),
        .DDR_dq(DDR_dq),
        .DDR_dqs_n(DDR_dqs_n),
        .DDR_dqs_p(DDR_dqs_p),
        .DDR_odt(DDR_odt),
        .DDR_ras_n(DDR_ras_n),
        .DDR_reset_n(DDR_reset_n),
        .DDR_we_n(DDR_we_n),
        .FCLK_RESET0_N(FCLK_RESET0_N),
        .FIXED_IO_ddr_vrn(FIXED_IO_ddr_vrn),
        .FIXED_IO_ddr_vrp(FIXED_IO_ddr_vrp),
        .FIXED_IO_mio(FIXED_IO_mio),
        .FIXED_IO_ps_clk(FIXED_IO_ps_clk),
        .FIXED_IO_ps_porb(FIXED_IO_ps_porb),
        .FIXED_IO_ps_srstb(FIXED_IO_ps_srstb),
        // master AXI interface (zynq = master, fpga = slave)
        .M_AXI_araddr(M_AXI_araddr),
        .M_AXI_arburst(M_AXI_arburst), // burst type
        .M_AXI_arcache(),
        .M_AXI_arid(M_AXI_arid),
        .M_AXI_arlen(M_AXI_arlen), // burst length (#transfers)
        .M_AXI_arlock(),
        .M_AXI_arprot(),
        .M_AXI_arqos(),
        .M_AXI_arready(M_AXI_arready),
        .M_AXI_arregion(),
        .M_AXI_arsize(M_AXI_arsize), // burst size (bits/transfer)
        .M_AXI_arvalid(M_AXI_arvalid),
        //
        .M_AXI_awaddr(M_AXI_awaddr),
        .M_AXI_awburst(M_AXI_awburst),
        .M_AXI_awcache(),
        .M_AXI_awid(M_AXI_awid),
        .M_AXI_awlen(M_AXI_awlen),
        .M_AXI_awlock(),
        .M_AXI_awprot(),
        .M_AXI_awqos(),
        .M_AXI_awready(M_AXI_awready),
        .M_AXI_awregion(),
        .M_AXI_awsize(M_AXI_awsize),
        .M_AXI_awvalid(M_AXI_awvalid),
        //
        .M_AXI_bid(M_AXI_bid),
        .M_AXI_bready(M_AXI_bready),
        .M_AXI_bresp(2'b00),
        .M_AXI_bvalid(M_AXI_bvalid),
        //
        .M_AXI_rdata(M_AXI_rdata),
        .M_AXI_rid(M_AXI_rid),
        .M_AXI_rlast(M_AXI_rlast),
        .M_AXI_rready(M_AXI_rready),
        .M_AXI_rresp(),
        .M_AXI_rvalid(M_AXI_rvalid),
        //
        .M_AXI_wdata(M_AXI_wdata),
        .M_AXI_wlast(M_AXI_wlast),
        .M_AXI_wready(M_AXI_wready),
        .M_AXI_wstrb(M_AXI_wstrb),
        .M_AXI_wvalid(M_AXI_wvalid),

        // slave AXI interface (fpga = master, zynq = slave) 
        // connected directly to DDR controller to handle test chip mem
        .S_AXI_araddr(S_AXI_araddr),
        .S_AXI_arburst(S_AXI_arburst),
        .S_AXI_arcache(S_AXI_arcache),
        .S_AXI_arid(S_AXI_arid),
        .S_AXI_arlen(S_AXI_arlen),
        .S_AXI_arlock(S_AXI_arlock),
        .S_AXI_arprot(S_AXI_arprot),
        .S_AXI_arqos(S_AXI_arqos),
        .S_AXI_arready(S_AXI_arready),
        .S_AXI_arregion(S_AXI_arregion),
        .S_AXI_arsize(S_AXI_arsize),
        .S_AXI_arvalid(S_AXI_arvalid),
        //
        .S_AXI_awaddr(S_AXI_awaddr),
        .S_AXI_awburst(S_AXI_awburst),
        .S_AXI_awcache(S_AXI_awcache),
        .S_AXI_awid(S_AXI_awid),
        .S_AXI_awlen(S_AXI_awlen),
        .S_AXI_awlock(S_AXI_awlock),
        .S_AXI_awprot(S_AXI_awprot),
        .S_AXI_awqos(S_AXI_awqos),
        .S_AXI_awready(S_AXI_awready),
        .S_AXI_awregion(S_AXI_awregion),
        .S_AXI_awsize(S_AXI_awsize),
        .S_AXI_awvalid(S_AXI_awvalid),
        //
        .S_AXI_bid(S_AXI_bid),
        .S_AXI_bready(S_AXI_bready),
        .S_AXI_bresp(S_AXI_bresp),
        .S_AXI_bvalid(S_AXI_bvalid),
        //
        .S_AXI_rid(S_AXI_rid),
        .S_AXI_rdata(S_AXI_rdata),
        .S_AXI_rlast(S_AXI_rlast),
        .S_AXI_rready(S_AXI_rready),
        .S_AXI_rresp(S_AXI_rresp),
        .S_AXI_rvalid(S_AXI_rvalid),
        //
        .S_AXI_wdata(S_AXI_wdata),
        .S_AXI_wlast(S_AXI_wlast),
        .S_AXI_wready(S_AXI_wready),
        .S_AXI_wstrb(S_AXI_wstrb),
        .S_AXI_wvalid(S_AXI_wvalid),
        .ext_clk_in(host_clk)
        );

`define DCOUNT_ADDR 5'h00
`define RFIFO_ADDR  5'h01

`define WFIFO_ADDR 5'h00
`define RESET_ADDR 5'h1f

  // HTIF interface between ARM and reference chip on FPGA via memory mapped registers
  // 2 read addresses : 1 for FIFO data count (0x0), 1 for FIFO data (0x1)
  // 2 write addresses: 1 for FIFO data (0x0), 1 for reset (0x31)
  
  // host_in (from ARM to fpga)

  assign waddr = M_AXI_awaddr[6:2];
  assign raddr = M_AXI_araddr[6:2];

  fifo_32x32 host_in_fifo (
    .clk(host_clk),
    .reset(reset),
    .din(M_AXI_wdata),
    .wren(host_in_fifo_wren),
    .rden(host_in_fifo_rden),
    .dout(host_in_fifo_dout),
    .full(host_in_fifo_full),
    .empty(host_in_fifo_empty),
    .count()
  );

  assign host_in_valid = !host_in_fifo_empty;
  assign host_in_fifo_rden = host_in_count && host_in_valid && host_in_ready;
  assign host_in_bits = !host_in_count ? host_in_fifo_dout[15:0] : host_in_fifo_dout[31:16];

  // host_out (from FPGA to ARM)
  
  assign host_out_ready = !host_out_fifo_full;
  assign host_out_fifo_wren = (host_out_count == 1'b1);
  assign host_out_fifo_rden = M_AXI_rvalid && M_AXI_rready && (raddr_r == `RFIFO_ADDR);

  fifo_32x32 host_out_fifo (
    .clk(host_clk),
    .reset(reset),
    .din({host_out_bits, host_out_bits_r}),
    .wren(host_out_fifo_wren),
    .rden(host_out_fifo_rden),
    .dout(host_out_fifo_dout),
    .full(host_out_fifo_full),
    .empty(host_out_fifo_empty),
    .count(host_out_fifo_count)
  );

  assign reset = !FCLK_RESET0_N || !mmcm_locked;

  parameter st_rd_idle = 1'b0;
  parameter st_rd_read = 1'b1;

  reg st_rd = st_rd_idle;

  parameter st_wr_idle  = 2'd0;
  parameter st_wr_write = 2'd1;
  parameter st_wr_ack   = 2'd2;

  reg [1:0] st_wr = st_wr_idle;

  always @(posedge host_clk)
  begin

    if (reset)
    begin
      host_out_bits_r <= 16'd0;
      host_out_count <= 1'd0;
      host_in_count <= 1'd0;
      raddr_r <= 5'd0;
      waddr_r <= 5'd0;
      arid_r <= 12'd0;
      awid_r <= 12'd0;
      st_rd <= st_rd_idle;
      st_wr <= st_wr_idle;
    end
    else
    begin
      if (host_out_valid)
      begin
        host_out_bits_r <= host_out_bits;
        host_out_count <= host_out_count + 1;
      end
      if (host_in_valid && host_in_ready)
        host_in_count <= host_in_count + 1;

// state machine to handle reads from AXI master (ARM)
      case (st_rd)
        st_rd_idle : begin
          if (M_AXI_arvalid)
          begin
            st_rd <= st_rd_read;
            raddr_r <= raddr;
            arid_r <= M_AXI_arid;
          end
        end
        st_rd_read : begin
          if (M_AXI_rready)
            st_rd <= st_rd_idle;
        end
      endcase

// state machine to handle writes from AXI master
      case (st_wr)
        st_wr_idle : begin
          if (M_AXI_awvalid && M_AXI_wvalid)
          begin
            st_wr <= st_wr_write;
            waddr_r <= waddr;
            awid_r <= M_AXI_awid;
          end
        end
        st_wr_write : begin
          if (!host_in_fifo_full || (waddr_r == `RESET_ADDR))
            st_wr <= st_wr_ack;
        end
        st_wr_ack : begin
          if (M_AXI_bready)
            st_wr <= st_wr_idle;
        end
      endcase

    end
  end

  assign M_AXI_arready = (st_rd == st_rd_idle);
  assign M_AXI_rvalid  = (st_rd == st_rd_read);
  assign M_AXI_rlast   = (st_rd == st_rd_read);
  assign M_AXI_rdata   = (raddr_r == `DCOUNT_ADDR) ? {27'd0, host_out_fifo_count} : host_out_fifo_dout;
  assign M_AXI_rid = arid_r;

  wire do_write = (st_wr == st_wr_write);
  assign M_AXI_awready = do_write;
  assign M_AXI_wready  = do_write;
  assign host_in_fifo_wren = do_write && (waddr_r == `WFIFO_ADDR);
  assign reset_cpu = do_write && (waddr_r == `RESET_ADDR);

  assign M_AXI_bvalid = (st_wr == st_wr_ack);
  assign M_AXI_bid = awid_r;

/*
  fifo_8x5 tag_queue (
    .clk(host_clk),
    .reset(reset),
    .din(mem_req_tag),
    .wren(S_AXI_arvalid & S_AXI_arready),
    .rden(S_AXI_rlast_r),
    .dout(mem_resp_tag),
    .full(),
    .empty()
  );
*/

  wire [31:0] mem_araddr;
  wire [31:0] mem_awaddr;

  // Memory given to Rocket is the upper 256 MB of the 512 MB DRAM
  assign S_AXI_araddr = {4'd1, mem_araddr[27:0]};
  assign S_AXI_awaddr = {4'd1, mem_awaddr[27:0]};

  Top top(
       .clk(host_clk),
       .reset(reset_cpu),
       .io_host_in_ready( host_in_ready ),
       .io_host_in_valid( host_in_valid ),
       .io_host_in_bits( host_in_bits ),
       .io_host_out_ready( host_out_ready ),
       .io_host_out_valid( host_out_valid ),
       .io_host_out_bits( host_out_bits ),
       .io_mem_0_ar_valid (S_AXI_arvalid),
       .io_mem_0_ar_ready (S_AXI_arready),
       .io_mem_0_ar_bits_addr (mem_araddr),
       .io_mem_0_ar_bits_id (S_AXI_arid),
       .io_mem_0_ar_bits_size (S_AXI_arsize),
       .io_mem_0_ar_bits_len (S_AXI_arlen),
       .io_mem_0_ar_bits_burst (S_AXI_arburst),
       .io_mem_0_ar_bits_cache (S_AXI_arcache),
       .io_mem_0_ar_bits_lock (S_AXI_arlock),
       .io_mem_0_ar_bits_prot (S_AXI_arprot),
       .io_mem_0_ar_bits_qos (S_AXI_arqos),
       .io_mem_0_ar_bits_region(S_AXI_arregion),
       .io_mem_0_aw_valid (S_AXI_awvalid),
       .io_mem_0_aw_ready (S_AXI_awready),
       .io_mem_0_aw_bits_addr (mem_awaddr),
       .io_mem_0_aw_bits_id (S_AXI_awid),
       .io_mem_0_aw_bits_size (S_AXI_awsize),
       .io_mem_0_aw_bits_len (S_AXI_awlen),
       .io_mem_0_aw_bits_burst (S_AXI_awburst),
       .io_mem_0_aw_bits_cache (S_AXI_awcache),
       .io_mem_0_aw_bits_lock (S_AXI_awlock),
       .io_mem_0_aw_bits_prot (S_AXI_awprot),
       .io_mem_0_aw_bits_qos (S_AXI_awqos),
       .io_mem_0_aw_bits_region(S_AXI_awregion),
       .io_mem_0_w_valid (S_AXI_wvalid),
       .io_mem_0_w_ready (S_AXI_wready),
       .io_mem_0_w_bits_strb (S_AXI_wstrb),
       .io_mem_0_w_bits_data (S_AXI_wdata),
       .io_mem_0_w_bits_last (S_AXI_wlast),
       .io_mem_0_b_valid (S_AXI_bvalid),
       .io_mem_0_b_ready (S_AXI_bready),
       .io_mem_0_b_bits_resp (S_AXI_bresp),
       .io_mem_0_b_bits_id (S_AXI_bid),
       .io_mem_0_r_valid (S_AXI_rvalid),
       .io_mem_0_r_ready (S_AXI_rready),
       .io_mem_0_r_bits_resp (S_AXI_rresp),
       .io_mem_0_r_bits_id (S_AXI_rid),
       .io_mem_0_r_bits_data (S_AXI_rdata),
       .io_mem_0_r_bits_last (S_AXI_rlast)
  );
`ifndef differential_clock
  IBUFG ibufg_gclk (.I(clk), .O(gclk_i));
`else
  IBUFDS #(.DIFF_TERM("TRUE"), .IBUF_LOW_PWR("TRUE"), .IOSTANDARD("DEFAULT")) clk_ibufds (.O(gclk_i), .I(SYSCLK_P), .IB(SYSCLK_N));
`endif
  BUFG  bufg_host_clk (.I(host_clk_i), .O(host_clk));

  MMCME2_BASE #(
    .BANDWIDTH("OPTIMIZED"),
    .CLKFBOUT_MULT_F(`RC_CLK_MULT),
    .CLKFBOUT_PHASE(0.0),
    .CLKIN1_PERIOD(`ZYNQ_CLK_PERIOD),
    .CLKOUT1_DIVIDE(1),
    .CLKOUT2_DIVIDE(1),
    .CLKOUT3_DIVIDE(1),
    .CLKOUT4_DIVIDE(1),
    .CLKOUT5_DIVIDE(1),
    .CLKOUT6_DIVIDE(1),
    .CLKOUT0_DIVIDE_F(`RC_CLK_DIVIDE),
    .CLKOUT0_DUTY_CYCLE(0.5),
    .CLKOUT1_DUTY_CYCLE(0.5),
    .CLKOUT2_DUTY_CYCLE(0.5),
    .CLKOUT3_DUTY_CYCLE(0.5),
    .CLKOUT4_DUTY_CYCLE(0.5),
    .CLKOUT5_DUTY_CYCLE(0.5),
    .CLKOUT6_DUTY_CYCLE(0.5),
    .CLKOUT0_PHASE(0.0),
    .CLKOUT1_PHASE(0.0),
    .CLKOUT2_PHASE(0.0),
    .CLKOUT3_PHASE(0.0),
    .CLKOUT4_PHASE(0.0),
    .CLKOUT5_PHASE(0.0),
    .CLKOUT6_PHASE(0.0),
    .CLKOUT4_CASCADE("FALSE"),
    .DIVCLK_DIVIDE(1),
    .REF_JITTER1(0.0),
    .STARTUP_WAIT("FALSE")
  ) MMCME2_BASE_inst (
    .CLKOUT0(host_clk_i),
    .CLKOUT0B(),
    .CLKOUT1(),
    .CLKOUT1B(),
    .CLKOUT2(),
    .CLKOUT2B(),
    .CLKOUT3(),
    .CLKOUT3B(),
    .CLKOUT4(),
    .CLKOUT5(),
    .CLKOUT6(),
    .CLKFBOUT(gclk_fbout),
    .CLKFBOUTB(),
    .LOCKED(mmcm_locked),
    .CLKIN1(gclk_i),
    .PWRDWN(1'b0),
    .RST(1'b0),
    .CLKFBIN(gclk_fbout));

endmodule


// fifo queues originally from fifos.v

/*
module fifo_8x5 (
    input clk,
    input reset,
    input wren,
    input rden,
    input [4:0] din,
    output reg empty,
    output reg full,
    output [4:0] dout
    );

  reg [4:0] data [0:7];
  reg [2:0] raddr, waddr;
  wire [2:0] waddr_next, raddr_next;
  wire write = wren && (rden || !full);
  wire read = rden && !empty;

  assign waddr_next = write ? waddr + 1'b1 : waddr;
  assign raddr_next = read ? raddr + 1'b1 : raddr;
  assign dout = data[raddr];

  always @(posedge clk)
  begin
    if (reset)
    begin
      empty <= 1'b1;
      full <= 1'b0;
      raddr <= 3'd0;
      waddr <= 3'd0;
    end
    else
    begin
      waddr <= waddr_next;
      raddr <= raddr_next;
      if (write)
        data[waddr] <= din;

      if (read && raddr_next == waddr_next && !full)
        empty <= 1'b1;
      else if (write && !read)
        empty <= 1'b0;

      if (write && raddr_next == waddr_next)
        full <= 1'b1;
      else if (read && !write)
        full <= 1'b0;

    end
  end
endmodule
*/

module fifo_32x32 (
    input clk,
    input reset,
    input wren,
    input rden,
    input [31:0] din,
    output reg empty,
    output reg full,
    output [31:0] dout,
    output [4:0] count
    );

  reg [31:0] data [0:31];
  reg [4:0] raddr, waddr, cnt;
  wire [4:0] waddr_next, raddr_next;
  wire write = wren && (rden || !full);
  wire read = rden && !empty;

  assign waddr_next = write ? waddr + 1'b1 : waddr;
  assign raddr_next = read ? raddr + 1'b1 : raddr;
  assign dout = data[raddr];
  assign count = cnt;

  always @(posedge clk)
  begin
    if (reset)
    begin
      empty <= 1'b1;
      full <= 1'b0;
      raddr <= 5'd0;
      waddr <= 5'd0;
      cnt <= 5'd0;
    end
    else
    begin
      waddr <= waddr_next;
      raddr <= raddr_next;
      if (write)
        data[waddr] <= din;

      if (read && raddr_next == waddr_next && !full)
        empty <= 1'b1;
      else if (write && !read)
        empty <= 1'b0;

      if (write && raddr_next == waddr_next)
        full <= 1'b1;
      else if (read && !write)
        full <= 1'b0;

      if (write ^ read)
        cnt <= write ? cnt + 1 : cnt - 1;

    end
  end
endmodule
