#include "zynq_driver.h"

#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <assert.h>

#define ZYNQ_BASE_PADDR 0x43C00000L

#define TSI_OUT_FIFO_DATA 0x00
#define TSI_OUT_FIFO_COUNT 0x04
#define TSI_IN_FIFO_DATA 0x08
#define TSI_IN_FIFO_COUNT 0x0C
#define SYSTEM_RESET 0x10
#define BLKDEV_REQ_FIFO_DATA 0x20
#define BLKDEV_REQ_FIFO_COUNT 0x24
#define BLKDEV_DATA_FIFO_DATA 0x28
#define BLKDEV_DATA_FIFO_COUNT 0x2C
#define BLKDEV_RESP_FIFO_DATA 0x30
#define BLKDEV_RESP_FIFO_COUNT 0x34
#define BLKDEV_NSECTORS_REG 0x38

#define BLKDEV_REQ_NWORDS 4
#define BLKDEV_DATA_NWORDS 3

zynq_driver_t::zynq_driver_t(tsi_t *tsi, BlockDevice *bdev)
{
    this->tsi = tsi;
    this->bdev = bdev;

    fd = open("/dev/mem", O_RDWR|O_SYNC);
    assert(fd != -1);
    dev = (uint8_t *) mmap(
            0, sysconf(_SC_PAGESIZE),
            PROT_READ|PROT_WRITE, MAP_SHARED, fd, ZYNQ_BASE_PADDR);
    assert(dev != MAP_FAILED);

    // reset the target
    write(SYSTEM_RESET, 1);
    write(SYSTEM_RESET, 0);

    // set nsectors
    write(BLKDEV_NSECTORS_REG, bdev->nsectors());
}

zynq_driver_t::~zynq_driver_t()
{
    munmap(dev, sysconf(_SC_PAGESIZE));
    close(fd);
}

uint32_t zynq_driver_t::read(int off)
{
    volatile uint32_t *ptr = (volatile uint32_t *) (this->dev + off);
    return *ptr;
}

void zynq_driver_t::write(int off, uint32_t word)
{
    volatile uint32_t *ptr = (volatile uint32_t *) (this->dev + off);
    *ptr = word;
}

struct blkdev_request zynq_driver_t::read_request()
{
    uint32_t word;
    struct blkdev_request req;

    // tag + write
    word = read(BLKDEV_REQ_FIFO_DATA);
    req.write = word & 0x1;
    req.tag = word >> 1;
    // addr (this is ignored)
    word = read(BLKDEV_REQ_FIFO_DATA);
    // offset, then len
    req.offset = read(BLKDEV_REQ_FIFO_DATA);
    req.len = read(BLKDEV_REQ_FIFO_DATA);

    return req;
}

struct blkdev_data zynq_driver_t::read_req_data()
{
    struct blkdev_data data;

    data.tag = read(BLKDEV_DATA_FIFO_DATA);
    data.data = read(BLKDEV_DATA_FIFO_DATA) & 0xffffffffU;
    data.data |= ((uint64_t) read(BLKDEV_DATA_FIFO_DATA)) << 32;

    return data;
}

void zynq_driver_t::write_response(struct blkdev_data &resp)
{
    write(BLKDEV_RESP_FIFO_DATA, resp.tag);
    write(BLKDEV_RESP_FIFO_DATA, resp.data & 0xffffffffU);
    write(BLKDEV_RESP_FIFO_DATA, resp.data >> 32);
}

void zynq_driver_t::poll(void)
{
    while (read(TSI_OUT_FIFO_COUNT) > 0) {
        uint32_t out_data = read(TSI_OUT_FIFO_DATA);
        tsi->send_word(out_data);
    }

    while (tsi->data_available() && read(TSI_IN_FIFO_COUNT) > 0) {
        uint32_t in_data = tsi->recv_word();
        write(TSI_IN_FIFO_DATA, in_data);
    }

    while (read(BLKDEV_REQ_FIFO_COUNT) >= BLKDEV_REQ_NWORDS) {
        struct blkdev_request req = read_request();
        bdev->send_request(req);
    }

    while (read(BLKDEV_DATA_FIFO_COUNT) >= BLKDEV_DATA_NWORDS) {
        struct blkdev_data data = read_req_data();
        bdev->send_data(data);
    }

    while (bdev->resp_valid() && read(BLKDEV_RESP_FIFO_COUNT) >= BLKDEV_DATA_NWORDS) {
        struct blkdev_data resp = bdev->recv_response();
        write_response(resp);
    }

    tsi->switch_to_host();
    bdev->switch_to_host();
}
