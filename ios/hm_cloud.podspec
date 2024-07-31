#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint hm_cloud.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'hm_cloud'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin project.'
  s.description      = <<-DESC
A new Flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.resource              = 'HaiMaSDK/HaiMaSDK_Bundle_iOS.bundle'
  s.resource_bundles = {
      'SanA_Game' => ['Classes/**/*.{xib,png,xcassets,json,plist}']
    }
  s.vendored_frameworks   = 'HaiMaSDK/HMCloudPlayerCore.framework', 'HaiMaSDK/HMWebRTC.framework'
  s.dependency 'Flutter'
  s.dependency 'Masonry', '~> 1.1.0'
  s.dependency 'MJExtension', '~> 3.4.2'
  s.dependency 'AFNetworking', '~> 4.0'
  s.platform = :ios, '13.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
end
