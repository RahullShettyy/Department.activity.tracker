# Department Activity Tracker

## Setup Instructions

1. **Database Setup**:
   - Ensure you have MySQL installed and running.
   - Run the script `src/main/resources/schema.sql` in your MySQL database to create the necessary tables and initial data.
     ```bash
     mysql -u root -p < src/main/resources/schema.sql
     ```
   - Update `src/main/resources/application.properties` if your MySQL username/password differs from `root/password`.

2. **Build & Run**:
   - Open this folder in VS Code.
   - Run the `Main.java` file (found in `src/main/java/com/tracker/Main.java`).
   - Alternatively, use Maven:
     ```bash
     mvn clean compile exec:java -Dexec.mainClass="com.tracker.Main"
     ```

3. **Usage**:
   - Open your browser to [http://localhost:8000](http://localhost:8000).
   - You will see the Dashboard.
   - Navigate to "Events" to see the list (populated from database).

## Features
- **Frontend**: HTML5, Tailwind CSS, Vanilla JS.
- **Backend**: Java HTTP Server (No heavy frameworks).
- **Database**: MySQL.
