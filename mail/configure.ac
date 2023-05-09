# Process this file with autoconf to produce a configure script.

# 
# $Id: configure.ac,v 1.11 2005-08-25 12:31:12 dog Exp $
# Copyright (C) 2003 Nic Ferrier
# 
# This file is part of GNU JavaMail.
# 
# GNU JavaMail is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2, or (at your option) any
# later version.
#  
# GNU JavaMail is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with GNU JavaMail; see the file COPYING.  If not, write to the
# Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301 USA. 
#

AC_INIT(gnumail, 1.5)
AC_CONFIG_SRCDIR(source/gnu/mail/providers/ReadOnlyMessage.java)
AM_INIT_AUTOMAKE

PACKAGE="mail"
VERSION="1.5"
AC_SUBST(PACKAGE)
AC_SUBST(VERSION)

AC_PROG_JAVAC
AC_PROG_JAVADOC
AC_PROG_JAR
AC_JAVA_OPTIONS
dnl AC_CHECK_CLASSPATH

GNUMAIL_WITH_ACTIVATION_JAR
GNUMAIL_WITH_INETLIB_JAR
dnl GNUMAIL_WITH_JSSE

AC_CONFIG_FILES(Makefile)

AC_OUTPUT
