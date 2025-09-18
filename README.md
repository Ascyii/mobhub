# Mobile hub for my android device

Inspired from https://github.com/czak/minimal-android-project/tree/master.
Must setup android home variable and install an sdk with the manager then just run `gradle installDebug` with devices connected.

## Preparation

Install the sdk with `sdkmanager --install "platforms;android-34"` and then accept the licenses with `sdkmanager --licenses`.
Also install the platform tools with `sdkmanager "platform-tools"`.

## Development

To apply the changes run
`gradle installDebug && adb shell am start -n de.ascyii.mobhub/.MainActivity`
