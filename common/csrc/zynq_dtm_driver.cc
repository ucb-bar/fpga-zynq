// See LICENSE for license details.

#include "zynq_dtm_driver.h"
#include <fesvr/dtm.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <assert.h>
#include <stdio.h>

#define read_reg(r) (dev_vaddr[r>>2])
#define write_reg(r, v) (dev_vaddr[r>>2] = v)

// These are byte addresses
#define DTM_REQ_PAYLOAD_VADDR 0x0
#define DTM_REQ_VALID_VADDR 0x8
#define DTM_RESP_VADDR 0x10
#define RESET_VADDR 0x20

// Field lengths
#define DM_RESP_RC_MASK 0x3
#define DM_RESP_RC_OFFSET 2
#define DM_DATA_BITS 34
#define DM_DATA_H_MASK 0x3
#define DM_REQ_OP_OFFSET 2
#define DM_REQ_ADDR_OFFSET 4


zynq_dtm_driver_t::zynq_dtm_driver_t()
{
  int fd = open("/dev/mem", O_RDWR|O_SYNC);
  assert(fd != -1);
  dev_vaddr = (uintptr_t*)mmap(0, sysconf(_SC_PAGESIZE), PROT_READ|PROT_WRITE, MAP_SHARED, fd, dev_paddr);
  assert(dev_vaddr != MAP_FAILED);
  write_reg(RESET_VADDR, 0);
}

zynq_dtm_driver_t::~zynq_dtm_driver_t()
{
}

void zynq_dtm_driver_t::poll(dtm_t * dtm){
  uint32_t req_l = dtm->req_bits().data;
  write_reg(DTM_REQ_PAYLOAD_VADDR, req_l);
  uint32_t req_h = ((dtm->req_bits().data>>32) & DM_DATA_H_MASK) +
                   (dtm->req_bits().op << DM_REQ_OP_OFFSET) +
                   (dtm->req_bits().addr << DM_REQ_ADDR_OFFSET);
  write_reg((DTM_REQ_PAYLOAD_VADDR + sizeof(uint32_t)), req_h);

  // Ensure the lower word has been written before initiating the request
  asm volatile ("dmb");
  write_reg(DTM_REQ_VALID_VADDR, 1);
  // Ensure our previous request has been received before pooling for resp
  asm volatile ("dmb");

  dtm_t::resp resp_buf;
  uint32_t data_l = read_reg(DTM_RESP_VADDR);
  uint32_t data_h = read_reg((DTM_RESP_VADDR + sizeof(uint32_t)));
  resp_buf.resp = (data_h >> DM_RESP_RC_OFFSET) & DM_RESP_RC_MASK;
  resp_buf.data = ((uint64_t (data_h & DM_DATA_H_MASK)) << 32) + data_l;
  dtm->return_resp(resp_buf);
}
