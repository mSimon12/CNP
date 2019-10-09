#! /bin/bash

javac -cp ".:./jade-4.3.jar" agents/*.java
javac -cp ".:./jade-4.3.jar" StartJade.java
java -cp ".:./jade-4.3.jar" StartJade