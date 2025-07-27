ActivityAnnouncement-CS102-SummerProject

This project is a desktop application designed to help Bilkent University clubs announce their events to students. It is developed using Java, JavaFX, and Firebase technologies.

------------------------------------------------------------
 Setup

1. Clone this repository:

   git clone https://github.com/n-utku-n/ActivityAnnouncement-CS102-SummerProject.git
   cd ActivityAnnouncement-CS102-SummerProject

2. Install the required tools:
   - JDK 17+
   - Maven
   - JavaFX SDK
   - Firebase Admin SDK (for backend use)

------------------------------------------------------------
 Firebase Integration

To run the project, you need a `serviceAccountKey.json` file.

Please follow these steps:

1. Go to [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Navigate to "Project Settings" → "Service Accounts" tab.
3. Click “Generate new private key”.
4. Download the `serviceAccountKey.json` file.
5. Place the file in the following directory:

   project1/src/main/resources/firebase/serviceAccountKey.json

    # OR

   Copy the file that Utku shared with you with the specified name to the specified location (you can request it again if you think there is a problem):
      project1/src/main/resources/firebase/serviceAccountKey.json

Note: This file is listed in `.gitignore` and **must never be uploaded to GitHub**.

------------------------------------------------------------
 WARNING: If you cloned this repo earlier...

This repository previously included a private Firebase key (`serviceAccountKey.json`) in its history.  
The history has been fully rewritten, but if you have an old copy:

To avoid security issues:

   git fetch origin
   git reset --hard origin/main

OR, the safest way:

   Delete the old folder and re-clone the repository:

   git clone https://github.com/n-utku-n/ActivityAnnouncement-CS102-SummerProject.git

------------------------------------------------------------
 Contributing

All contributions are welcome! Please open an issue before submitting a pull request.

------------------------------------------------------------
 Security

GitHub Secret Scanning is enabled for this repository. Commits containing sensitive information will be automatically flagged.

------------------------------------------------------------
 License

None
