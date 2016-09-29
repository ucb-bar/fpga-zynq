#include <vpi_user.h>
#include <svdpi.h>

extern "C" int serial_tick(
        unsigned char *out_valid,
        unsigned char out_ready,
        unsigned int  *out_bits,

        unsigned char in_valid,
        unsigned char *in_ready,
        unsigned int  in_bits)
{
    // TODO: Implement the protocol
    return 0;
}

