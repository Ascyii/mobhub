default:
    @just --list

package := "de.ascyii.mobhub"
activity := ".MainActivity"

export ANDROID_HOME := "/home/jonas/Android"

build:
    @echo "Installing onto device"
    gradle installDebug
    adb shell am start -n {{package}}/{{activity}}

setup:
	sdkmanager --install "platform-tools" "platforms;android-34"
	sdkmanager --licenses
