TOP_MODULE ?= TestHarness
CONFIG ?= ZynqConfig
TB ?= TestDriver

simv = simv-$(CONFIG)
simv_debug = simv-$(CONFIG)-debug

default: $(simv)
debug: $(simv_debug)

include ../common/Makefrag

sim_vsrcs = \
	src/verilog/$(TOP_MODULE).$(CONFIG).v \
	$(base_dir)/rocket-chip/vsrc/TestDriver.v \
	$(base_dir)/testchipip/vsrc/SimSerial.v

sim_csrcs = \
	$(base_dir)/testchipip/csrc/SimSerial.cc

VCS = vcs -full64

VCS_OPTS = -notice -line +lint=all,noVCDE,noONGS,noUI -error=PCWM-L -timescale=1ns/10ps -quiet \
	+rad +v2k +vcs+lic+wait \
	+vc+list -CC "-I$(VCS_HOME)/include" \
	-CC "-I$(RISCV)/include" \
	-CC "-std=c++11" \
	-CC "-Wl,-rpath,$(RISCV)/lib" \
	$(RISCV)/lib/libfesvr.so \
	-sverilog \
	+incdir+$(generated_dir) \
	+define+CLOCK_PERIOD=1.0 $(sim_vsrcs) $(sim_csrcs) \
	+define+PRINTF_COND=$(TB).printf_cond \
	+define+STOP_COND=!$(TB).reset \
	+define+RANDOMIZE_MEM_INIT \
	+define+RANDOMIZE_REG_INIT \
	+define+RANDOMIZE_GARBAGE_ASSIGN \
	+define+RANDOMIZE_INVALID_ASSIGN \
	+libext+.v \

verilog: $(sim_vsrcs)

$(simv): $(sim_vsrcs) $(sim_csrcs)
	rm -rf csrc && $(VCS) $(VCS_OPTS) -o $@ \
	-debug_pp

$(simv_debug) : $(sim_vsrcs) $(sim_csrcs)
	rm -rf csrc && $(VCS) $(VCS_OPTS) -o $@ \
	+define+DEBUG -debug_pp
