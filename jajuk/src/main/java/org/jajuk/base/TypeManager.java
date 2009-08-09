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
import java.util.Locale;
import java.util.Map;

import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Manages types ( mp3, ogg...) supported by jajuk
 * <p>
 */
public final class TypeManager extends ItemManager {
  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_APE = "icons/16x16/type_ape_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_MP2 = "icons/16x16/type_mp2_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_AAC = "icons/16x16/type_aac_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_WMA = "icons/16x16/type_wma_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_FLAC = "icons/16x16/type_flac_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_RAM = "icons/16x16/type_ram_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_OGG = "icons/16x16/type_ogg_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_WAV = "icons/16x16/type_wav_16x16.png";

  /**
   * 
   */
  private static final String ICONS_16X16_TYPE_MP3 = "icons/16x16/type_mp3_16x16.png";

  /** extensions->types */
  private final Map<String, Type> hmSupportedTypes = new HashMap<String, Type>(10);

  /** Self instance */
  private static TypeManager singleton;

  /**
   * No constructor available, only static access
   */
  private TypeManager() {
    super();
    // ---register properties---
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, false, false,
        String.class, null));
    // Extension
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE_EXTENSION, false, true, true,
        false, false, String.class, null));
    // Player impl
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE_PLAYER_IMPL, false, true, true,
        false, false, Class.class, null));
    // Tag impl
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE_TAG_IMPL, false, true, true, false,
        false, Class.class, null));
    // Music
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE_IS_MUSIC, false, false, true,
        false, false, Boolean.class, null));
    // Seek
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE_SEEK_SUPPORTED, false, false, true,
        false, false, Boolean.class, null));
    // Icon
    registerProperty(new PropertyMetaInformation(Const.XML_TYPE_ICON, false, false, false, false,
        false, String.class, null));
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
  @SuppressWarnings("unchecked")
  public Type registerType(String sName, String sExtension, Class<?> cPlayerImpl, Class<?> cTagImpl) {
    return registerType(sExtension.toLowerCase(Locale.getDefault()), sName, sExtension,
        (Class<IPlayerImpl>) cPlayerImpl, (Class<ITagImpl>) cTagImpl);
  }

  /**
   * Register a type jajuk can read with a known id
   * 
   * @param type
   */
  private Type registerType(String sId, String sName, String sExtension,
      Class<IPlayerImpl> cPlayerImpl, Class<ITagImpl> cTagImpl) {
    Type type = getTypeByID(sId);
    if (type != null) {
      return type;
    }
    if (hmSupportedTypes.containsKey(sExtension)) {
      // if the type is already in memory, use it
      return hmSupportedTypes.get(sExtension);
    }
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
    return hmSupportedTypes.containsKey(sExt.toLowerCase(Locale.getDefault()));
  }

  /**
   * Return type for a given extension
   * 
   * @param sExtension
   * @return
   */
  public Type getTypeByExtension(String sExtension) {
    return hmSupportedTypes.get(sExtension.toLowerCase(Locale.getDefault()));
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
      if (type.getBooleanValue(Const.XML_TYPE_IS_MUSIC)) {
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
    return Const.XML_TYPES;
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
    return (List<Type>) getItems();
  }

  /**
   * 
   * @return types iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<Type> getTypesIterator() {
    return new ReadOnlyIterator<Type>((Iterator<Type>) getItemsIterator());
  }

  /**
   * Convenient method to register all types when mplayer is not available
   * <p>
   * Note that we use explicite strings for icon location. It's to avoid loading
   * all icons at startup, we do it asynchronously to accelerate startup
   * </p>
   * 
   * @throws ClassNotFoundException
   * 
   */
  public static void registerTypesNoMplayer() throws ClassNotFoundException {
    // mp3
    Type type = TypeManager.getInstance().registerType(Messages.getString("Type.mp3"),
        Const.EXT_MP3, Class.forName(Const.PLAYER_IMPL_JAVALAYER),
        Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    // Do not use IconLoader to get icon, it takes too much time to
    // load all icons
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_MP3)
        .toExternalForm());
    // playlists
    type = TypeManager.getInstance().registerType(Messages.getString("Type.playlist"),
        Const.EXT_PLAYLIST, Class.forName(Const.PLAYER_IMPL_JAVALAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, false);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, false);
    // Ogg vorbis
    type = TypeManager.getInstance().registerType(Messages.getString("Type.ogg"), Const.EXT_OGG,
        Class.forName(Const.PLAYER_IMPL_JAVALAYER), Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, false);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_OGG)
        .toExternalForm());
    // Wave
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wav"), Const.EXT_WAV,
        Class.forName(Const.PLAYER_IMPL_JAVALAYER), Class.forName(Const.TAG_IMPL_NO_TAGS));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_WAV)
        .toExternalForm());
    // au
    type = TypeManager.getInstance().registerType(Messages.getString("Type.au"), Const.EXT_AU,
        Class.forName(Const.PLAYER_IMPL_JAVALAYER), Class.forName(Const.TAG_IMPL_NO_TAGS));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, false);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_WAV)
        .toExternalForm());
  }

  /**
   * Convenient method to register all types when mplayer is available.
   * <p>
   * Note that we use explicite strings for icon location. It's to avoid loading
   * all icons at startup, we do it asynchronously to accelerate startup
   * </p>
   * 
   * @throws ClassNotFoundException
   * 
   * @throws Exception
   */
  public static void registerTypesMplayerAvailable() throws ClassNotFoundException {
    // mp3
    Type type = TypeManager.getInstance().registerType(Messages.getString("Type.mp3"),
        Const.EXT_MP3, Class.forName(Const.PLAYER_IMPL_MPLAYER),
        Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_MP3)
        .toExternalForm());
    // playlists
    type = TypeManager.getInstance().registerType(Messages.getString("Type.playlist"),
        Const.EXT_PLAYLIST, Class.forName(Const.PLAYER_IMPL_JAVALAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, false);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, false);
    // Ogg vorbis
    type = TypeManager.getInstance().registerType(Messages.getString("Type.ogg"), Const.EXT_OGG,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_OGG)
        .toExternalForm());
    // Wav
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wav"), Const.EXT_WAV,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), Class.forName(Const.TAG_IMPL_NO_TAGS));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_WAV)
        .toExternalForm());
    // au
    type = TypeManager.getInstance().registerType(Messages.getString("Type.au"), Const.EXT_AU,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), Class.forName(Const.TAG_IMPL_NO_TAGS));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_WAV)
        .toExternalForm());
    // flac
    type = TypeManager.getInstance().registerType(Messages.getString("Type.flac"), Const.EXT_FLAC,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_FLAC)
        .toExternalForm());
    // WMA
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wma"), Const.EXT_WMA,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_WMA)
        .toExternalForm());
    // M4A
    type = TypeManager.getInstance().registerType(Messages.getString("Type.aac"), Const.EXT_M4A,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_AAC)
        .toExternalForm());
    // Real audio (.rm)
    type = TypeManager.getInstance().registerType(Messages.getString("Type.real"),
        Const.EXT_REAL_RM, Class.forName(Const.PLAYER_IMPL_MPLAYER),
        Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_RAM)
        .toExternalForm());
    // Real audio (.ra)
    type = TypeManager.getInstance().registerType(Messages.getString("Type.real"),
        Const.EXT_REAL_RA, Class.forName(Const.PLAYER_IMPL_MPLAYER),
        Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_RAM)
        .toExternalForm());
    // mp2
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mp2"), Const.EXT_MP2,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_MP2)
        .toExternalForm());
    // web radios
    type = TypeManager.getInstance().registerType(Messages.getString("Type.radio"),
        Const.EXT_RADIO, Class.forName(Const.PLAYER_IMPL_WEBRADIOS), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, true);
    // APE
    type = TypeManager.getInstance().registerType(Messages.getString("Type.ape"), Const.EXT_APE,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_APE)
        .toExternalForm());
    // MAC = APE
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mac"), Const.EXT_MAC,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_APE)
        .toExternalForm());
    // MPC
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mpc"), Const.EXT_MPC,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    // Change the MPC icon here if you find one
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_MP3)
        .toExternalForm());
    // MP+
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mpc"), Const.EXT_MPPLUS,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    // Change the MPC icon here if you find one
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_MP3)
        .toExternalForm());
    // MPP
    type = TypeManager.getInstance().registerType(Messages.getString("Type.mpc"), Const.EXT_MPP,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    // Change the MPC icon here if you find one
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_MP3)
        .toExternalForm());
    // WavPack
    type = TypeManager.getInstance().registerType(Messages.getString("Type.wavpack"), Const.EXT_WV,
        Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    // Official logo contains text and doesn't display well in 16x16, take wav
    // logo
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem.getResource(ICONS_16X16_TYPE_WAV)
        .toExternalForm());

    // -- VIDEO --
    // AVI
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_AVI, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // mpg
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_MPG, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // MP4
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_MP4, Class.forName(Const.PLAYER_IMPL_MPLAYER),
        Class.forName(Const.TAG_IMPL_JAUDIOTAGGER));
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // mpeg
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_MPEG, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // mkv
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_MKV, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // asf
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_ASF, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // wmv
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_WMV, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // mov
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_MOV, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
    // ogm
    type = TypeManager.getInstance().registerType(Messages.getString(Const.TYPE_VIDEO),
        Const.EXT_OGM, Class.forName(Const.PLAYER_IMPL_MPLAYER), null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);
    type.setProperty(Const.XML_TYPE_SEEK_SUPPORTED, Const.TRUE);
    type.setProperty(Const.XML_TYPE_ICON, UtilSystem
        .getResource("icons/16x16/type_video_16x16.png").toExternalForm());
  }

}
