Summary: Jajuk Advanced Jukebox
Name: jajuk
Version: 0.3.2
Release: 1
Group: Applications/Audio
Copyright: Copyright Bertrand Florat & Jajuk team 2003 - 2004
URL: http://jajuk.sourceforge.net
Source: http://jajuk.sourceforge.net
Packager: Bertrand Florat
BuildRoot: %_topdir/%{name}
Requires: java2 >= 1.4.2

%description
Jajuk is a Java music organizer for all platforms.
The main goal of this project is to provide a fully-featured
application to advanced users with large or scattered music
collections. Jajuk supports MP3, OGG Vorbis, AU, AIFF, WAV
and SPEEX audio formats. Jajuk is a Free Software published
under GPL license.

Jajuk main principles are :

   * Maximum portability: 100% pure Java when possible
   * Maximum features: Jajuk is made firstly for advanced
   users looking for powerful functionalities
   * Maximum usability: Jajuk is designed to be simple, fast
   and provide multiple ways to perform the same operation
   * Only for music: Jajuk will never become a multi-media
   (video, pictures) jukebox
%files
%defattr(-,root,root)
  /usr/bin/jajuk
   /usr/lib/jajuk/bin/jajuk-help.jar
   /usr/lib/jajuk/bin/jajuk-help_fr.jar
   /usr/lib/jajuk/bin/jajuk.jar
   /usr/lib/jajuk/lib/animation-1.1.3.jar
   /usr/lib/jajuk/lib/basicplayer-2.3.jar
   /usr/lib/jajuk/lib/commons-httpclient-2.0.1.jar
   /usr/lib/jajuk/lib/commons-logging-1.0.jar
   /usr/lib/jajuk/lib/id3-1.6.0d7.jar
   /usr/lib/jajuk/lib/javalayer-0.4.jar
   /usr/lib/jajuk/lib/jcommon-0.9.0.jar
   /usr/lib/jajuk/lib/jfreechart-0.9.15.jar
   /usr/lib/jajuk/lib/jh-2.0.2.jar
   /usr/lib/jajuk/lib/jlGui-2.3.jar
   /usr/lib/jajuk/lib/jogg-0.0.5.jar
   /usr/lib/jajuk/lib/jorbis-0.0.12.jar
   /usr/lib/jajuk/lib/kunststoff-2.0.1.jar
   /usr/lib/jajuk/lib/liquidlnf-0.2.9-alpha5.jar
   /usr/lib/jajuk/lib/log4j-1.2.6.jar
   /usr/lib/jajuk/lib/metouia-1.0b.jar
   /usr/lib/jajuk/lib/mp3spi-1.9.jar
   /usr/lib/jajuk/lib/tablelayout-1.0.jar
   /usr/lib/jajuk/lib/tray-0.1.6.jar
   /usr/lib/jajuk/lib/tritonus_share-1.0.jar
   /usr/lib/jajuk/lib/vorbisspi-1.0.jar
    /usr/lib/jajuk/lib/infonodeDockingWindows-1.1.0.jar
   /usr/lib/jajuk/native/libtray.so
   /usr/share/applications/Jajuk.desktop
   /usr/share/doc/packages/jajuk/README.html
   /usr/share/doc/packages/jajuk/README_chn.html
   /usr/share/doc/packages/jajuk/README_deu.html
   /usr/share/doc/packages/jajuk/README_fra.html
   /usr/share/doc/packages/jajuk/README_ita.html
   /usr/share/doc/packages/jajuk/README_nld.html
   /usr/share/doc/packages/jajuk/README_swe.html
   /usr/share/doc/packages/jajuk/README_esp.html
   /usr/share/pixmaps/jajuk-logo.png

%post

%postun
rm -rf /usr/share/doc/packages/jajuk
rm -rf /usr/lib/jajuk
   