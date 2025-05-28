@echo off

rem Set the JavaFX path - update this with your actual JavaFX path
rem feriel
rem set JAVAFX_PATH=C:\Program Files\Java\javafx-sdk-21.0.6
rem ilyes
set JAVAFX_PATH=C:\javafx-sdk-21.0.6

rem Set the MySQL connector path
rem feriel
rem set MYSQL_CONNECTOR=C:\mysql-connector-j-9.2.0.jar
rem ilyes
set MYSQL_CONNECTOR=C:\mysql-connector-j-9.2.0.jar


rem Create bin directory if it doesn't exist
if not exist ..\bin mkdir ..\bin

rem Compile all the Java files
echo Compiling Java files...
javac -Xlint:unchecked --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -d ..\bin ..\src\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    exit /b 1
)

rem Copy all non-Java files to bin directory
echo Copying resources...
copy ..\src\*.fxml ..\bin\
copy ..\src\*.css ..\bin\
copy ..\src\*.sql ..\bin\

rem Copy the font file to the bin directory
echo Copying font files...
copy ..\src\*.otf ..\bin\

if not exist ..\bin\images mkdir ..\bin\images
if exist ..\src\images xcopy /E /I ..\src\images ..\bin\images
if exist images xcopy /E /I images ..\bin\images

rem Run the application
echo Running the application...
cd ..\bin
java --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -cp .;%MYSQL_CONNECTOR% App