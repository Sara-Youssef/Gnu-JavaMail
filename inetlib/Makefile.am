## Process this file with automake to produce Makefile.in.

# Makefile.am for GNU classpath inetlib.

AUTOMAKE_OPTIONS = 1.10
EXTRA_DIST = source

JAVAC = @JAVAC@
JAVACFLAGS = @JAVACFLAGS@
JAVADOC = @JAVADOC@
JAVADOCFLAGS = @JAVADOCFLAGS@
VERSION=@VERSION@

src = @srcdir@/source
dst = classes
doc = @srcdir@/docs

inetlib_jar = inetlib.jar
inetlib_version_jar = inetlib-$(VERSION).jar

sources = \
$(src)/gnu/inet/util/BASE64.java \
$(src)/gnu/inet/util/CRLFInputStream.java \
$(src)/gnu/inet/util/CRLFOutputStream.java \
$(src)/gnu/inet/util/EmptyX509TrustManager.java \
$(src)/gnu/inet/util/GetLocalHostAction.java \
$(src)/gnu/inet/util/GetSystemPropertyAction.java \
$(src)/gnu/inet/util/LaconicFormatter.java \
$(src)/gnu/inet/util/LineInputStream.java \
$(src)/gnu/inet/util/MessageInputStream.java \
$(src)/gnu/inet/util/MessageOutputStream.java \
$(src)/gnu/inet/util/SaslCallbackHandler.java \
$(src)/gnu/inet/util/SaslCramMD5.java \
$(src)/gnu/inet/util/SaslInputStream.java \
$(src)/gnu/inet/util/SaslLogin.java \
$(src)/gnu/inet/util/SaslNTLM.java \
$(src)/gnu/inet/util/SaslOutputStream.java \
$(src)/gnu/inet/util/SaslPlain.java \
$(src)/gnu/inet/util/TraceLevel.java \
$(src)/gnu/inet/smtp/Parameter.java \
$(src)/gnu/inet/smtp/ParameterList.java \
$(src)/gnu/inet/smtp/SMTPConnection.java \
$(src)/gnu/inet/imap/BODY.java \
$(src)/gnu/inet/imap/BODYSTRUCTURE.java \
$(src)/gnu/inet/imap/ENVELOPE.java \
$(src)/gnu/inet/imap/FLAGS.java \
$(src)/gnu/inet/imap/FetchDataItem.java \
$(src)/gnu/inet/imap/IMAPAdapter.java \
$(src)/gnu/inet/imap/IMAPCallback.java \
$(src)/gnu/inet/imap/IMAPConnection.java \
$(src)/gnu/inet/imap/IMAPConstants.java \
$(src)/gnu/inet/imap/IMAPException.java \
$(src)/gnu/inet/imap/INTERNALDATE.java \
$(src)/gnu/inet/imap/Literal.java \
$(src)/gnu/inet/imap/LiteralFactory.java \
$(src)/gnu/inet/imap/LiteralToken.java \
$(src)/gnu/inet/imap/MessageSet.java \
$(src)/gnu/inet/imap/Namespace.java \
$(src)/gnu/inet/imap/RFC822_SIZE.java \
$(src)/gnu/inet/imap/StringToken.java \
$(src)/gnu/inet/imap/Token.java \
$(src)/gnu/inet/imap/Tokenizer.java \
$(src)/gnu/inet/imap/UID.java \
$(src)/gnu/inet/imap/UIDSet.java \
$(src)/gnu/inet/imap/UTF7imap.java \
$(src)/gnu/inet/pop3/POP3Connection.java \
$(src)/gnu/inet/nntp/ActiveTime.java \
$(src)/gnu/inet/nntp/ActiveTimesIterator.java \
$(src)/gnu/inet/nntp/ArticleNumberIterator.java \
$(src)/gnu/inet/nntp/ArticleResponse.java \
$(src)/gnu/inet/nntp/ArticleStream.java \
$(src)/gnu/inet/nntp/FileNewsrc.java \
$(src)/gnu/inet/nntp/GroupIterator.java \
$(src)/gnu/inet/nntp/Group.java \
$(src)/gnu/inet/nntp/GroupResponse.java \
$(src)/gnu/inet/nntp/HeaderEntry.java \
$(src)/gnu/inet/nntp/HeaderIterator.java \
$(src)/gnu/inet/nntp/LineIterator.java \
$(src)/gnu/inet/nntp/Newsrc.java \
$(src)/gnu/inet/nntp/NNTPConnection.java \
$(src)/gnu/inet/nntp/NNTPConstants.java \
$(src)/gnu/inet/nntp/NNTPException.java \
$(src)/gnu/inet/nntp/OverviewIterator.java \
$(src)/gnu/inet/nntp/Overview.java \
$(src)/gnu/inet/nntp/PairIterator.java \
$(src)/gnu/inet/nntp/Pair.java \
$(src)/gnu/inet/nntp/PendingData.java \
$(src)/gnu/inet/nntp/PostStream.java \
$(src)/gnu/inet/nntp/Range.java \
$(src)/gnu/inet/nntp/StatusResponse.java \
$(src)/gnu/inet/ftp/ActiveModeDTP.java \
$(src)/gnu/inet/ftp/BlockInputStream.java \
$(src)/gnu/inet/ftp/BlockOutputStream.java \
$(src)/gnu/inet/ftp/CompressedInputStream.java \
$(src)/gnu/inet/ftp/CompressedOutputStream.java \
$(src)/gnu/inet/ftp/DTPInputStream.java \
$(src)/gnu/inet/ftp/DTP.java \
$(src)/gnu/inet/ftp/DTPOutputStream.java \
$(src)/gnu/inet/ftp/FTPConnection.java \
$(src)/gnu/inet/ftp/FTPException.java \
$(src)/gnu/inet/ftp/FTPResponse.java \
$(src)/gnu/inet/ftp/FTPURLConnection.java \
$(src)/gnu/inet/ftp/Handler.java \
$(src)/gnu/inet/ftp/PassiveModeDTP.java \
$(src)/gnu/inet/ftp/StreamInputStream.java \
$(src)/gnu/inet/ftp/StreamOutputStream.java \
$(src)/gnu/inet/gopher/DirectoryEntry.java \
$(src)/gnu/inet/gopher/DirectoryListing.java \
$(src)/gnu/inet/gopher/GopherConnection.java \
$(src)/gnu/inet/gopher/GopherContentHandler.java \
$(src)/gnu/inet/gopher/GopherURLConnection.java \
$(src)/gnu/inet/gopher/Handler.java \
$(src)/gnu/inet/finger/FingerConnection.java \
$(src)/gnu/inet/finger/FingerURLConnection.java \
$(src)/gnu/inet/finger/Handler.java \
$(src)/gnu/inet/http/Authenticator.java \
$(src)/gnu/inet/http/ByteArrayRequestBodyWriter.java \
$(src)/gnu/inet/http/ByteArrayResponseBodyReader.java \
$(src)/gnu/inet/http/ChunkedInputStream.java \
$(src)/gnu/inet/http/Cookie.java \
$(src)/gnu/inet/http/CookieManager.java \
$(src)/gnu/inet/http/Credentials.java \
$(src)/gnu/inet/http/HTTPConnection.java \
$(src)/gnu/inet/http/HTTPDateFormat.java \
$(src)/gnu/inet/http/Headers.java \
$(src)/gnu/inet/http/Request.java \
$(src)/gnu/inet/http/RequestBodyWriter.java \
$(src)/gnu/inet/http/Response.java \
$(src)/gnu/inet/http/ResponseBodyReader.java \
$(src)/gnu/inet/http/ResponseHeaderHandler.java \
$(src)/gnu/inet/http/SimpleCookieManager.java \
$(src)/gnu/inet/http/event/ConnectionEvent.java \
$(src)/gnu/inet/http/event/ConnectionListener.java \
$(src)/gnu/inet/http/event/RequestEvent.java \
$(src)/gnu/inet/http/event/RequestListener.java \
$(src)/gnu/inet/ldap/AttributeValues.java \
$(src)/gnu/inet/ldap/BERConstants.java \
$(src)/gnu/inet/ldap/BERDecoder.java \
$(src)/gnu/inet/ldap/BEREncoder.java \
$(src)/gnu/inet/ldap/BERException.java \
$(src)/gnu/inet/ldap/LDAPConnection.java \
$(src)/gnu/inet/ldap/LDAPResult.java \
$(src)/gnu/inet/ldap/Modification.java \
$(src)/gnu/inet/ldap/ResultHandler.java \
$(src)/gnu/inet/comsat/ComsatClient.java \
$(src)/gnu/inet/comsat/ComsatInfo.java

# Package names for gjdoc
package_names = \
gnu.inet.util \
gnu.inet.smtp \
gnu.inet.imap \
gnu.inet.pop3 \
gnu.inet.nntp \
gnu.inet.ftp \
gnu.inet.gopher \
gnu.inet.finger \
gnu.inet.http \
gnu.inet.http.event \
gnu.inet.ldap \
gnu.inet.comsat

# Resources
src_l10n = \
$(src)/gnu/inet/http/L10N.properties \
$(src)/gnu/inet/imap/L10N.properties

# Targets
BUILT_SOURCES = $(inetlib_version_jar)

jsse = @JSSE_JAR@
sasl = @SASL_JAR@
auth_callback = @AUTH_CALLBACK_JAR@

INETLIB_CLASSPATH = $(dst):$(src):$(jsse):$(sasl):$(auth_callback)
META_INF = $(dst)/META-INF
META_INF_FILES = $(META_INF)/COPYING

classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(sources))
dst_l10n = $(patsubst $(src)/%, $(dst)/%, $(src_l10n))

# Targets...

# Jar

$(inetlib_version_jar): $(dst) $(META_INF_FILES) $(classes) $(dst_l10n)
	$(JAR) cf $(inetlib_version_jar) -C $(dst) META-INF -C $(dst) gnu
	rm -f $(inetlib_jar)
	ln -s $(inetlib_version_jar) $(inetlib_jar)

$(dst):
	mkdir -p $(dst)

$(dst_l10n): $(dst)/%: $(src)/% $(dst)
	cp $< $@

$(META_INF):
	mkdir -p $(META_INF)

$(META_INF)/COPYING: $(META_INF) COPYING
	cp @srcdir@/COPYING $(META_INF)/

# Compilation

$(classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<

javadoc:
	mkdir -p $(doc)
	$(JAVADOC) -d $(doc) -windowtitle "GNU inetlib API documentation" -sourcepath $(src) $(package_names)

clean-local:
	rm -rf $(dst) $(doc) $(inetlib_version_jar) $(inetlib_jar)

datadir = $(prefix)/share/java
data_DATA = $(inetlib_jar) $(inetlib_version_jar)

# End of Makefile.am
