# ezSchedule

A simple Android app for saving schedule images by month.

## What it does

- One main scrollable page only.
- Year section: 2026.
- Month sections from January to December.
- Floating plus button to add a schedule.
- Add Schedule modal lets you choose a month and select a local image file.
- Saved images appear as preview cards under the chosen month.
- Tap a preview to open the schedule in full screen.
- No team, calendar, settings, bottom navigation, dashboard features, statuses, or weekly-hours labels.

## Build the APK without a PC

This project includes a GitHub Actions workflow that can build the APK in the cloud.

1. Create a new GitHub repository from your phone.
2. Upload these project files to the repository.
3. Open the repository on GitHub.
4. Go to **Actions**.
5. Select **Build Android APK**.
6. Tap **Run workflow**.
7. When it finishes, open the finished run and download **ezSchedule-debug-apk**.
8. Open the ZIP on your phone and install `app-debug.apk`.

You may need to allow installing unknown apps on your Samsung phone. After installing, turn that permission off again for safety.
