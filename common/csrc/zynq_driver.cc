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
#define BLKDEV_NSECTORS 0x38
#define BLKDEV_MAX_REQUEST_LENGTH 0x3C
#define NET_OUT_FIFO_DATA 0x40
#define NET_OUT_FIFO_COUNT 0x44
#define NET_IN_FIFO_DATA 0x48
#define NET_IN_FIFO_COUNT 0x4C
#define NET_MACADDR_LO 0x50
#define NET_MACADDR_HI 0x54

#define BLKDEV_REQ_NWORDS 4
#define BLKDEV_DATA_NWORDS 3
#define NET_FLIT_NWORDS 3

zynq_driver_t::zynq_driver_t(tsi_t *tsi, BlockDevice *bdev,
                            NetworkDevice *netdev, NetworkSwitch *netsw)
{
    this->tsi = tsi;
    this->bdev = bdev;
    this->netdev = netdev;
    this->netsw = netsw;

    fd = open("/dev/mem", O_RDWR|O_SYNC);
    assert(fd != -1);
    dev = (uint8_t *) mmap(
            0, sysconf(_SC_PAGESIZE),
            PROT_READ|PROT_WRITE, MAP_SHARED, fd, ZYNQ_BASE_PADDR);
    assert(dev != MAP_FAILED);

    // reset the target
    write(SYSTEM_RESET, 1);
    write(SYSTEM_RESET, 0);

    // set nsectors and max_request_length
    if (bdev == NULL) {
        write(BLKDEV_NSECTORS, 0);
        write(BLKDEV_MAX_REQUEST_LENGTH, 0);
    } else {
        write(BLKDEV_NSECTORS, bdev->nsectors());
        write(BLKDEV_MAX_REQUEST_LENGTH, bdev->max_request_length());
    }

    // set MAC address
    if (netdev != NULL) {
        write_macaddr(netdev->macaddr());
    }
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

struct blkdev_request zynq_driver_t::read_blkdev_request()
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

struct blkdev_data zynq_driver_t::read_blkdev_req_data()
{
    struct blkdev_data data;

    data.tag = read(BLKDEV_DATA_FIFO_DATA);
    data.data = read(BLKDEV_DATA_FIFO_DATA) & 0xffffffffU;
    data.data |= ((uint64_t) read(BLKDEV_DATA_FIFO_DATA)) << 32;

    return data;
}

void zynq_driver_t::write_blkdev_response(struct blkdev_data &resp)
{
    write(BLKDEV_RESP_FIFO_DATA, resp.tag);
    write(BLKDEV_RESP_FIFO_DATA, resp.data & 0xffffffffU);
    write(BLKDEV_RESP_FIFO_DATA, resp.data >> 32);
}

struct network_flit zynq_driver_t::read_net_out()
{
    struct network_flit flt;

    flt.data = read(NET_OUT_FIFO_DATA) & 0xffffffff;
    flt.data |= ((uint64_t) read(NET_OUT_FIFO_DATA)) << 32;
    flt.last = read(NET_OUT_FIFO_DATA) & 0x1;

    return flt;
}

void zynq_driver_t::write_net_in(struct network_flit &flt)
{
    write(NET_IN_FIFO_DATA, flt.data & 0xffffffff);
    write(NET_IN_FIFO_DATA, flt.data >> 32);
    write(NET_IN_FIFO_DATA, flt.last);
}

void zynq_driver_t::write_macaddr(uint64_t macaddr)
{
    write(NET_MACADDR_LO, macaddr & 0xffffffff);
    write(NET_MACADDR_HI, macaddr >> 32);
}

void zynq_driver_t::poll(void)
{
    if (tsi != NULL) {
        while (read(TSI_OUT_FIFO_COUNT) > 0) {
            uint32_t out_data = read(TSI_OUT_FIFO_DATA);
            tsi->send_word(out_data);
        }

        while (tsi->data_available() && read(TSI_IN_FIFO_COUNT) > 0) {
            uint32_t in_data = tsi->recv_word();
            write(TSI_IN_FIFO_DATA, in_data);
        }

        tsi->switch_to_host();
    }

    if (netdev != NULL) {
        while (read(NET_OUT_FIFO_COUNT) >= NET_FLIT_NWORDS) {
            struct network_flit flt = read_net_out();
            netdev->send_out(flt);
        }

        while (netdev->in_valid() && read(NET_IN_FIFO_COUNT) >= NET_FLIT_NWORDS) {
            struct network_flit flt = netdev->recv_in();
            write_net_in(flt);
        }

        netdev->switch_to_host();
        netsw->distribute();
        netsw->switch_to_worker();
    }

    if (bdev != NULL) {
        while (read(BLKDEV_REQ_FIFO_COUNT) >= BLKDEV_REQ_NWORDS) {
            struct blkdev_request req = read_blkdev_request();
            bdev->send_request(req);
        }

        while (read(BLKDEV_DATA_FIFO_COUNT) >= BLKDEV_DATA_NWORDS) {
            struct blkdev_data data = read_blkdev_req_data();
            bdev->send_data(data);
        }

        while (bdev->resp_valid() && read(BLKDEV_RESP_FIFO_COUNT) >= BLKDEV_DATA_NWORDS) {
            struct blkdev_data resp = bdev->recv_response();
            write_blkdev_response(resp);
        }

        bdev->switch_to_host();
    }
}
