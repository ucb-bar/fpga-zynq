#ifndef __ZYNQ_DRIVER_H
#define __ZYNQ_DRIVER_H

#include "fesvr/tsi.h"
#include "blkdev.h"
#include "network.h"
#include <stdint.h>

class zynq_driver_t {
  public:
    zynq_driver_t(tsi_t *tsi, BlockDevice *bdev, NetworkDevice *netdev);
    ~zynq_driver_t();

    void poll(void);

  private:
    uint8_t *dev;
    int fd;
    tsi_t *tsi;
    BlockDevice *bdev;
    NetworkDevice *netdev;

  protected:
    uint32_t read(int off);
    void write(int off, uint32_t word);
    struct blkdev_request read_blkdev_request();
    struct blkdev_data read_blkdev_req_data();
    void write_blkdev_response(struct blkdev_data &resp);
    struct network_flit read_net_out();
    void write_net_in(struct network_flit &flt);
};

#endif
