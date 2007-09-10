<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "./helpset_2_0.dtd">

<helpset version="1.0">

  <!-- title -->
  <title>Aide Jajuk</title>

  <!-- maps -->
  <maps>
     <homeID>table_of_contents</homeID>
     <mapref location="Map.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Sommaire</label>
    <type>javax.help.TOCView</type>
    <data>jajukToc.xml</data>
  </view>

   <view>
    <name>Search</name>
    <label>Recherche</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
  </view>

  <view>
    <name>Favorites</name>
    <label>Favoris</label>
    <type>javax.help.FavoritesView</type>
  </view>
 
</helpset>
