#include "zynq_sai_driver.h"
#include "fesvr/sai.h"
#include <vector>

int main(int argc, char** argv)
{
  zynq_sai_driver_t sai_driver;
  sai_t sai(std::vector<std::string>(argv + 1, argv + argc));

  while(!sai.done()){
    sai_driver.poll(&sai);
  }
  return sai.exit_code();
}
