Summary: Jajuk Advanced Jukebox
Name: jajuk
Version: 0.3.3
Release: suse
Group: Applications/Audio
Copyright: Copyright Bertrand Florat & Jajuk team 2003 - 2004
URL: http://jajuk.sourceforge.net
Source: http://jajuk.sourceforge.net
Packager: Bertrand Florat
BuildRoot: %_topdir/%{name}

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
  /usr/bin/*
   /usr/lib/jajuk/bin/*
   /usr/lib/jajuk/lib/*
   /usr/lib/jajuk/native/*
   /usr/share/applications/*
   /usr/share/doc/packages/jajuk/*
   /usr/share/pixmaps/*

%post

%postun
   