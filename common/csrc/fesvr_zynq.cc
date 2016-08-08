#include "zynq_dtm_driver.h"
#include "fesvr/dtm.h"
#include <vector>

int main(int argc, char** argv)
{
  zynq_dtm_driver_t dtm_driver;
  dtm_t dtm(std::vector<std::string>(argv + 1, argv + argc));

  while(!dtm.done()){
    dtm_driver.poll(&dtm);
  }
  return dtm.exit_code();
}
