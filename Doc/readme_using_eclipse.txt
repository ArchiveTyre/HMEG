





For debugging some of these may be needed:
sudo apt-get install git gcc libgl1-mesa-dev libglu1-mesa-dev xorg-dev libice-dev libsm-dev libx11-dev libxext-dev libxxf86vm-dev libxinerama-dev libxrandr-dev libxrender-dev



Eclipse plugin egit can optionally be added. Help->Install new Software, work with: idigo update site, collaboration tools egit. 
Right click on "RoboticsSandBox" -> Team -> Share project -> Git -> "Use or create repository in parent folder of project -> Finish



Some jogl lib files will be needed (path below may differ):
cp -au /home/henrik/twotb/d/arc/java/jogl/lib ~/eclipse/lib

A mail lib will be needed (path below may differ):
cd /tmp
unzip /home/henrik/Desktop/twotb/d/arc/java/javamail/javamail1_4_7.zip 
cd lib
cp -au lib/mail.jar ~/eclipse/lib/




To work with this code:
mkdir ~/git
cd ~/git

In that folder do (path below may differ):
git clone /home/henrik/twotb/git/RoboticsSandBox RoboticsSandBox




You will also need to add some library files

gluegen-rt.jar
gluegen-rt-natives-linux-amd64.jar
jetty-all-7.0.2.v20100331.jar
jogl-all.jar
jogl-all-natives-linux-amd64.jar
mail.jar
servlet-api-2.5.jar

mkdir ~/eclipse/lib
cd ~/eclipse/lib
scp alex@b3.eit.se:/home/henrik/twotb/d/arc/java/lib/mail.jar .





I wish I knew how to do the rest on CLI so that I could have a script to set this up. Alas I only have set up Eclipse via GUI.




How to setup and build (compile) 

Open new eclipse workspace.

Import projects 
Root dir ~/git/RoboticsSandBox


Open java perspective.





Set up build path

For each project
Right click over project -> "Build path" -> "Configure build path" -> "Libraries" 

Check that it does have a "Add JRE System Library". If not add that: -> "Add library" -> "Add JRE System Library" -> Next -> Finish.





For the server:

The server needs jogl (see client) and javamail.

javamail:
 
javamail-1.4.5 can be downloaded from http://www.oracle.com/technetwork/java/javamail/index-138643.html
Unzip and put the mail.jar in folder "~/eclipse/lib" or (if you have root) in folder "/usr/share/java".

In Eclipse "Package Explorer" right click on "RSB_Server". 
Do: "Build Path" -> "Add external archives or JARS" -> mail.jar.

Do refresh on the projects in eclipse that have a red ! on them.



Now it should be possible to run the program from within eclipse.
For the server it is the main method in se.eit.rsb_server_pkg that is main.

On first try output can look like this:

Usage: ./server.jar <options> [options]

Where [options] are:
-p <port>   : tcpip port number
-d <path>   : javascript directory
-s <path>   : game save directory
-w          : if set launch a web browser


Try someting like:
Debug as -> debug configurations -> Arguments
-p 8080 -d /home/henrik/git/RoboticsSandBox/JavaScript -s /home/henrik/git/RoboticsSandBox/savedGamesAndPlayers -w


Using ~/... instead of /home/henrik/...
Did not work for some reason.





For the client:

This project needs additional jar JOGL. Now add needed external jars.
http://download.java.net/media/jogl/builds/archive/jsr-231-1.1.0/jogl-1.1.0-windows-i586/Userguide.html

Download JOGL binaries from:
http://jogamp.org/wiki/index.php/Downloading_and_installing_JOGL

Placed the 4 files "gluegen-rt.jar", "gluegen-rt-natives-linux-amd64.jar", "jogl.all.jar" & "jogl-all-natives-linux-amd64.jar" 
in folder "~/eclipse/lib" or (if you have root) in folder "/usr/share/java".

"jogl.all.jar" is called "jogl-all.jar" in later versions. 
It seems "jogl.all.jar" is sometimes jogl-all.jar

In Eclipse "Package Explorer" right click on "RSB_Client". 
Do: "Build Path" -> "Add external archives or JARS" -> those 4 jar files.


If the client can not find the RoboticsSandBox (server) project.
In Eclipse "Package Explorer" right click on "RSB_Client". 
Do: "Build Path" -> Projects.
Remove RoboticsSandBox and add it again (as workspace path).






To create runnable jar files do:

Start Server at least once in dubug.
Right click on package -> Export -> java -> runnable jar file
Select launch configuration: RSB_Server
Give export destination like: ~/git/RoboticsSandBox/server.jar
Select "Package required libraries into generated JAR"
Press Finish.

Start Client at least once in dubug.
Right click on package -> Export -> java -> runnable jar file
Select launch configuration: RSB_Client
Give export destination like: ~/git/RoboticsSandBox/client.jar
Select "Package required libraries into generated JAR"
Press Finish.


To actually run a link to (or copy of) the res folder will also be needed.




In a terminal you can try it:
cd ~/git/RoboticsSandBox
chmod +x server.jar client.jar
./server.jar
./client.jar


The resulting exported jar file should run on all 3 major desktop OSes (not tested yet).
Well that may require more native files in the build path.








Set up eclipse to work with git
Right click the projects -> team -> share project
Select git
Select "Use or create a repository in parent folder of project".
Finnsh











Before doing changes, make sure your work area is up to date.
	cd ~/git/RoboticsSandBox/; git pull ~/Desktop/twotb/git/RoboticsSandBox 
	
After doing changes. 
* Update the Version string v in Version.java, remove + and increment number. 
* If major changes write something in history below. 
* Check in to local repository.
* backup by doing pull from server repository.
	cd ~/Desktop/twotb/git/RoboticsSandBox; git pull ~/git/RoboticsSandBox/




When starting eclipse next time tell it to use workarea:
~/git/RoboticsSandBox




