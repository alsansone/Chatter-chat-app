# Chatter

A multi-threaded command line implemented chat room utilizing Java

## Server

The Server can be started in an IDE or from the command line using:

        Java ServerMain

This will start the server on port 8191 and try and get a connection to the database. In order to utilize the JDBC code you will first need to install mySQL from here:

        https://dev.mysql.com/downloads/

You must make sure the Connector/J is installed as this is what connects mySQL to Java. To connect the two after mySQL has been downloaded you must add it to the class file. In IntelliJ this can be done by navigating to:

        File -> Project Structure -> Modules

Then click on the + icon select JARs or Directories and navigate to the mySQL Connector/J folder. The JDBC implementation includes the credentials of my own mySQL server so those will need to be replaced with
whomever runs the server. The JDBC class will create a database and a table if they don’t already exist as well as add some random users to the database. The users code will have to be commented out after the initial running otherwise a duplicate entry error occurs.

Now we sit back and wait for the clients to connect.
