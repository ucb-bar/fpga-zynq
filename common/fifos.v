// fifo queues

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
