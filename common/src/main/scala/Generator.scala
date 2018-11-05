package zynq

import freechips.rocketchip.util.GeneratorApp

object Generator extends GeneratorApp {
  val longName = names.topModuleClass + "." + names.configs
  generateFirrtl
}
