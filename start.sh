#!/bin/bash

# allow any users on the localhost to use our xsession
# this is needed for letting root open the gui window
# (if you will be using the gui option)
xhost +localhost

# this kernal module is for the pi's camera
# it should already be installed with raspbian
# we need it to let opencv find the camera
sudo modprobe bcm2835-v4l2

# and now we start the program
opencvlib='/home/pi/Downloads/opencv-3.0.0/build/lib'
classpath='build/classes:lib/*:/opt/pi4j/lib/*:.'

echo sudo java -Djava.library.path="$opencvlib" -cp "$classpath" org.ccfls.counter.CVCounter -p childrens

sudo java -Djava.library.path="$opencvlib" -cp "$classpath" org.ccfls.counter.CVCounter -p childrens
