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
Jajuk is a Java music organizer for all platforms.
The main goal of this project is to provide a fully-featured
application to advanced users with large or scattered music
collections. Jajuk is a Free Software published
under GPL license.

Jajuk main principles are :
   * Maximum portability: 100% pure Java when possible.
   * Maximum features: Jajuk is made firstly for advanced
   users looking for powerful functionalities.
   * Maximum usability: Jajuk is designed to be simple, fast
   and provide multiple ways to perform the same operation.
   
%files
%defattr(-,root,root)
   /usr/bin/*
   /usr/share/jajuk/bin/*
   /usr/share/applications/*
   /usr/share/doc/jajuk/*
   /usr/share/pixmaps/*
%defattr(555,root,root)
   /usr/share/jajuk/lib/*
   /usr/share/jajuk/lib64/*
   
%post

%postun
   
%define _binaries_in_noarch_packages_terminate_build   0
