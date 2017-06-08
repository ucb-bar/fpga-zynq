#include "zynq_driver.h"
#include "fesvr/tsi.h"
#include <vector>

#define BLKDEV_NTAGS 2

static inline int copy_argv(int argc, char **argv, char **new_argv)
{
    int optind = 1;
    int new_argc = argc;

    new_argv[0] = argv[0];

    for (int i = 1; i < argc; i++) {
        if (argv[i][0] != '+') {
            optind = i - 1;
            new_argc = argc - i + 1;
            break;
        }
    }

    for (int i = 1; i < new_argc; i++)
        new_argv[i] = argv[i + optind];

    return new_argc;
}

int main(int argc, char** argv)
{
    char **new_argv = (char **) malloc(sizeof(char *) * argc);
    int new_argc = copy_argv(argc, argv, new_argv);
    tsi_t tsi(new_argc, new_argv);

    BlockDevice *blkdev = NULL;
    NetworkDevice *netdev = NULL;
    NetworkSwitch *netsw = NULL;
    zynq_driver_t *driver;

    for (int i = 1; i < argc; i++) {
        const char *name = NULL;

        if (strncmp(argv[i], "+blkdev=", 8) == 0) {
            name = argv[i] + 8;
            blkdev = new BlockDevice(name, BLKDEV_NTAGS);
        } else if (strncmp(argv[i], "+netdev=", 8) == 0) {
            name = argv[i] + 8;
            netsw = new NetworkSwitch(name);
            netdev = new NetworkDevice(random_macaddr());
            netsw->add_device(netdev);
        }
    }

    driver = new zynq_driver_t(&tsi, blkdev, netdev, netsw);

    while(!tsi.done()){
        driver->poll();
    }

    delete driver;
    if (blkdev != NULL)
        delete blkdev;
    if (netdev != NULL)
        delete netdev;

    return tsi.exit_code();
}
