require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "Flic2Simulator"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/X-Guard/react-native-flic2.git", :tag => "#{s.version}" }

  install_modules_dependencies(s)

  s.source_files = "ios/Flic2SimulatorStub.{h,mm}"
  s.private_header_files = "ios/Flic2SimulatorStub.h"
end
