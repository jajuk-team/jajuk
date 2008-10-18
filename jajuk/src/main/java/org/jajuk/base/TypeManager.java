/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */
package org.jajuk.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Manages types ( mp3, ogg...) supported by jajuk
 * <p>
 * static class
 */
public final class TypeManager extends ItemManager {
  /** extensions->types */
  private Map<String, Type> hmSupportedTypes = new HashMap<String, Type>(10);

  /** Self instance */
  private static TypeManager singleton;

  /**
   * No constructor available, only static access
   */
  private TypeManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, false, false,
        String.class, null));
    // Extension
    registerProperty(new PropertyMetaInformation(XML_TYPE_EXTENSION, false, true, true, false,
        false, String.class, null));
    // Player impl
    registerProperty(new PropertyMetaInformation(XML_TYPE_PLAYER_IMPL, false, true, true, false,
        false, Class.class, null));
    // Tag impl
    registerProperty(new PropertyMetaInformation(XML_TYPE_TAG_IMPL, false, true, true, false,
        false, Class.class, null));
    // Music
    registerProperty(new PropertyMetaInformation(XML_TYPE_IS_MUSIC, false, false, true, false,
        false, Boolean.class, null));
    // Seek
    registerProperty(new PropertyMetaInformation(XML_TYPE_SEEK_SUPPORTED, false, false, true,
        false, false, Boolean.class, null));
    // Icon
    registerProperty(new PropertyMetaInformation(XML_TYPE_ICON, false, false, false, false, false,
        String.class, null));
  }

  /**
   * @return singleton
   */
  public static TypeManager getInstance() {
    if (singleton == null) {
      singleton = new TypeManager();
    }
    return singleton;
  }

  /**
   * Register a type jajuk can read
   * 
   * @param type
   */
  public Type registerType(String sName, String sExtension, Class<?> cPlayerImpl, Class<?> cTagImpl) {
    return registerType(sExtension, sName, sExtension, cPlayerImpl, cTagImpl);
  }

  /**
   * Register a type jajuk can read with a known id
   * 
   * @param type
   */
  @SuppressWarnings("unchecked")
  private Type registerType(String sId, String sName, String sExtension, Class cPlayerImpl,
      Class cTagImpl) {
    if (hmSupportedTypes.containsKey(sExtension)) {
      // if the type is already in memory, use it
      return hmSupportedTypes.get(sExtension);
    }
    Type type = null;
    try {
      type = new Type(sId, sName, sExtension, cPlayerImpl, cTagImpl);
      registerItem(type);
      hmSupportedTypes.put(type.getExtension(), type);
    } catch (Exception e) {
      Log.error(109, "sPlayerImpl=" + cPlayerImpl + " sTagImpl=" + cTagImpl, e);
    }
    return type;
  }

  /**
   * Tells if the type is supported
   * 
   * @param type
   * @return
   */
  public boolean isExtensionSupported(String sExt) {
    return hmSupportedTypes.containsKey(sExt);
  }

  /**
   * Return type for a given extension
   * 
   * @param sExtension
   * @return
   */
  public Type getTypeByExtension(String sExtension) {
    return hmSupportedTypes.get(sExtension);
  }

  /**
   * Return all music types
   * 
   * @return
   */
  public List<Type> getAllMusicTypes() {
    List<Type> alResu = new ArrayList<Type>(5);
    Iterator<Type> it = hmSupportedTypes.values().iterator();
    while (it.hasNext()) {
      Type type = it.next();
      if (type.getBooleanValue(XML_TYPE_IS_MUSIC)) {
        alResu.add(type);
      }
    }
    return alResu;
  }

  /**
   * Return a list "a,b,c" of registered extensions, used by FileChooser
   * 
   * @return
   */
  public String getTypeListString() {
    StringBuilder sb = new StringBuilder();
    Iterator<String> it = hmSupportedTypes.keySet().iterator();
    while (it.hasNext()) {
      sb.append(it.next());
      sb.append(',');
    }
    sb.deleteCharAt(sb.length() - 1); // remove last ','
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return XML_TYPES;
  }

  /**
   * @param sID
   *          Item ID
   * @return item
   */
  public Type getTypeByID(String sID) {
    return (Type) getItemByID(sID);
  }

  /**
   * 
   * @return types list
   */
  @SuppressWarnings("unchecked")
  public synchronized List<Type> getTypes() {
   return (List<Type>)getItems();
  }
  
  /**
   * 
   * @return types iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<Type> getTypesIterator() {
    return new ReadOnlyIterator<Type>((Iterator<Type>)getItemsIterator());
  }

  /**
   * Convenient method to register all types when mplayer is not available
   * <p>
   * Note that we use explicite strings for icon location. It's to avoid loading
   * all icons at startup, we do it asynchronously to accelerate startup
   * </p>
   * 
   * @throws Exception
   */
  public static void registerTypesNoMplayer() throws Exception {
    // mp3
    Type type = TypeManager.getInstance().registerType(Messages.getString("Type.mp3"), EXT_MP3,
        Class.forName(PLAYER_IMPL_JAVALAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    // Do not use IconLoader to get icon, it takes too much time to
    // load all icons
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_mp3_16x16.png")
        .toExternalForm());
    // playlists
    type = TypeManager.getInstance().registerType(Messages.getString("Type.playlist"),
        EXT_PLAYLIST, Class.forName(PLAYER_IMPL_JAVALAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, false);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
    // Ogg vorbis
    type = TypeManager.getInstance().registerType(Messages.getString("Type.ogg"), EXT_OGG,
        Class.forName(PLAYER_IMPL_JAVALAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_ogg_16x16.png")
        .toExternalForm());
    // Wave
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wav"), EXT_WAV,
        Class.forName(PLAYER_IMPL_JAVALAYER), Class.forName(TAG_IMPL_NO_TAGS));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_wav_16x16.png")
        .toExternalForm());
    // au
    type = TypeManager.getInstance().registerType(Messages.getString("Type.au"), EXT_AU,
        Class.forName(PLAYER_IMPL_JAVALAYER), Class.forName(TAG_IMPL_NO_TAGS));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_wav_16x16.png")
        .toExternalForm());
  }

  /**
   * Convenient method to register all types when mplayer is available.
   * <p>
   * Note that we use explicite strings for icon location. It's to avoid loading
   * all icons at startup, we do it asynchronously to accelerate startup
   * </p>
   * 
   * @throws Exception
   */
  public static void registerTypesMplayerAvailable() throws Exception {
    // mp3
    Type type = TypeManager.getInstance().registerType(Messages.getString("Type.mp3"), EXT_MP3,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_mp3_16x16.png")
        .toExternalForm());
    // playlists
    type = TypeManager.getInstance().registerType(Messages.getString("Type.playlist"),
        EXT_PLAYLIST, Class.forName(PLAYER_IMPL_JAVALAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, false);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, false);
    // Ogg vorbis
    type = TypeManager.getInstance().registerType(Messages.getString("Type.ogg"), EXT_OGG,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_ogg_16x16.png")
        .toExternalForm());
    // Wav
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wav"), EXT_WAV,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_NO_TAGS));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_wav_16x16.png")
        .toExternalForm());
    // au
    type = TypeManager.getInstance().registerType(Messages.getString("Type.au"), EXT_AU,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_NO_TAGS));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_wav_16x16.png")
        .toExternalForm());
    // flac
    type = TypeManager.getInstance().registerType(Messages.getString("Type.flac"), EXT_FLAC,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_flac_16x16.png")
        .toExternalForm());
    // WMA
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wma"), EXT_WMA,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_wma_16x16.png")
        .toExternalForm());
    // M4A
    type = TypeManager.getInstance().registerType(Messages.getString("Type.aac"), EXT_M4A,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_aac_16x16.png")
        .toExternalForm());
    // Real audio (.rm)
    type = TypeManager.getInstance().registerType(Messages.getString("Type.real"), EXT_REAL_RM,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_ram_16x16.png")
        .toExternalForm());
    // Real audio (.ra)
    type = TypeManager.getInstance().registerType(Messages.getString("Type.real"), EXT_REAL_RA,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_ram_16x16.png")
        .toExternalForm());
    // mp2
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mp2"), EXT_MP2,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_mp2_16x16.png")
        .toExternalForm());
    // web radios
    type = TypeManager.getInstance().registerType(Messages.getString("Type.radio"), EXT_RADIO,
        Class.forName(PLAYER_IMPL_WEBRADIOS), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, true);
    // APE
    type = TypeManager.getInstance().registerType(Messages.getString("Type.ape"), EXT_APE,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_ape_16x16.png")
        .toExternalForm());
    // MAC = APE
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mac"), EXT_MAC,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_ape_16x16.png")
        .toExternalForm());
    // MPC
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mpc"), EXT_MPPLUS,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    // Change the MPC icon here if you find one
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_mp3_16x16.png")
        .toExternalForm());
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mpc"), EXT_MPP,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    // Change the MPC icon here if you find one
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_mp3_16x16.png")
        .toExternalForm());
    // -- VIDEO --
    // AVI
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_AVI,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // mpg
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_MPG,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // MP4
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_MP4,
        Class.forName(PLAYER_IMPL_MPLAYER), Class.forName(TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // mpeg
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_MPEG,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // mkv
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_MKV,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // asf
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_ASF,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // wmv
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_WMV,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // mov
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_MOV,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // ogm
    type = TypeManager.getInstance().registerType(Messages.getString("Type.video"), EXT_OGM,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_video_16x16.png")
        .toExternalForm());
    // WavPack
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wavpack"), EXT_WV,
        Class.forName(PLAYER_IMPL_MPLAYER), null);
    type.setProperty(XML_TYPE_IS_MUSIC, true);
    type.setProperty(XML_TYPE_SEEK_SUPPORTED, TRUE);
    // Official logo contains text and doesn't display well in 16x16, take wav
    // logo
    type.setProperty(XML_TYPE_ICON, UtilSystem.getResource("icons/16x16/type_wav_16x16.png")
        .toExternalForm());
  }

}
