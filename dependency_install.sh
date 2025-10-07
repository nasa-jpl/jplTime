#!/bin/bash
mvn install:install-file   -Dfile=lib/JNISpice-v2022-05.jar   -DgroupId=gov.nasa.jpl.spice   -DartifactId=jnispice   -Dversion=v2022-05   -Dpackaging=jar
