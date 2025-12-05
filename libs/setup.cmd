@echo off

REM install traas into local maven repository
../mvnw.cmd install:install-file ^
    -Dfile=TraaS.jar             ^
    -DgroupId=de.tudresden       ^
    -DartifactId=traas           ^
    -Dversion=1.1                ^
    -Dpackaging=jar
