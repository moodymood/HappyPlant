# Happy Plant
Happy Plant is an Android project that uses Phidget hardware to monitor the environmental status of a plant.
It consists of two parts:

* Android Service - The service connects to the Phidgets and logs data periodically whilst running. This data is saved to a SQLite database on the device.
* UI - The UI is used to start and stop the serice as well as to display the health of the plant.

##Running
The project runs in Android studio and uses Gradle build scripts.

##Testing
Additionally the project has tests for both the UI and service allowing mocking of data from sensors to be created.
