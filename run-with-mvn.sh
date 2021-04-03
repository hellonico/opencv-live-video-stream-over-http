#!/bin/bash 
mvn clean compile exec:java -Dexec.args="0.0.0.0 8080  cam.edn"
