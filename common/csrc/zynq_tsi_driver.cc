#include "zynq_tsi_driver.h"

#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <assert.h>

#define SAI_BASE_PADDR 0x43C00000L
#define SAI_OUT_FIFO_DATA 0x00
#define SAI_OUT_FIFO_COUNT 0x04
#define SAI_IN_FIFO_DATA 0x08
#define SAI_IN_FIFO_COUNT 0x0C
#define SAI_SYS_RESET 0x10

zynq_tsi_driver_t::zynq_tsi_driver_t()
{
    fd = open("/dev/mem", O_RDWR|O_SYNC);
    assert(fd != -1);
    dev = (uint8_t *) mmap(0, sysconf(_SC_PAGESIZE), PROT_READ|PROT_WRITE, MAP_SHARED, fd, SAI_BASE_PADDR);
    assert(dev != MAP_FAILED);

    // reset the target
    write(SAI_SYS_RESET, 1);
    write(SAI_SYS_RESET, 0);
}


zynq_tsi_driver_t::~zynq_tsi_driver_t()
{
    munmap(dev, sysconf(_SC_PAGESIZE));
    close(fd);
}

void zynq_tsi_driver_t::poll(tsi_t *tsi)
{
    while (read(SAI_OUT_FIFO_COUNT) > 0) {
        uint32_t out_data = read(SAI_OUT_FIFO_DATA);
        tsi->send_word(out_data);
    }

    while (tsi->data_available() && read(SAI_IN_FIFO_COUNT) > 0) {
        uint32_t in_data = tsi->recv_word();
        write(SAI_IN_FIFO_DATA, in_data);
    }

    tsi->switch_to_host();
}

uint32_t zynq_tsi_driver_t::read(int off)
{
    volatile uint32_t *ptr = (volatile uint32_t *) (this->dev + off);
    return *ptr;
}

void zynq_tsi_driver_t::write(int off, uint32_t word)
{
    volatile uint32_t *ptr = (volatile uint32_t *) (this->dev + off);
    *ptr = word;
}
