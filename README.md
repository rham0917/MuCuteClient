# MuCuteClient 🚀

<img src="logo.png" alt="Logo" width="30%" />

MuCuteRelay is a powerful and efficient implementation for Android, specifically tailored for Minecraft Bedrock Edition. Our goal is to enhance your gaming experience while maintaining the integrity of the original client. 🎮✨

## Notice
MuCuteClient is developed independently and does not utilize any code from protohax. Any attempts to take down this repository based on this claim will not be entertained. We value originality and innovation in our development process. 🚫

## Features
1. **No Modifications to the Minecraft Client**: Our solution ensures that the original client remains untouched, providing a safe and reliable gaming experience. 🙌
2. **Seamless Version Management**: Effortlessly switch and adapt between multiple versions without hassle, allowing you to enjoy the latest features and updates. 🌈
3. **Comprehensive Packet Layer Control**: Gain full control over the packet layer for enhanced gameplay, enabling you to customize your experience to your liking. 🔥
4. **Reliable Connectivity**: Enjoy a smooth experience with minimal connection issues, ensuring that your gameplay is uninterrupted and enjoyable. 🚀
5. **User-Friendly Interface**: Designed with the user in mind, our interface is intuitive and easy to navigate, making it accessible for players of all skill levels. 🖥️
6. **Active Community Support**: Join a vibrant community of players and developers who are eager to help and share their experiences. 🤝

## App Screenshots 📸
Here are some screenshots of the app in action:

![Screenshot 1](https://i.postimg.cc/SN71ZKyN/image.png)
*Screenshot of the main interface.*

![Screenshot 2](https://i.postimg.cc/7h19QBrQ/image.png)
*Screenshot showcasing features in use.*

![image.png](https://i.postimg.cc/3xjhFhqg/image.png)
*Screenshot showcasing Homepage of App.*

## Issues 🤔
If you encounter any bugs or have suggestions for features, please reach out through the following channels:
- Connect with us on [QQ](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=0teULf5spCrBeznoVuQPZzaqvB1QdlLJ&authKey=MjXaE2ZlTEDqF2OZJx4BwgXvs8epkgpsxrXX3WDQ42YYZPdS%2BxiYKK7xJqwQ6%2FpS&noverify=0&group_code=542992134) for ongoing discussions. 💬  
- Join our [Telegram](https://t.me/mucuteclient) channel for updates and community interaction. 📱

## Project Structure


```
📦 
├─ .gitattributes
├─ .gitignore
├─ LICENSE
├─ README.md
├─ app
│  ├─ .gitignore
│  ├─ build.gradle.kts
│  ├─ libs
│  │  └─ MuCuteRelay.jar
│  ├─ proguard-rules.pro
│  ├─ release
│  │  ├─ baselineProfiles
│  │  │  ├─ 0
│  │  │  │  └─ app-release.dm
│  │  │  └─ 1
│  │  │     └─ app-release.dm
│  │  └─ output-metadata.json
│  └─ src
│     └─ main
│        ├─ AndroidManifest.xml
│        ├─ java
│        │  └─ com
│        │     └─ mucheng
│        │        └─ mucute
│        │           └─ client
│        │              ├─ activity
│        │              │  ├─ CrashHandlerActivity.kt
│        │              │  └─ MainActivity.kt
│        │              ├─ application
│        │              │  └─ AppContext.kt
│        │              ├─ game
│        │              │  ├─ ComposedPacketHandler.kt
│        │              │  ├─ Module.kt
│        │              │  ├─ ModuleCategory.kt
│        │              │  ├─ ModuleContent.kt
│        │              │  ├─ ModuleManager.kt
│        │              │  ├─ ModuleValues.kt
│        │              │  ├─ TranslationManager.kt
│        │              │  ├─ data
│        │              │  │  └─ Effect.kt
│        │              │  ├─ entity
│        │              │  │  ├─ Entity.kt
│        │              │  │  ├─ LocalPlayer.kt
│        │              │  │  └─ Player.kt
│        │              │  └─ module
│        │              │     ├─ Misc
│        │              │     │  ├─ HasteModule.kt
│        │              │     │  └─ NoClipModule.kt
│        │              │     ├─ Visual
│        │              │     │  ├─ NightVisionModule.kt
│        │              │     │  └─ ZoomModule.kt
│        │              │     ├─ motion
│        │              │     │  ├─ AirJumpModule.kt
│        │              │     │  ├─ FlyModule.kt
│        │              │     │  ├─ HighJumpModule.kt
│        │              │     │  ├─ JetPackModule.kt
│        │              │     │  ├─ LevitationModule.kt
│        │              │     │  ├─ SlowFallModule.kt
│        │              │     │  └─ SpeedHackModule.kt
│        │              │     └─ player
│        │              │        ├─ AntiKnockbackModule.kt
│        │              │        └─ PoseidonModule.kt
│        │              ├─ logging
│        │              │  └─ LoggingPacketHandler.kt
│        │              ├─ model
│        │              │  ├─ CaptureModeModel.kt
│        │              │  └─ GameSettingsModel.kt
│        │              ├─ navigation
│        │              │  └─ Navigation.kt
│        │              ├─ overlay
│        │              │  ├─ OverlayButton.kt
│        │              │  ├─ OverlayClickGUI.kt
│        │              │  ├─ OverlayLifecycleOwner.kt
│        │              │  ├─ OverlayManager.kt
│        │              │  ├─ OverlayShortcutButton.kt
│        │              │  └─ OverlayWindow.kt
│        │              ├─ router
│        │              │  └─ main
│        │              │     ├─ AboutPage.kt
│        │              │     ├─ AccountPage.kt
│        │              │     ├─ HomePage.kt
│        │              │     ├─ MainScreen.kt
│        │              │     └─ SettingsPage.kt
│        │              ├─ service
│        │              │  └─ MuCuteRelayService.kt
│        │              ├─ ui
│        │              │  ├─ component
│        │              │  │  └─ NavigationRailX.kt
│        │              │  └─ theme
│        │              │     └─ Theme.kt
│        │              ├─ util
│        │              │  ├─ MinecraftUtils.kt
│        │              │  ├─ SnackBarUtils.kt
│        │              │  ├─ TranslationUtil.kt
│        │              │  ├─ WindowUtil.kt
│        │              │  └─ WorkModes.kt
│        │              └─ viewmodel
│        │                 └─ MainScreenViewModel.kt
│        └─ res
│           ├─ drawable
│           │  ├─ falling.xml
│           │  ├─ ic_launcher_background.xml
│           │  ├─ ic_launcher_foreground.xml
│           │  ├─ manufacturing_24px.xml
│           │  ├─ sprint_24px.xml
│           │  ├─ swords_24px.xml
│           │  ├─ toc_24px.xml
│           │  └─ view_in_ar_24px.xml
│           ├─ mipmap-anydpi-v26
│           │  ├─ ic_launcher.xml
│           │  └─ ic_launcher_round.xml
│           ├─ mipmap-hdpi
│           │  ├─ ic_launcher.webp
│           │  ├─ ic_launcher_round.webp
│           │  └─ minecraft_icon.png
│           ├─ mipmap-mdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-xhdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-xxhdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ mipmap-xxxhdpi
│           │  ├─ ic_launcher.webp
│           │  └─ ic_launcher_round.webp
│           ├─ values-v27
│           │  └─ themes.xml
│           ├─ values-zh
│           │  └─ strings.xml
│           ├─ values
│           │  ├─ colors.xml
│           │  ├─ strings.xml
│           │  └─ themes.xml
│           └─ xml
│              └─ network_security_config.xml
├─ build.gradle.kts
├─ buildKey.jks
├─ gradle.properties
├─ gradle
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradlew
├─ gradlew.bat
├─ logo.png
└─ settings.gradle.kts
```
©generated by [Project Tree Generator](https://woochanleee.github.io/project-tree-generator)

## License 📝
This project is licensed under the [Mozilla Public License v2.0]. This license applies solely to the source code within this repository. Additional code utilized during development may not fall under this license.

### Key Points of the License:
*Actions you are permitted to do:*
- **Use**: Feel free to use the code for personal or commercial projects.
- **Share**: Share the code with others while respecting the license terms.
- **Modify**: Customize the code to fit your needs.

*If you choose to use any code from the source:*
- **You must disclose the source code of your modified work if you distribute it. All modifications must be made available under the same terms.** 🔑
- **You may integrate this code with proprietary code, but ensure that MPL-licensed components remain open and accessible. Clearly separate MPL components from proprietary code.** 🔍
- **Include a copy of the MPL with any distribution of your modified code.** 📄

## Platform-specific Implementations
MuCuteRelay is designed exclusively for **ANDROID**. 📱💥

## Setting Up the Workspace in Android Studio 🛠️

To set up MuCuteRelay in Android Studio, please follow these detailed steps:

1. **Install Android Studio**: 
   - Download and install Android Studio from the [official website](https://developer.android.com/studio).

2. **Clone the Repository**: 
   - Open Android Studio and select **Get from Version Control**.
   - Enter the repository URL: `https://github.com/CaiMuCheng/MuCuteRelay` and click **Clone**.

3. **Open the Project**: 
   - Once cloned, Android Studio will prompt you to open the project. Click **Open**.

4. **Sync Gradle**: 
   - After opening the project, Android Studio may prompt you to sync Gradle. Click on **Sync Now** to ensure all dependencies are downloaded.

5. **Locate the `publishAppToMavenLocal` Task**: 
   - In the right sidebar, open the **Gradle** tab.
   - Navigate to `Tasks` > `build` and locate the `publishAppToMavenLocal` task.

6. **Publish the Application**: 
   - Right-click on the `publishAppToMavenLocal` task and select **Run**. This will publish the application locally.

7. **Run the Application**: 
   - Connect your Android device or start an emulator.
   - Click on the **Run** button (green play icon) in the toolbar to build and run the application.

8. **Enjoy MuCuteRelay**: 
   - Once the app is installed on your device or emulator, you can start using MuCuteRelay! 🚀

## Contributing 🤝
We welcome contributions to MuCuteRelay! If you wish to get involved, please fork or clone the repository, make your changes, and submit a pull request. Your contributions are greatly appreciated and help us improve the project for everyone! 🙌

## Disclaimer ⚠️
Use MuCuteRelay at your own risk. We **DO NOT** take responsibility for any bans or penalties that may arise from using this tool. Any losses incurred are solely your responsibility. Please use responsibly and enjoy the game! 🎮💔


## Installation Tutorial for APKs 📦

To install the APK for MuCuteRelay, please follow these steps:

1. **Download the APK**: Go to the [Releases](https://github.com/CaiMuCheng/MuCuteClient/releases) tab on our GitHub repository and download the latest APK file.

2. **Enable Unknown Sources**: 
   - On your Android device, go to **Settings** > **Security**.
   - Enable **Unknown Sources** to allow installations from sources other than the Google Play Store.

3. **Locate the APK**: Use a file manager app to navigate to the folder where you downloaded the APK file.

4. **Install the APK**: Tap on the APK file and follow the on-screen instructions to install the application.

5. **Launch the App**: Once installed, you can find MuCuteRelay in your app drawer. Tap to launch and enjoy!

**Note**: Ensure that you have sufficient storage space and that your device meets the minimum requirements for running the app. 


## Social Links 🌐
- Connect with us on [QQ](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=0teULf5spCrBeznoVuQPZzaqvB1QdlLJ&authKey=MjXaE2ZlTEDqF2OZJx4BwgXvs8epkgpsxrXX3WDQ42YYZPdS%2BxiYKK7xJqwQ6%2FpS&noverify=0&group_code=542992134) for ongoing discussions. 💬  
- Join our [Telegram](https://t.me/mucuteclient) channel for updates and community interaction. 📱

## Badges
![License](https://img.shields.io/badge/license-MPL%202.0-brightgreen.svg) ![GitHub issues](https://img.shields.io/github/issues/CaiMuCheng/MuCuteRelay.svg) ![GitHub stars](https://img.shields.io/github/stars/CaiMuCheng/MuCuteRelay.svg)

![.](https://i.ibb.co/5hn5Fh5h/1000073558-removebg-preview.png)
*Screenshot of the main interface.*
