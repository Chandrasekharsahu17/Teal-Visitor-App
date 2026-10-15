# TEAL Expo Visitor Capture — Android Project

This project packages the offline visitor-capture app as an Android application. It includes two separate optional image fields: **visitor photo** and **visitor badge / business-card photo**. The images stay on the phone and are embedded in the exported Excel workbook. Exports are written to `Downloads/TEAL Expo` on Android 10 and newer.

## What is included

- Android app source (`app/src/main/java/com/teal/expo/MainActivity.java`)
- Fully offline HTML UI bundled as an Android asset
- Local browser database on the Android app's private WebView origin
- Native camera/gallery chooser for both image fields
- Excel (`.xlsx`) export with both photo columns and embedded pictures
- JSON backup/import stored in the phone Downloads folder
- GitHub Actions workflow that builds a debug APK artifact

## How to get an APK using GitHub Actions

1. Create a **private** repository on GitHub, for example `teal-expo-visitor-app`.
2. Extract this ZIP and upload all files/folders to the repository's `main` branch. Keep the `.github/workflows/build-apk.yml` file.
3. Open the repository's **Actions** tab and enable workflows if asked.
4. Select **Build TEAL Expo APK** and click **Run workflow**. If you just pushed the files to `main`, the workflow may have started automatically.
5. When the run completes successfully, open the run and download the artifact named `TEAL-Expo-Visitor-Capture-APK`.
6. Extract the artifact ZIP to find `app-debug.apk`, then transfer it to the Android phone and install it. Android may ask you to allow installation from that file manager.

The first build may take a few minutes because GitHub downloads the Android SDK and Gradle dependencies. This project does not require a backend or runtime internet access. GitHub is used only to compile the APK.

## Alternative: build locally with Android Studio

Open the folder in Android Studio with JDK 17 and Android SDK Platform 35 installed, let Gradle sync, then run **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Copy the resulting debug APK from `app/build/outputs/apk/debug/app-debug.apk`.

## Expo-day checklist

- Register 3 test visitors, with and without both images.
- Export Excel and open it on a computer to verify both photo columns and images.
- Create a JSON backup and test importing it.
- Restart the app and confirm visitors remain saved.
- Verify the camera chooser on the exact Android phone used at the booth.
- Export a backup at regular intervals and at event close.

## Important status note

The APK binary is **not included in this source ZIP**. The available build environment did not have Android SDK/Gradle installed and could not download them. The source and a GitHub Actions build workflow are included so a real APK can be compiled without having Android Studio installed locally. The APK still needs a successful build and testing on the target phone before the event.
