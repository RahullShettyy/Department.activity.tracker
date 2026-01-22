@echo off
echo Starting Event Tracker Backend...
echo DataBase User: root
echo Ensure MySQL is running!
java -cp "target/classes;lib/*" com.tracker.Main
pause
start http://localhost:8080
