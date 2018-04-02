# Net-computing
## Project: bicycle racks

### How to run:
Our system consists of three parts: the server, the racks and the client (app). To use the system, you need to have RabbitMQ running on a system, and if you're using multiple machines, you need to create an account in RabbitMQ that can be used by the server and racks.

#### Starting the server:
_Make sure that nodejs, nodejs-legacy and npm are installed before continuing._
To start the server, go to the nodejs directory and install the required dependencies, using the following command:
```{bash}
npm install
```
(this only needs to be done once).

You should now be able to start the server using the following command:
```{bash}
node server
```

After starting the server, you can put in the credentials and ip-address of the RabbitMQ server. You can also press enter to use the default values (localhost, guest account).

#### Starting a bicycle rack
_Make sure that you have maven installed before continuing._
To start a bicycle rack, you should first compile the code and make the program executable. To do so, go to the java/bicycleStand directory and use:
```{bash}
mvn install
cd target
chmod +x ./bicycleStand-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

You can now run a rack by going into the target directory and running either `./bicycleStand-0.0.1-SNAPSHOT-jar-with-dependencies.jar` for a graphical interface, or `bicycleStand-0.0.1-SNAPSHOT-jar-with-dependencies.jar --cli` for a command line interface (recommended).

#### Running the client (app)
_Make sure that you have the android sdk installed, and that the environment variables are set correctly before continuing._

##### Setting the debug server
Please put the computer name of the computer running the nodejs server in the strings.xml file, in the field named "debug_ip". This should not be necessary in a production version, but unfortunately is now because the server doesn't have a domain name.

##### Creating an unsigned apk file
To create an apk file, please go into the app directory and run:
```{bash}
./gradlew assembleDebug
```
The apk file should have been created and should be located at 'app/build/outputs/apk/debug/app-debug.apk'.

##### Running the client
Install the client on your phone (might require developer mode), and run it from there.
