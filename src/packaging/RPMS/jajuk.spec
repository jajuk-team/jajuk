Summary: Jajuk Advanced Jukebox
Name: jajuk
Version: @VERSION_REPLACED_BY_ANT@
Release: 1
Group: Applications/Audio
URL: http://jajuk.sourceforge.net
License: GPL
Source: http://jajuk.sourceforge.net
Packager: Jajuk team
BuildRoot: %{_topdir}/%{name}

%description
Jajuk is software that organizes and plays music. Jajuk is designed
 to be intuitive, fast and provide multiple ways to perform the same
 operation. It is a fully-featured application geared towards advanced
 users with very large or scattered music collections.

%files
%defattr(-,root,root)
   /usr/bin/jajuk
   /usr/share/jajuk/*
   /usr/share/applications/jajuk.desktop
   /usr/share/doc/jajuk/*
   /usr/share/pixmaps/*
%defattr(555,root,root)
   /usr/share/jajuk/lib32/*
   /usr/share/jajuk/lib64/*

%post

%postun
   
