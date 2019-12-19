require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-flic2"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-flic2
                   DESC
  s.homepage     = "https://github.com/X-Guard/react-native-flic2"
  s.license      = "MIT"
  s.authors      = { "X-Guard B.V." => "npm-packages@x-guard.nl" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/X-Guard/react-native-flic2.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end

