# GNU Java Mail
This repository modules are based on modules provided by Classpath Extensions Software [(classpathx)](https://savannah.gnu.org/svn/?group=classpathx), which is a part of GNU Savannah Project.

# Packages
There are three packages used to provide special handling for JavaMail API:
1. **mail:** it implements the public J2EE JavaMail API with an additional provided features including:
* mbox provider
* Maildir Provider
* NNTP (store and transport) provider
2. **inetlib:** it's an extension library that can provide network protocols such as http, imap, pop3 and smtp to the client applications.
3. **activation:** it's used to build activation.jar which is a dependency for gnumail package.

# License
The included packages are distributed under the terms of the **GNU General Public License with Classpath Exception**, see [classpathx license](https://www.gnu.org/software/classpath/license.html).
