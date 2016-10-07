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
    uint8_t *dev;
    int fd;

    uint32_t read(int off);
    void write(int off, uint32_t word);
};

#endif
