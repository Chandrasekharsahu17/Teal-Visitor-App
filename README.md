# TEAL Expo Visitor Capture — Web + Android v2.3

A cross-platform visitor registration app for TEAL exhibition and expo teams. Open it in Chrome, Safari or another modern browser on Android, iPhone, iPad or desktop. No app-store installation is required.

## Open the web app

After GitHub Pages deployment completes, open: `https://chandrasekharsahu17.github.io/Teal-Visitor-App/`.

- **Android:** open the link in Chrome. Use the browser menu → Install app / Add to Home screen if desired.
- **iPhone/iPad:** open the link in Safari → Share → Add to Home Screen.
- Visitor records remain in the browser storage on that device. They are **not synced or shared** between different people/devices; export Excel or JSON backups and transfer them securely when needed.
- The web app shell can be cached for repeat/offline use. First-time browser OCR downloads the OCR engine and English language data; card text recognition runs in the browser on the device. The Android APK continues to use native on-device OCR.
- Camera access requires HTTPS and browser permission. Private browsing or clearing site data can remove locally stored records; create backups regularly.

## Visitor-capture flow

1. **Home dashboard** — visitor totals, today's registrations, photo count, and quick navigation.
2. **Business-card capture** — take a photo or select an image and run on-device ML Kit text recognition.
3. **Contact details** — review the suggested name, company, designation and phone. These four fields are required.
4. **Notes and follow-up** — alternate phone, email addresses, website, LinkedIn/profile, address, representative, meeting notes and visitor photo. Areas of Interest has been removed.
5. **Review and save** — confirm the full record before storing it. The app shows a saved confirmation.
6. **Saved visitors** — search, view full details, edit or delete records.
7. **Export and settings** — export Excel, create/restore JSON backups, and set a default TEAL representative.

## OCR and data preservation

Text recognition is processed on the phone; no OCR service or web API is called. OCR is not infallible, so the app shows the extracted text and highlights fields it suggests. **Verify suggested values before saving.** The complete recognized text is stored with the record and included in the Excel and JSON exports, so text that the field parser does not classify is not discarded. A photo is also stored when one is captured.

The app can suggest a person's name, company, designation, phone numbers, email addresses, website, LinkedIn/profile URL and a labelled address when these are readable. Accuracy depends on card quality, layout, language, glare and OCR quality; manual review remains necessary.

## Local data and exports

- Records use the local IndexedDB database in the bundled Android WebView origin.
- No backend, cloud sync, third-party analytics or runtime internet service is included.
- Visitor and business-card photos are stored locally.
- Excel export includes visitor details, all optional contact fields, meeting notes, original OCR text and embedded images. There is no Areas of Interest column.
- JSON backup/import carries the full record fields and photos. Keep a backup before reinstalling the app or changing phones.
- On Android 10 and newer, exports are written under **Downloads / TEAL Expo**.

## Source of truth

The packaged app's UI is **`app/src/main/assets/index.html`**. The Java Android bridge is **`app/src/main/java/com/teal/expo/MainActivity.java`**. The root `index.html` and `TEAL_Expo_Visitor_App_V2.html` are synchronized previews of the packaged UI; offline OCR itself requires the installed Android APK.

## Build an APK

1. Open the GitHub repository's **Actions** tab.
2. Open the latest **Build TEAL Expo APK** run. Pushes to app/build files trigger it automatically; manual runs are also available.
3. Wait until the run shows a green **Success** status.
4. Under **Artifacts**, download `TEAL-Expo-Visitor-Capture-APK`.
5. Extract the downloaded ZIP and install `app-debug.apk` on an Android phone.

The APK is a sideloadable debug build, not a Play Store release. The workflow keeps a stable debug signing key in the GitHub Actions cache so future builds can normally update the installed app without changing its signature.

## Expo-day acceptance checklist

Before collecting real visitor data, test all of the following on the exact phone that will be used:

- Capture a card in camera mode and choose a card image from Gallery.
- Test cards with multiple emails, phone numbers, a website and an address; compare suggested fields with the original OCR text.
- Save a record with all required fields and a record without optional fields.
- Add a visitor photo, then verify both photos and OCR text in the visitor details.
- Close and reopen the app; confirm records remain.
- Edit a saved record and confirm changes persist.
- Export Excel and open it in a spreadsheet app on a computer; inspect every contact column and both embedded-photo columns.
- Create a JSON backup, import it, and verify that fields and photos are preserved.
- Turn off internet and repeat scanning, saving, search and export.
- Keep a separate backup outside the phone before the event.

The GitHub Actions build verifies that Gradle produces the APK. It does not replace real-device testing of the camera/gallery flows, local storage, Excel workbook and OCR quality.
