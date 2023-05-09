#!/bin/bash
 mvn -U -e release:clean release:prepare release:perform -DreleaseVersion=0.9 -DdevelopmentVersion=0.10-SNAPSHOT -Dtag=0.9
