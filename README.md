# University of Bouira Mobile Application

![Demo](screenshots/univ-bouira-logo.gif)

## Overview

The University of Bouira Mobile Application is a comprehensive platform designed to connect students, professors, and administrative staff at the University of Bouira. This Android application streamlines academic processes, enhances communication, and provides essential tools for the university community.

**Note:** This application was developed as a graduation project for a Bachelor's degree program at the University of Bouira.

## Features

### For Students
- **Course Registration**: Register for courses each semester with real-time availability updates
- **Class Schedules**: Access personalized class timetables with location information
- **Grade Tracker**: View grades and academic progress
- **Announcements**: Receive important notifications from administration and professors
- **Campus Map**: Navigate the university campus with interactive maps
- **Document Access**: Download lecture materials, syllabi, and academic resources

### For Professors
- **Course Management**: Create and manage course content and materials
- **Attendance Tracking**: Record and monitor student attendance
- **Grade Management**: Input and update student grades
- **Communication Tools**: Send announcements and messages to students
- **Office Hours**: Schedule and manage office hours and appointments
- **Resource Sharing**: Upload course materials and resources

### For Administration
- **Announcement Distribution**: Broadcast important messages to the university community
- **Event Management**: Create and promote university events
- **Academic Calendar**: Manage and update the academic calendar
- **Data Analytics**: Access statistics about student performance and attendance

## Screenshots

(TODO : Add app Screenshots)

## Technology Stack

- **Frontend**: Native Android (Kotlin/Java)
- **Database**: PostgreSQL
- **Push Notifications**: Firebase Cloud Messaging
- **Analytics**: Firebase Analytics

## Prerequisites
- Android 6.0 (Marshmallow) or higher
- Active internet connection
- University of Bouira email address for authentication


### Student Access
1. Log in with your student credentials
2. Access your personalized dashboard
3. View your schedule, announcements, and course materials
4. Register for courses during the registration period

### Professor Access
1. Log in with your faculty credentials
2. Manage your courses from the dashboard
3. Update grades, attendance, and course materials
4. Communicate with your students through announcements

## Development

### Setup Development Environment
1. Clone the repository
```bash
git clone https://github.com/AmaraSami/univ-bouira-app.git
```
2. Open the project in Android Studio
3. Sync Gradle files and install dependencies
4. Configure your local environment variables for API access

### Project Structure
```
univ-bouira-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/           # Kotlin/Java source files
│   │   │   ├── res/            # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── test/               # Test files
│   ├── build.gradle            # App-level build configuration
│   └── proguard-rules.pro      # ProGuard rules
├── gradle/                     # Gradle wrapper
├── build.gradle                # Project-level build configuration
└── README.md                   # Project documentation
```

### Contributors and there Contacts:
- Amara Sami Anis - samianis.amara@univ-bouira.dz
- Meziane Akram   - akram.meziane@univ-bouira.dz
- Rezouali Raouf  - raouf.rezouali@univ-bouira.dz

## Contact and Support
For assistance with the application:

- **Technical Support**:  samianis.amara@univ-bouira.dz
- **Bug Reports**: Please submit via the GitHub Issues tab

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- University of Bouira IT Department
- Student Technology Advisory Committee
- Faculty Technology Committee
- All beta testers and early adopters
