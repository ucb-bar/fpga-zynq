#include "zynq_tsi_driver.h"
#include "fesvr/tsi.h"
#include <vector>

int main(int argc, char** argv)
{
  zynq_tsi_driver_t tsi_driver;
  tsi_t tsi(std::vector<std::string>(argv + 1, argv + argc));

  while(!tsi.done()){
    tsi_driver.poll(&tsi);
  }
  return tsi.exit_code();
}
