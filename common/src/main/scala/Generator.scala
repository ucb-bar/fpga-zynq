package zynq

object Generator extends testchipip.GeneratorApp {
  override lazy val longName = names.topModuleClass + "." + names.configs
  generateFirrtl
}
