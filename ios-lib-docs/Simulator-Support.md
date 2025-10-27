![Apple Logo](https://user-images.githubusercontent.com/2717016/70519799-d42e1900-1b3c-11ea-8966-4a6064fcb8e9.png)

# Simulator Support

**Note:** As of `April 6th, 2020`, these instructions are no longer valid. The new XCFramework bundle will run in the simulator by default, no configuration needed.

***

The `flic2lib.framework` itself does not contain slices (binaries) for x86 and i386 (iOS Simulator). The reason for this is that Apple does not allow frameworks to be submitted to App Store if they contain such slices (so called Fat Binaries). This means that if we were to include those slices to the framework then they would have to be removed again before App Store submission by adding a build script to the project's Build Phases.

Instead of forcing all our users to do this, we opted to use a different approach. In this repository you will find a folder called `simulator`. This folder contains two files, `FLICManagerSimulator.m` and `FLICButtonSimulator.m`. 

These files contains empty implementations of the FLICManager and FLICButton interfaces that will be included when Xcode is building for x86 and i386. When your application loads on the simulator, it will not load a framework binary (since the slice does not exist), but instead use the implementations found in those files.

So, for those who want to run the the app in the simulator, all you need to do is to include the files inside the `simulator` folder to your project. This would be `FLICManagerSimulator.m` and `FLICButtonSimulator.m`. You do not need to import any additional header files to make this work. Just make sure that the `.m` files are listed under `Compile Sources` in your project's `Build Phases`:

![Build Phases](https://user-images.githubusercontent.com/2717016/73062700-46647e00-3e9d-11ea-99d4-11f03653e4a8.png)

The implementations within those files are wrapped within `#if TARGET_OS_SIMULATOR` macros, so there is no risk of accidentally adding duplicate implementations on actual iOS devices.

Please note that you will get a warning while building for the simulator that lets you know that the flic2lib.framework will be ignored:

![Ignoring flic2lib in simulator](https://user-images.githubusercontent.com/2717016/71010533-4b8b1c00-20ec-11ea-8eba-39ef1240670b.png)

This is expected behavior.

**Note:** If you are importing these files into a Swift application then you should **not** create a bridging header since all the headers are already contained within the framework.

***