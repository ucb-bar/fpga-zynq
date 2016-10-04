#include <vpi_user.h>
#include <svdpi.h>
#include <fesvr/sai.h>

sai_t *sai = NULL;

std::vector<std::string> build_sai_args(int argc, char **argv)
{
    std::vector<std::string> out;

    for (int i = 1; i < argc; i++)
        out.push_back(argv[i]);

    return out;
}

extern "C" int serial_tick(
        unsigned char out_valid,
        unsigned char *out_ready,
        unsigned int  out_bits,

        unsigned char *in_valid,
        unsigned char in_ready,
        unsigned int  *in_bits)
{
    if (!sai) {
        s_vpi_vlog_info info;
        if (!vpi_get_vlog_info(&info))
          abort();
        sai = new sai_t(build_sai_args(info.argc, info.argv));
    }

    *out_ready = true;
    if (out_valid) {
        sai->send_word(out_bits);
    }

    *in_valid = sai->data_available();
    if (*in_valid && in_ready) {
        *in_bits = sai->recv_word();
    }

    sai->switch_to_host();

    return sai->done() ? (sai->exit_code() << 1 | 1) : 0;
}
