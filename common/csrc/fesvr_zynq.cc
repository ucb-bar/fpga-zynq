#include "zynq_driver.h"
#include "fesvr/tsi.h"
#include <vector>

#define BLKDEV_NTAGS 2

int main(int argc, char** argv)
{
    tsi_t tsi(std::vector<std::string>(argv + 1, argv + argc));

    const char *fname = NULL;
    BlockDevice *blkdev = NULL;
    zynq_driver_t *driver;

    for (int i = 1; i < argc; i++) {
        if (strncmp(argv[i], "+blkdev=", 8) == 0) {
            fname = argv[i] + 8;
            break;
        }
    }

    if (fname != NULL)
        blkdev = new BlockDevice(fname, BLKDEV_NTAGS);
    driver = new zynq_driver_t(&tsi, blkdev);

    while(!tsi.done()){
        driver->poll();
    }

    delete driver;
    if (blkdev != NULL)
        delete blkdev;

    return tsi.exit_code();
}
