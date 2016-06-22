cv-counter
==========

A computer vision people counter for Raspberry Pi.

# Features
+ Submits data to a MySQL database.
+ Extensible to multiple doors and sites.
+ Automatic report generation.

# Installation
## Required
### Hardware
+ Raspberry Pi 2 (1 is too slow, others not tested)
+ Raspberry Pi camera module (other cameras possible with modifications)
+ Indicator LEDs (optional)
### Software
+ OpenCV Java (computer vision library)
+ mysql-connector Java (MySQL library)
+ pi4j (Raspberry Pi GPIO library, used for LED indicators)
## Process
### Camera Units
1. Connect camera module to Pi
2. Connect LED indicators, green to 03, orange to 04, red to 17. These may be changed in the Indicators.java file.
3. Install dependencies, opencv bins expected in /home/pi/Downloads/opencv-3.0.0/build/bin
4. Download and compile the Java version of OpenCV.
5. Clone the cv-counter repo to the Raspberry Pi.
6. Modify the start and nightly\_build scripts to point to the correct names and directories.
7. Configure the PeopleCounter.java class to point to the correct MySQL server address and password.
8. Add cron jobs to run these scrips, see the install.md file for an example.
9. Recommended, configure sshd and vncserver for remote maintenance.
10. Install the unit with the camera pointing towards where people will walk in and out.
11. Run CVCounter in visual mode to configure entry and exit zones.
12. Restart the Raspberry Pi.
### Server
To be continued...
