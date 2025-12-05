#!/bin/sh

# install traas into local maven repository
./../mvnw install:install-file \
    -Dfile=TraaS.jar           \
    -DgroupId=de.tudresden     \
    -DartifactId=traas         \
    -Dversion=1.1              \
    -Dpackaging=jar
