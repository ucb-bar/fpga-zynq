#ifndef __ZYNQ_DRIVER_H
#define __ZYNQ_DRIVER_H

#include "fesvr/tsi.h"
#include "blkdev.h"
#include <stdint.h>

class zynq_driver_t {
  public:
    zynq_driver_t(tsi_t *tsi, BlockDevice *bdev);
    ~zynq_driver_t();

    void poll(void);

  private:
    uint8_t *dev;
    int fd;
    tsi_t *tsi;
    BlockDevice *bdev;

  protected:
    uint32_t read(int off);
    void write(int off, uint32_t word);
    struct blkdev_request read_request();
    struct blkdev_data read_req_data();
    void write_response(struct blkdev_data &resp);
};

#endif
