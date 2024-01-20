#!/bin/sh

# Use the XMX environment variable or a default value
JAVA_OPTS="-Xmx${XMX:-1g} -XX:MaxDirectMemorySize=512m"

echo $JAVA_OPTS

# Start the Java application with the JAVA_OPTS
java $JAVA_OPTS -jar app.jar
