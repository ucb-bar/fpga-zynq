#ifndef __ZYNQ_SAI_DRIVER_H
#define __ZYNQ_SAI_DRIVER_H

#include "fesvr/sai.h"
#include <stdint.h>

class zynq_sai_driver_t {
  public:
    zynq_sai_driver_t();
    ~zynq_sai_driver_t();

    void poll(sai_t *sai);

  private:
    volatile uint32_t *dev;
    int fd;
};

#endif
