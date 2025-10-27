![Apple Logo](https://user-images.githubusercontent.com/2717016/70519799-d42e1900-1b3c-11ea-8966-4a6064fcb8e9.png)

# OS Compatibility

The `flic2lib.framework` only supports iOS 12 and above.

However, to make it easier for developers, the framework's Deployment Target is set to iOS 9.0. This means that you will be able to load the framework without any issues on all devices that support iOS 9 and above. Slices for both arm64 and armv7 are included in the fat binary.

It was made like this so that developers would be able to add the framework to applications with lower deployment targets than iOS 12 without having to manually keep track of when it is safe to load and use.

Instead, if you try to configure the `FLICManager` on a device that is not supported, then the manager state will immediately switch to `FLICManagerStateUnsupported`. So it will fail safely.

It is recommended that you handle this case in the `manager:didUpdateSate:` callback.

***