%define name                     jajuk
%define version                     0.3.2
%define release                     1mdk

%define section                  Multimedia/Sound
%define title                         Jajuk
%define longtitle                 Jajuk Advanced Jukebox


Summary:                    %{longtitle}
Name:                         %{name}
Version:                     %{version}
Release:                     %{release}
License:                     GPL
Group:                         Sound
Url:                         http://%{name}.sourceforge.net
Source: http://jajuk.sourceforge.net
BuildRoot: %_topdir/%{name}
Requires:                     j2re >= 1.4.2

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
   /usr/share/doc/jajuk/README.html
   /usr/share/doc/jajuk/README_chn.html
   /usr/share/doc/jajuk/README_deu.html
   /usr/share/doc/jajuk/README_fra.html
   /usr/share/doc/jajuk/README_ita.html
   /usr/share/doc/jajuk/README_nld.html
   /usr/share/doc/jajuk/README_swe.html
   /usr/share/doc/jajuk/README_esp.html
   /usr/share/icons/jajuk-logo.png
   /usr/share/icons/mini/jajuk-logo.png
   /usr/share/icons/large/jajuk-logo.png
   /usr/lib/menu/jajuk

%post
update-menus

%postun
update-menus
rm -rf /usr/share/doc/jajuk
rm -rf /usr/lib/jajuk
