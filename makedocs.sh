#!/bin/sh
javadoc \
    -d docs \
    -link https://docs.oracle.com/en/java/javase/13/docs/api/ \
    -cp $HOME/.m2/repository/gov/nasa/jpl/jpl_time/2025-09a/jpl_time-2025-09a.jar:$HOME/.m2/repository/org/apache/commons/commons-lang3/3.7/commons-lang3-3.7.jar:lib/JNISpice-v2022-05.jar:$HOME/.m2/repository/org/jdom/jdom2/2.0.6/jdom2-2.0.6.jar \
    -docletpath $HOME/.m2/repository/nl/talsmasoftware/umldoclet/2.0.8/umldoclet-2.0.8.jar \
    -doclet nl.talsmasoftware.umldoclet.UMLDoclet \
    -sourcepath src/main/java \
    -subpackages gov.nasa.jpl
