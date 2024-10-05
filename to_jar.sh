#!/bin/bash

# Enable error handling
set -e

# Declaration of variables
work_dir="/home/eli/Documents/winter-framework"
src="$work_dir/src"
lib="$work_dir/lib"
bin="$work_dir/classes"
jar_name="winter-framework"
jar_path="$work_dir/$jar_name.jar"

# Delete the [bin] directory if it exists
if [ -d "$bin" ]; then
    rm -rf "$bin"
fi

# Create the [bin] directory
mkdir -p "$bin"

# Compile Java files
find "$src" -name "*.java" > sources.txt
javac -d "$bin" -cp "$lib/*" @sources.txt
rm sources.txt

# Package into JAR file
echo "Packaging $jar_name.jar..."
jar cf "$jar_path" -C "$bin" .

echo "JAR packaging completed."
