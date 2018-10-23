package zynq

import freechips.rocketchip.util.{GeneratorApp}


object Generator extends GeneratorApp {
  override lazy val longName = names.topModuleClass + "." + names.configs
  generateFirrtl
}
