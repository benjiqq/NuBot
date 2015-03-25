#!/bin/bash

#Concatenate arguments 
args=""
for var in "$@"
do
    args+=$var
done
    echo "$args"

#Execute nubot  and wait
java -jar NuBot $args
wait

# It gets here when the app quits, even with forced kill -9 <pid>
echo "NuBot was shut down. Executing the termination script to make sure all orders are cleaned up and liquidity info reset"

# Execute the cleanup using the same arguments
#java -jar NuBot kill $args