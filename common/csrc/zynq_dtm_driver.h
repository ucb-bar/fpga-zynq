#ifndef __ZYNQ_DTM_DRIVER_H
#define __ZYNQ_DTM_DRIVER_H

#include "fesvr/dtm.h"
#include <stdint.h>

class zynq_dtm_driver_t {
  public:
    zynq_dtm_driver_t();
    ~zynq_dtm_driver_t();

    void poll(dtm_t * dtm);

 private:
   volatile uintptr_t* dev_vaddr;
   const static uintptr_t dev_paddr = 0x43C00000;

};

#endif // __ZYNQ_DTM_DRIVER_H

