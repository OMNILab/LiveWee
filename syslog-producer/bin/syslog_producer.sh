#!/bin/bash

VERSION=1.0.1

# Get base folder
base_dir=$(dirname $0)/..

# Check which java to use
if [ -z "$JAVA_HOME" ]; then
    JAVA="java"
else
    JAVA="$JAVA_HOME/bin/java"
fi

# Add multiple java class paths
for file in $base_dir/target/syslog-producer-$VERSION*.jar;
do
    CLASSPATH=$CLASSPATH:$file
done 

exec $JAVA -cp $CLASSPATH cn.edu.sjtu.omnilab.livewee.logproducer.StartJob $@