#ifndef __ZYNQ_SAI_DRIVER_H
#define __ZYNQ_SAI_DRIVER_H

#include "fesvr/tsi.h"
#include <stdint.h>

class zynq_tsi_driver_t {
  public:
    zynq_tsi_driver_t();
    ~zynq_tsi_driver_t();

    void poll(tsi_t *tsi);

  private:
    uint8_t *dev;
    int fd;

    uint32_t read(int off);
    void write(int off, uint32_t word);
};

#endif
