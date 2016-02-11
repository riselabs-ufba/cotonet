#!/bin/sh
echo "====================="
echo "Gradle cleaning..."
gradle clean

echo "====================="
echo "Gradle building..."
gradle build

echo "====================="
echo "Gradle deploying..."
scp -P 22022 build/libs/repos_crawler-1.0.jar tindao:~/
scp -P 22022 src/test/resources/target_systems-conflict_prediction.txt tindao:~/

echo "====================="
echo "Done."
echo "====================="

