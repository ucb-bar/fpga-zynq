#include "zynq_sai_driver.h"

#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <assert.h>

#define SAI_BASE_PADDR 0x43C00000L
#define SAI_OUT_FIFO_DATA 0x0
#define SAI_OUT_FIFO_COUNT 0x4
#define SAI_IN_FIFO_DATA 0x8
#define SAI_IN_FIFO_COUNT 0xC

zynq_sai_driver_t::zynq_sai_driver_t()
{
    fd = open("/dev/mem", O_RDWR|O_SYNC);
    assert(fd != -1);
    dev = (uint8_t *) mmap(0, sysconf(_SC_PAGESIZE), PROT_READ|PROT_WRITE, MAP_SHARED, fd, SAI_BASE_PADDR);
    assert(dev != MAP_FAILED);
}


zynq_sai_driver_t::~zynq_sai_driver_t()
{
    munmap(dev, sysconf(_SC_PAGESIZE));
    close(fd);
}

void zynq_sai_driver_t::poll(sai_t *sai)
{
    while (read(SAI_OUT_FIFO_COUNT) > 0) {
        uint32_t out_data = read(SAI_OUT_FIFO_DATA);
        sai->send_word(out_data);
    }

    while (sai->data_available() && read(SAI_IN_FIFO_COUNT) > 0) {
        uint32_t in_data = sai->recv_word();
        write(SAI_IN_FIFO_DATA, in_data);
    }

    sai->switch_to_host();
}

uint32_t zynq_sai_driver_t::read(int off)
{
    volatile uint32_t *ptr = (volatile uint32_t *) (this->dev + off);
    return *ptr;
}

void zynq_sai_driver_t::write(int off, uint32_t word)
{
    volatile uint32_t *ptr = (volatile uint32_t *) (this->dev + off);
    *ptr = word;
}
