dnl Process this file with autoconf to produce a configure script.

dnl 
dnl $Id: configure.ac,v 1.14 2010-03-26 07:35:57 dog Exp $
dnl Copyright (C) 2003 The Free Software Foundation
dnl 
dnl This file is part of GNU classpath inetlib.
dnl 
dnl GNU classpath is free software; you can redistribute it and/or modify it
dnl under the terms of the GNU General Public License as published by the
dnl Free Software Foundation; either version 2, or (at your option) any
dnl later version.
dnl  
dnl GNU classpath is distributed in the hope that it will be useful, but
dnl WITHOUT ANY WARRANTY; without even the implied warranty of
dnl MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
dnl General Public License for more details.
dnl 
dnl You should have received a copy of the GNU General Public License
dnl along with GNU JavaMail; see the file COPYING.  If not, write to the
dnl Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
dnl 02110-1301 USA. 
dnl

AC_INIT(inetlib, 1.2)
AC_CONFIG_SRCDIR(source/gnu/inet/util/LineInputStream.java)
AC_CONFIG_AUX_DIR([config])
AM_INIT_AUTOMAKE

PACKAGE="inetlib"
VERSION="1.2"
AC_SUBST(PACKAGE)
AC_SUBST(VERSION)

AC_PROG_JAVAC
AC_PROG_JAR
AC_PROG_JAVADOC
AC_JAVA_OPTIONS
dnl AC_CHECK_CLASSPATH

INETLIB_WITH_JSSE
INETLIB_WITH_SASL
INETLIB_WITH_AUTH_CALLBACK

AC_CONFIG_FILES(Makefile)

AC_OUTPUT

