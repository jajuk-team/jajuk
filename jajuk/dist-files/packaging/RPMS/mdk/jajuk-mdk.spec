%define name                     jajuk
%define version                     0.3.2rc1
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
   /usr/lib/jajuk/bin/*
   /usr/lib/jajuk/lib/*
   /usr/lib/jajuk/native/*
   /usr/share/doc/jajuk/*
   /usr/share/icons/*
   /usr/lib/menu/*

%post
update-menus

%postun
update-menus
rm -rf /usr/share/doc/jajuk
rm -rf /usr/lib/jajuk
