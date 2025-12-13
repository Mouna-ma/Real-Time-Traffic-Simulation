#!/bin/bash

python3 "$SUMO_HOME/tools/randomTrips.py" -n network.net.xml --route-file trips.trips.xml -e 50 --period 0.5

duarouter -n network.net.xml --route-files trips.trips.xml -o routes.rou.xml --named-routes true