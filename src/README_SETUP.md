# Restaurant Operations Management System (ROMS)

## Environment Setup and Running Instructions

This document provides detailed instructions for setting up the development environment and running the Restaurant Operations Management System (ROMS) on both Windows and macOS.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [JavaFX SDK Installation](#javafx-sdk-installation)
3. [MySQL Installation](#mysql-installation)
4. [MySQL JDBC Driver](#mysql-jdbc-driver)
5. [Running the Application](#running-the-application)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

To run this application, you need:

- JDK 11 or higher
- JavaFX SDK 21.x
- MySQL 8.0 or higher
- MySQL JDBC Connector

## JavaFX SDK Installation

### Windows

1. Download the JavaFX SDK for Windows from [Gluon's website](https://gluonhq.com/products/javafx/)
2. Extract the downloaded zip file to a location of your choice (e.g., `C:\Program Files\javafx-sdk-21.0.6`)
3. Add the JavaFX bin directory to your PATH:
   - Go to Control Panel → System → Advanced System Settings → Environment Variables
   - Edit the PATH variable and add the path to the JavaFX bin directory

### macOS

1. Download the JavaFX SDK for macOS from [Gluon's website](https://gluonhq.com/products/javafx/)
2. Extract the downloaded zip file to a location of your choice (e.g., `/Users/yourusername/javafx-sdk-21.0.6`)

## MySQL Installation

### Windows

1. Download MySQL Installer from [MySQL's website](https://dev.mysql.com/downloads/installer/)
2. Run the installer and select "Developer Default" installation
3. Follow the installation wizard to set up MySQL Server
4. Make note of the root password you create during installation
5. Make sure the MySQL service is running

### macOS

1. Download MySQL from [MySQL's website](https://dev.mysql.com/downloads/mysql/)
2. Run the package installer
3. Note the temporary password provided after installation
4. Start MySQL from System Preferences or using the command:
   ```bash
   sudo /usr/local/mysql/support-files/mysql.server start
   ```

## MySQL JDBC Driver

### Adding to the Project

1. Download MySQL Connector/J (JDBC Driver) from [MySQL's website](https://dev.mysql.com/downloads/connector/j/)
2. Add the JAR file to your project's lib directory

## Database Setup

1. Open a terminal or command prompt
2. Log in to MySQL:
   ```bash
   mysql -u root -p
   ```
3. Create the restaurant database:
   ```sql
   CREATE DATABASE restaurant_db;
   USE restaurant_db;
   ```
4. Import the database schema:

   ```bash
   # Windows (Command Prompt)
   mysql -u root -p restaurant_db < path\to\restaurant_schema.sql

   # macOS (Terminal)
   mysql -u root -p restaurant_db < path/to/restaurant_schema.sql
   ```

## Running the Application

### Windows

#### Using Command Line

1. Open Command Prompt
2. Navigate to the project directory:
   ```
   cd path\to\ROMS-branch
   ```
3. Compile the Java files:
   ```
   mkdir -p bin
   javac --module-path "C:\path\to\javafx-sdk-21.0.6\lib" --add-modules javafx.controls,javafx.fxml -d bin src\ROMS\*.java
   ```
4. Copy the resources:
   ```
   mkdir -p bin\ROMS
   copy src\ROMS\*.fxml bin\ROMS\
   copy src\ROMS\*.css bin\ROMS\
   copy src\ROMS\*.sql bin\ROMS\
   xcopy /E images bin\images\
   ```
5. Run the application:
   ```
   java --module-path "C:\path\to\javafx-sdk-21.0.6\lib" --add-modules javafx.controls,javafx.fxml -cp bin;path\to\mysql-connector-j-8.0.33.jar App
   ```

#### Using the Script (run.bat)

1. Create a file named `run.bat` in the project root with the following content (adjust paths accordingly):

   ```bat
   @echo off

   rem Set the JavaFX path - update this with your actual JavaFX path
   set JAVAFX_PATH=C:\path\to\javafx-sdk-21.0.6

   rem Set the MySQL connector path
   set MYSQL_CONNECTOR=C:\path\to\mysql-connector-j-8.0.33.jar

   rem Create bin directory if it doesn't exist
   if not exist bin mkdir bin

   rem Compile all the Java files
   echo Compiling Java files...
   javac --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -d bin src\ROMS\*.java

   if %ERRORLEVEL% neq 0 (
       echo Compilation failed.
       exit /b 1
   )

   rem Copy all non-Java files to bin directory
   echo Copying resources...
   if not exist bin\ROMS mkdir bin\ROMS
   copy src\ROMS\*.fxml bin\ROMS\
   copy src\ROMS\*.css bin\ROMS\
   copy src\ROMS\*.sql bin\ROMS\

   if not exist bin\images mkdir bin\images
   xcopy /E images bin\images\

   rem Run the application
   echo Running the application...
   java --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -cp bin;%MYSQL_CONNECTOR% App
   ```

2. Run the script:
   ```
   run.bat
   ```

### macOS

#### Using Command Line

1. Open Terminal
2. Navigate to the project directory:
   ```bash
   cd /path/to/ROMS-branch
   ```
3. Compile the Java files:
   ```bash
   mkdir -p bin
   javac --module-path "/path/to/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.fxml -d bin src/ROMS/*.java
   ```
4. Copy the resources:
   ```bash
   mkdir -p bin/ROMS
   cp src/ROMS/*.fxml bin/ROMS/
   cp src/ROMS/*.css bin/ROMS/
   cp src/ROMS/*.sql bin/ROMS/
   mkdir -p bin/images
   cp -r images/* bin/images/
   ```
5. Run the application:
   ```bash
   java --module-path "/path/to/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.fxml -cp bin:/path/to/mysql-connector-j-8.0.33.jar App
   ```

#### Using the Script (run_app.sh)

1. Create a file named `run_app.sh` in the project root with the following content (adjust paths accordingly):

   ```bash
   #!/bin/bash

   # Set the JavaFX path - update this with your actual JavaFX path
   JAVAFX_PATH="/path/to/javafx-sdk-21.0.6"

   # Set the MySQL connector path
   MYSQL_CONNECTOR="/path/to/mysql-connector-j-8.0.33.jar"

   # Set the working directory
   cd "$(dirname "0 DA")"

   # Check if JavaFX exists
   if [ ! -d "$JAVAFX_PATH" ]; then
     echo "Error: JavaFX SDK not found at $JAVAFX_PATH"
     exit 1
   fi

   # Create bin directory if it doesn't exist
   mkdir -p bin

   # Compile all the Java files
   echo "Compiling Java files..."
   javac --module-path "$JAVAFX_PATH/lib" --add-modules javafx.controls,javafx.fxml -d bin src/ROMS/*.java

   # Check if compilation was successful
   if [ $? -ne 0 ]; then
     echo "Compilation failed."
     exit 1
   fi

   # Copy all non-Java files to bin directory
   echo "Copying resources..."
   mkdir -p bin/ROMS
   cp src/ROMS/*.fxml bin/ROMS/
   cp src/ROMS/*.css bin/ROMS/
   cp src/ROMS/*.sql bin/ROMS/
   mkdir -p bin/images
   cp -r images/* bin/images/

   # Run the application
   echo "Running the application..."
   java --module-path "$JAVAFX_PATH/lib" --add-modules javafx.controls,javafx.fxml -cp bin:$MYSQL_CONNECTOR App
   ```

2. Make the script executable and run it:
   ```bash
   chmod +x run_app.sh
   ./run_app.sh
   ```

## Troubleshooting

### JavaFX Runtime Components Missing

If you get an error like "Error: JavaFX runtime components are missing", make sure you're using the `--module-path` and `--add-modules` options correctly.

### MySQL JDBC Driver Not Found

If you get a "MySQL JDBC Driver not found" error:

1. Make sure you've downloaded the MySQL Connector/J
2. Ensure the connector JAR is correctly included in the classpath

### Connection to Database Failed

If you can't connect to the database:

1. Verify MySQL is running
2. Check the connection string in `DatabaseConnection.java`
3. Confirm the database exists and has the correct tables

### UI Issues or Missing Resources

If the UI doesn't display correctly or resources are missing:

1. Make sure all FXML and CSS files were correctly copied to the bin directory
2. Check that image paths are correct
3. Verify the package structure is maintained in the bin directory
