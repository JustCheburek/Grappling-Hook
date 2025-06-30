@echo on
echo Compiling GrapplingHook plugin...

rem Set JAVA_HOME
set JAVA_HOME=%ProgramFiles%\Java\jdk-24

echo JAVA_HOME set to %JAVA_HOME%

rem Create directories
mkdir target\classes
mkdir target\classes\META-INF

echo Directories created

rem Copy resources
xcopy /E /Y src\main\resources\* target\classes\

echo Resources copied

rem Find Maven
set MAVEN_HOME=%CD%\apache-maven-3.9.10
echo MAVEN_HOME set to %MAVEN_HOME%

rem Run Maven
echo Running Maven...
%MAVEN_HOME%\bin\mvn.cmd clean package

echo Build completed. JAR file should be in target\GrapplingHook-1.7.0.jar 