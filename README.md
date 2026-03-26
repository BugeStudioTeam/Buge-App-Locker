# Buge App Locker

<p align="center">
  <img src="https://raw.githubusercontent.com/BugeStudioTeam/Buge-App-Locker/refs/heads/main/icon.jpg" alt="Buge App Locker Icon" width="120"/>
</p>

[![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Material Design 3](https://img.shields.io/badge/Material%20Design-3-757575?logo=material-design&logoColor=white)](https://m3.material.io)

Buge App Locker is a lightweight Android application locker that operates using **Accessibility Service**. Designed with **Material Design 3**, it provides a seamless and secure way to protect sensitive apps from unauthorized access.

## Features

### 🔒 Core Capabilities
- **Accessibility Service Based** – Runs without root, leveraging Android's Accessibility Service to detect app launches and trigger lock screens.
- **App Locking** – Selectively lock any installed app (both user and system apps) with a simple toggle.
- **Material Design 3 UI** – Clean, modern interface that adapts to your device's system theme.

### 🔐 Lock Methods
- Pattern lock
- PIN code protection
- (Optional) Biometric authentication support

### 🛡️ Privacy & Security
- Hide app lock notifications for stealth protection
- Auto-lock on app switch or screen off
- Secure lock setup to prevent unauthorized changes

## How It Works

1. **Enable Accessibility** – Grant Accessibility Service permission to Buge App Locker.
2. **Select Apps** – Choose which apps you want to lock from the app list.
3. **Set Lock Method** – Configure your preferred unlock method (pattern, PIN, etc.).
4. **Auto Protection** – When a locked app is opened, the locker intercepts and prompts for authentication.

## Requirements

- Android device running **Android 8.0 (API 26) or higher**
- Accessibility Service permission enabled

## Installation

Download the latest APK from the [Releases](../../releases) page.

## Important Note

This app uses the **Accessibility Service API** solely for the purpose of detecting when a locked app is launched and displaying the lock screen. No user data is collected or transmitted.
