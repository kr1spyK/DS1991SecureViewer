Readme.txt

-----------------------------------------
| Set Default Adapter/Port (SetDefault) |
-----------------------------------------

I. Description
=------------=
Set the default 1-Wire Network by selecting the adapter/port 
combination.  This will write a 'onewire.properties' file in 
a platform specific location. 

II. Files
=------=
readme.txt             - this file
run.bat                - batch file to run this example
build.bat              - batch file to rebuild this example
tini_build.bat         - batch file to rebuild this example for TINI
SetDefault.class       - prebuilt Desktop class
\src\SetDefault.java   - source for this example
\tini\SetDefault.tini  - prebuilt TINI class (for TINI 1.02 or later)

III. Instructions
=--------------=

Desktop: 

1. Use the provided 'run.bat' to start this application.

2. Use the 'build.bat' to rebuild this example.  Note that this batch
   file assumes it is in the specific example directory.  It is recommended
   for ease of use to put the OneWireAPI.jar file in your default classpath
   or in <JDK_HOME>\jre\lib\ext.

TINI (1.02 or later):

1. Load SetDefault from the examples\SetDefault\tini directory
   using FTP. First open FTP and execute the following commands (for the sake
   of this discussion assume you chose 180.0.42.43 for your IP address):

       open 180.0.42.43
       root
       tini
       bin
       put SetDefault.tini
       close
       quit

  In Windows 9x you can place these commands in a script file (e.g. doftp.cmd) 
  and execute with 'ftp -s:doftp.cmd'.

2. Open a Telnet session to your TINI board.

    Welcome to slush.  (Version 1.02)

    TINI login: root
    TINI password:
    TINI />   

3. To run SetDefault type 'java SetDefault.tini'. 

4. Use the 'tini_build.bat' to rebuild this example for TINI.  Note that this 
   batch file assumes it is in the specific example directory.  This batch 
   file uses the  environment variable TINI_HOME.  This variable must be set 
   before running build batch file. For example:  
       set TINI_HOME=e:\tini1.02

IV. Revision History
=-------------------=
0.00 - First release.
0.01 - Changed to non-deprecated readLine()
