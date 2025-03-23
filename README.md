# 🚀 JetBrains IDE Git Activity Logger

## 📌 Description

JetBrains IDE Git Activity Logger is a plugin for JetBrains IDE that tracks the time spent in the active IDE window and records the current Git branch. It helps analyze productivity across different branches and monitor actual development time. Analyze it separately as you wish.

## ✨ Features

- ⏱️ **Automatic IDE activity logging every 5 minutes.**
- 🔀 **Captures the current Git branch.**
- 📂 **Saves data to a csv log.**
- 🏃 **Runs in the background without user intervention.**

## 📜 Log Format

Plugin working dir: **%user_folder%/.git-activity-logger/**

File name template is **%YEAR%.%MONTH%.csv**

Every monthly csv contains:

- 🆔 `project` — Name of working project with.
- 🕒 `datetime` — ISO date in UTC.
- 🌿 `branch` — Name of the active Git branch.

Example entry:
File 
```csv
project, datetime, branch
```

## 🛠️ Build

Firstly, check build.gradle.kts for clarify information about your version of IDE.

```
version.set("2023.3") // IDE version
type.set("IU") // Platform code. IU - WebStorm and etc. IC - IDEA Community
```

Run build
```
./gradlew buildPlugin
```