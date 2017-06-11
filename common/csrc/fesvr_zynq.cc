#include "zynq_driver.h"
#include "fesvr/tsi.h"
#include <vector>

#define BLKDEV_NTAGS 2

int main(int argc, char** argv)
{
    const char *fname = NULL;

    for (int i = 1; i < argc; i++) {
        if (strncmp(argv[i], "+blkdev=", 8) == 0) {
            fname = argv[i] + 8;
            break;
        }
    }

    tsi_t tsi(std::vector<std::string>(argv + 1, argv + argc));
    BlockDevice blkdev(fname, BLKDEV_NTAGS);
    zynq_driver_t driver(&tsi, &blkdev);

    while(!tsi.done()){
        driver.poll();
    }

    return tsi.exit_code();
}
