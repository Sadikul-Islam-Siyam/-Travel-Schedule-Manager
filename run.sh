#!/bin/bash
# Travel Schedule Manager Launcher
# This script runs the application with proper JavaFX configuration

echo "Starting Travel Schedule Manager..."
echo

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Run the application
cd "$SCRIPT_DIR"
java --module-path "$SCRIPT_DIR/target/TravelScheduleManager.jar" \
     --add-modules javafx.controls,javafx.fxml \
     -jar "$SCRIPT_DIR/target/TravelScheduleManager.jar"

if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Failed to start the application"
    echo "Make sure you have built the project first with: mvn clean package"
fi
