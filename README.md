# GNU Java Mail
This repository modules are based on modules provided by Classpath Extensions Software [(classpathx)](https://savannah.gnu.org/svn/?group=classpathx), which is a part of GNU Savannah Project.

# Packages
There are three packages used to provide a special handling for JavaMail API:
1. **mail:** it implements the public J2EE JavaMail API with an additional provided features including:
    * mbox provider
    * Maildir Provider
    * NNTP (store and transport) provider
2. **inetlib:** it's an extension library that can provide network protocols such as http, imap, pop3 and smtp to the client applications.
3. **activation:** it's used to build activation.jar which is a dependency for gnumail package.

# Version Number Convention
* The **build.xml** files of the three modules (activation, inetlib & mail) are modified to append a version number string to the end of the JAR name to easily differentiate between the JAR different versions after applying internal modifications.
* Since this repository depends on **GNU 1.1.2 release**, we add a minor version to the end of the JAR version string, so the JAR name becomes **jarname-1.1.2.x.jar**
* For example:
  1. The original **inetlib.jar** is renamed to be **inetlib-1.1.2.jar**
  2. The first modified version of **inetlib.jar** is **inetlib-1.1.2.1.jar**, the second one is **inetlib-1.1.2.2.jar**, and so on.

# Build JARs
1. Navigate to the required module, to build a new version upgrade the version number included in the **build.xml** file.
2. Run the command `make` to compile the source files.
3. Run the command `ant jarname.jar` to generate the required JAR with the previous assigned version number, there are four available packages to build:
    1. `ant activation.jar`
    2. `ant inetlib.jar`
    3. `ant gnumail.jar`
    4. `ant providers.jar`

# License
The included packages are distributed under the terms of the **GNU General Public License with Classpath Exception**, see [classpathx license](https://www.gnu.org/software/classpath/license.html).
