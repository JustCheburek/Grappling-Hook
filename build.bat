@echo off
echo Compiling GrapplingHook plugin...

rem Create directories
mkdir target\classes
mkdir target\classes\META-INF

rem Copy resources
xcopy /E /Y src\main\resources\* target\classes\

rem Compile Java files
javac -d target\classes src\main\java\com\snowgears\grapplinghook\*.java src\main\java\com\snowgears\grapplinghook\utils\*.java src\main\java\com\snowgears\grapplinghook\api\*.java

rem Create JAR file
jar -cf target\GrapplingHook-1.7.0.jar -C target\classes .

echo Build completed. JAR file is in target\GrapplingHook-1.7.0.jar 