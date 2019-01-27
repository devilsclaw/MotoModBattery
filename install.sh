#!/bin/bash
adb connect "${1}:5555"
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p "com.devilsclaw.motomodbattery" -c android.intent.category.LAUNCHER 1
