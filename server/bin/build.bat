@ECHO OFF

SET SOURCE_DIRECTORY=../../src/
SET BIN_DIRECTORY=../../bin
SET LIB_DIRECTORY=../webapps/WebCrawler/WEB-INF/lib
SET BUILD_DIRECTORY=%~dp0

CD %SOURCE_DIRECTORY%
SET SOURCE_DIRECTORY=%cd%

CD %BUILD_DIRECTORY%
CD %BIN_DIRECTORY%
SET BIN_DIRECTORY=%cd%

CD %BUILD_DIRECTORY%
CD %LIB_DIRECTORY%
SET LIB_DIRECTORY=%cd%

ECHO Build started.

ECHO Deleting old class files.
CD %BIN_DIRECTORY%
FOR /F "delims=" %%A IN ('DIR /b') DO ( RMDIR "%%A" /s/q >NUL 2>&1|| DEL "%%A" /s/q >NUL 2>&1) 
CD %BUILD_DIRECTORY%

ECHO Deleting old JAR.
CD %LIB_DIRECTORY%
DEL webCrawler.jar /s/q >NUL 2>&1
CD %BUILD_DIRECTORY%

ECHO Compiling class files.
SET CLASSPATH=%SOURCE_DIRECTORY%;%LIB_DIRECTORY%/*
javac -classpath %CLASSPATH% -d %BIN_DIRECTORY% %SOURCE_DIRECTORY%/server/WebCrawlerServlet.java
javac -classpath %CLASSPATH% -d %BIN_DIRECTORY% %SOURCE_DIRECTORY%/server/WebCrawlerServletListener.java

ECHO Building webCrawler.jar.
CD %BIN_DIRECTORY%
jar cfm webCrawler.jar %BUILD_DIRECTORY%/build_config/MANIFEST.MF *
MOVE /y ./webCrawler.jar %LIB_DIRECTORY% >NUL
CD %BUILD_DIRECTORY%

ECHO Done building webCrawler.jar.