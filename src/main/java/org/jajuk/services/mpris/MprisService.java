/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
 *
 */
package org.jajuk.services.mpris;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Introspectable;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.log.Log;

import javax.swing.*;
import java.util.*;

/**
 * Implements MPRIS 2.3 specification for Linux desktop integration.
 * Provides standardized D-Bus interface for media playback control.
 */
public class MprisService implements Observer {

  private final String objectPath = "/org/mpris/MediaPlayer2";
  private final DBusConnection connection;
  private final MprisCompliantObject mprisCompliantObject;

  // MPRIS MediaPlayer2 properties state
  private final boolean canQuit = true;
  private final boolean canRaise = true;
  private final boolean hasTrackList = false;
  private final String identity = "Jajuk Music Player";
  private final String desktopEntry = "jajuk";
  private final String[] supportedUriSchemes = {"file", "http"};
  private final String[] supportedMimeTypes = {"audio/mpeg", "audio/flac", "audio/ogg", "audio/wav"};

  // MPRIS Player properties state
  private double volume = 0.5;
  private final double rate = 1.0;
  private final double minimumRate = 1;
  private final double maximumRate = 1;
  private String loopStatus = "None";
  private boolean shuffle = false;

  public MprisService(String busName, DBusConnection connection) throws DBusException {
    this.connection = connection;

    // Request unique bus name (will fail if another instance runs)
    connection.requestBusName(busName);

    // Export ONLY ONE combined object
    mprisCompliantObject = new MprisCompliantObject();
    connection.exportObject(objectPath, mprisCompliantObject);

    // Register as observer for player events
    ObservationManager.register(this);
  }

  // =====================================================
  // SINGLE COMBINED CLASS WITH ALL INTERFACES
  // =====================================================
  protected class MprisCompliantObject implements Introspectable, Properties, MprisPlayerInterface {

    @Override
    public String getObjectPath() {
      return objectPath;
    }

    @Override
    public String Introspect() {
      return """
              <?xml version="1.0" encoding="utf-8"?>
              <!DOCTYPE node PUBLIC "-//freedesktop//DTD D-BUS Object Introspection 1.0//EN"
               "http://www.freedesktop.org/standards/dbus/1.0/introspect.dtd">
              <node>
                <interface name="org.freedesktop.DBus.Introspectable">
                  <method name="Introspect">
                    <arg direction="out" type="s"/>
                  </method>
                </interface>
                <interface name="org.freedesktop.DBus.Peer">
                  <method name="Ping"/>
                  <method name="GetMachineId">
                    <arg direction="out" type="s"/>
                  </method>
                </interface>
                <interface name="org.freedesktop.DBus.Properties">
                  <method name="Get">
                    <arg direction="in" type="s"/>
                    <arg direction="in" type="s"/>
                    <arg direction="out" type="v"/>
                  </method>
                  <method name="Set">
                    <arg direction="in" type="s"/>
                    <arg direction="in" type="s"/>
                    <arg direction="in" type="v"/>
                  </method>
                  <method name="GetAll">
                    <arg direction="in" type="s"/>
                    <arg direction="out" type="a{sv}"/>
                  </method>
                  <signal name="PropertiesChanged">
                    <arg type="s"/>
                    <arg type="a{sv}"/>
                    <arg type="as"/>
                  </signal>
                </interface>
                <interface name="org.mpris.MediaPlayer2">
                  <method name="Raise"/>
                  <method name="Quit"/>
                  <property name="CanQuit" type="b" access="read"/>
                  <property name="CanRaise" type="b" access="read"/>
                  <property name="HasTrackList" type="b" access="read"/>
                  <property name="Identity" type="s" access="read"/>
                  <property name="DesktopEntry" type="s" access="read"/>
                  <property name="SupportedUriSchemes" type="as" access="read"/>
                  <property name="SupportedMimeTypes" type="as" access="read"/>
                </interface>
                <interface name="org.mpris.MediaPlayer2.Player">
                  <method name="Next"/>
                  <method name="Previous"/>
                  <method name="Pause"/>
                  <method name="PlayPause"/>
                  <method name="Stop"/>
                  <method name="Play"/>
                  <method name="Seek"><arg direction="in" type="x"/></method>
                  <method name="SetPosition"><arg direction="in" type="o"/><arg direction="in" type="x"/></method>
                  <signal name="Seeked"><arg type="x"/></signal>
                  <property name="PlaybackStatus" type="s" access="read"/>
                  <property name="LoopStatus" type="s" access="readwrite"/>
                  <property name="Shuffle" type="b" access="readwrite"/>
                  <property name="Rate" type="d" access="readwrite"/>
                  <property name="MinimumRate" type="d" access="read"/>
                  <property name="MaximumRate" type="d" access="read"/>
                  <property name="Volume" type="d" access="readwrite"/>
                  <property name="Position" type="x" access="read"/>
                  <property name="Metadata" type="a{sv}" access="read"/>
                  <property name="CanGoNext" type="b" access="read"/>
                  <property name="CanGoPrevious" type="b" access="read"/>
                  <property name="CanPlay" type="b" access="read"/>
                  <property name="CanPause" type="b" access="read"/>
                  <property name="CanSeek" type="b" access="read"/>
                  <property name="CanControl" type="b" access="read"/>
                </interface>
              </node>
              """;
    }

    // =====================================================
    // Properties INTERFACE
    // =====================================================

    @Override
    @SuppressWarnings("unchecked")
    public <A> A Get(String _interfaceName, String _propertyName) {
      try {
        if ("org.mpris.MediaPlayer2".equals(_interfaceName)) {
          return (A) getMediaPlayer2Property(_propertyName);
        } else if ("org.mpris.MediaPlayer2.Player".equals(_interfaceName)) {
          return (A) getPlayerProperty(_propertyName);
        } else {
          throw new RuntimeException("Unknown interface: " + _interfaceName);
        }
      } catch (Exception e) {
        Log.error("Error in Get(): " + e.getMessage());
        throw new RuntimeException(e);
      }
    }

    @Override
    public <A> void Set(String _interfaceName, String _propertyName, A _value) {
      try {
        if (!"org.mpris.MediaPlayer2.Player".equals(_interfaceName)) {
          throw new RuntimeException("Cannot set properties on " + _interfaceName);
        }

        if ("Volume".equals(_propertyName)) {
          volume = Math.max(0.0, Math.min(1.0, (Double) _value));
          syncVolumeToJajuk((float) volume);
        } else if ("Shuffle".equals(_propertyName)) {
          shuffle = (Boolean) _value;
          syncShuffleToJajuk(shuffle);
        } else if ("LoopStatus".equals(_propertyName)) {
          loopStatus = (String) _value;
          syncLoopToJajuk(loopStatus);
        } else {
          throw new RuntimeException("Cannot set property: " + _propertyName);
        }
      } catch (Exception e) {
        Log.error("Error in Set(): " + e.getMessage());
        throw new RuntimeException(e);
      }
    }

    @Override
    public Map<String, Variant<?>> GetAll(String _interfaceName) {
      Map<String, Variant<?>> props = new HashMap<>();

      try {
        if ("org.mpris.MediaPlayer2".equals(_interfaceName)) {
          props.put("CanQuit", new Variant<>(canQuit));
          props.put("CanRaise", new Variant<>(canRaise));
          props.put("HasTrackList", new Variant<>(hasTrackList));
          props.put("Identity", new Variant<>(identity));
          props.put("DesktopEntry", new Variant<>(desktopEntry));
          props.put("SupportedUriSchemes", new Variant<>(supportedUriSchemes));
          props.put("SupportedMimeTypes", new Variant<>(supportedMimeTypes));
        } else if ("org.mpris.MediaPlayer2.Player".equals(_interfaceName)) {
          props.put("PlaybackStatus", getPlayerPlaybackStatus());
          props.put("Metadata", getPlayerMetadata());
          props.put("Volume", getPlayerVolume());
          props.put("Position", getPlayerPosition());
          props.put("Rate", new Variant<>(rate));
          props.put("Shuffle", new Variant<>(shuffle));
          props.put("LoopStatus", new Variant<>(loopStatus));
          props.put("MinimumRate", new Variant<>(minimumRate));
          props.put("MaximumRate", new Variant<>(maximumRate));
          props.put("CanGoNext", new Variant<>(true));
          props.put("CanGoPrevious", new Variant<>(true));
          props.put("CanPlay", new Variant<>(true));
          props.put("CanPause", new Variant<>(true));
          props.put("CanSeek", new Variant<>(false));
          props.put("CanControl", new Variant<>(true));
        } else {
          throw new RuntimeException("Unknown interface: " + _interfaceName);
        }
      } catch (Exception e) {
        Log.error("Error in GetAll(): " + e.getMessage());
        throw new RuntimeException(e);
      }

      return props;
    }

    // =====================================================
    // PROPERTY HELPERS - Returns Variant or primitive
    // =====================================================

    private Variant<?> getMediaPlayer2Property(String propertyName) {
      return switch (propertyName) {
        case "CanQuit" -> new Variant<>(canQuit);
        case "CanRaise" -> new Variant<>(canRaise);
        case "HasTrackList" -> new Variant<>(hasTrackList);
        case "Identity" -> new Variant<>(identity);
        case "DesktopEntry" -> new Variant<>(desktopEntry);
        case "SupportedUriSchemes" -> new Variant<>(supportedUriSchemes);
        case "SupportedMimeTypes" -> new Variant<>(supportedMimeTypes);
        default -> throw new RuntimeException("Unknown property: " + propertyName);
      };
    }

    private Variant<?> getPlayerProperty(String propertyName) {
      return switch (propertyName) {
        case "PlaybackStatus" -> getPlayerPlaybackStatus();
        case "Metadata" -> getPlayerMetadata();
        case "Volume" -> getPlayerVolume();
        case "Position" -> getPlayerPosition();
        case "Rate" -> new Variant<>(rate);
        case "Shuffle" -> new Variant<>(shuffle);
        case "LoopStatus" -> new Variant<>(loopStatus);
        case "MinimumRate" -> new Variant<>(minimumRate);
        case "MaximumRate" -> new Variant<>(maximumRate);
        case "CanGoNext",
             "CanGoPrevious",
             "CanPlay",
             "CanPause",
             "CanControl" -> new Variant<>(true);
        case "CanSeek" -> new Variant<>(false);
        default -> throw new RuntimeException("Unknown property: " + propertyName);
      };
    }

    // =====================================================
    // CONTROL METHODS - Both Interfaces
    // =====================================================

    // MediaPlayer2 methods
    public void Quit() {
      Log.info("MPRIS Quit requested");
      // Asynchronous call
      SwingUtilities.invokeLater(() -> {
        try {
          Thread.sleep(100); // Delay to allow D-Bus anwser
          Log.info("Closing Jajuk after MPRIS Quit...");
          invokeAction(JajukActions.EXIT);
        } catch (Exception e) {
          Log.error("Error during quit: " + e.getMessage());
        }
      });
    }

    public void Raise() {
      Log.info("MPRIS Raise requested");
      // Bring window to front
      SwingUtilities.invokeLater(() -> {
        try {
          // Obtenir la fenêtre principale de Jajuk
          JFrame mainWindow = JajukMainWindow.getInstance();

          if (mainWindow != null) {
            // Rendre la fenêtre visible si elle ne l'est pas
            mainWindow.setVisible(true);

            // Déminimiser si nécessaire
            if (mainWindow.getExtendedState() == JFrame.ICONIFIED) {
              mainWindow.setExtendedState(JFrame.NORMAL);
            }

            // Demander le focus et mettre au premier plan
            mainWindow.toFront();
            mainWindow.requestFocusInWindow();

            Log.debug("Jajuk window brought to front successfully");
          } else {
            Log.warn("Main window not found, cannot raise Jajuk");
          }
        } catch (Exception e) {
          Log.error("Failed to raise Jajuk window: " + e.getMessage());
        }
      });
    }

    // Player methods
    public void Next() {
      invokeAction(JajukActions.NEXT_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Previous() {
      invokeAction(JajukActions.PREVIOUS_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Pause() {
      invokeAction(JajukActions.PAUSE_RESUME_TRACK);
      emitPlaybackStatusChanged();
    }

    public void PlayPause() {
      invokeAction(JajukActions.PAUSE_RESUME_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Stop() {
      invokeAction(JajukActions.STOP_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Play() {
      if (QueueModel.isStopped()) {
        invokeAction(JajukActions.PAUSE_RESUME_TRACK);
      }
      emitPlaybackStatusChanged();
    }

    public void Seek(long offset) {
      Log.info("MPRIS Seek Not Implemented");
    }

    public void SetPosition(String trackId, long position) {
      Log.info("MPRIS SetPosition Not Implemented");
    }

    // =====================================================
    // STATE ACCESSORS
    // =====================================================

    private Variant<String> getPlayerPlaybackStatus() {
      return new Variant<>(
              QueueModel.isStopped() ? "Stopped" :
                      QueueModel.isPlayingTrack() ? "Playing" : "Paused"
      );
    }

    private Variant<Double> getPlayerVolume() {
      try {
        float vol = Player.getCurrentVolume() / 100.0f;
        volume = vol;
        return new Variant<>((double) vol);
      } catch (Exception e) {
        return new Variant<>(volume);
      }
    }

    private Variant<Long> getPlayerPosition() {
      try {
        long seconds = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
        return new Variant<>(seconds * 1000000L);
      } catch (Exception e) {
        return new Variant<>(0L);
      }
    }

    // =====================================================
    // SIGNALS & SYNCHRONIZATION
    // =====================================================

    private void syncVolumeToJajuk(float volume) {
      Log.debug("Syncing volume: " + (volume * 100) + "%");
      // Implement your volume control API
      Player.setVolume(volume);
    }

    private void syncShuffleToJajuk(boolean shuffle) {
      Log.debug("Syncing shuffle: " + shuffle);
      invokeAction(JajukActions.SHUFFLE_GLOBAL);
    }

    private void syncLoopToJajuk(String loopStatus) {
      Log.debug("Syncing loop: " + loopStatus);
      // Sync with repeat mode
    }

    private void invokeAction(JajukActions action) {
      try {
        // Retrieve the action and guard against null (tests may mock static method
        // to return null to verify graceful handling).
        org.jajuk.ui.actions.JajukAction jajukAction = ActionManager.getAction(action);
        if (jajukAction == null) {
          Log.error("Action not found: " + action);
          return;
        }
        jajukAction.perform(null);
      } catch (Exception e) {
        Log.error("Action failed: " + e.getMessage());
      }
    }
  }

  // =====================================================
  // OBSERVER
  // =====================================================

  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> keys = new HashSet<>();
    keys.add(JajukEvents.FILE_LAUNCHED);
    keys.add(JajukEvents.TRACK_CHANGED);
    keys.add(JajukEvents.VOLUME_CHANGED);
    keys.add(JajukEvents.PLAYER_PAUSE);
    keys.add(JajukEvents.PLAYER_RESUME);
    keys.add(JajukEvents.PLAYER_STOP);
    keys.add(JajukEvents.WEBRADIO_LAUNCHED);
    return keys;
  }

  /**
   * Emit PropertiesChanged signal when playback status changes.
   */
  protected void emitPlaybackStatusChanged() {
    try {
      Map<String, Variant<?>> changed = new HashMap<>();
      Variant<String> playerPlaybackStatus = getPlayerPlaybackStatus();
      changed.put("PlaybackStatus", playerPlaybackStatus);
      List<String> removed = new ArrayList<>();
      Properties.PropertiesChanged signal =
              new Properties.PropertiesChanged(objectPath, "org.mpris.MediaPlayer2.Player", changed, removed);
      connection.sendMessage(signal);
      Log.debug("Emitted PropertiesChanged for PlaybackStatus : " + playerPlaybackStatus.getValue());
    } catch (Exception e) {
      Log.error("Failed to emit signal: " + e.getMessage());
    }
  }

  /**
   * Emit PropertiesChanged signal when metadata changes.
   */
  protected void emitMetadataChanged() {
    try {
      Map<String, Variant<?>> changed = new HashMap<>();
      changed.put("Metadata", getPlayerMetadata());
      List<String> removed = new ArrayList<>();
      Properties.PropertiesChanged signal =
              new Properties.PropertiesChanged(objectPath, "org.mpris.MediaPlayer2.Player", changed, removed);
      connection.sendMessage(signal);
      Log.debug("Emitted PropertiesChanged for Metadata");
    } catch (DBusException e) {
      Log.error("Failed to emit metadata signal: " + e.getMessage());
    }
  }

  // =====================================================
  // HELPER METHODS (Moved to outer class for accessibility)
  // =====================================================

  protected Variant<String> getPlayerPlaybackStatus() {
    return new Variant<>(
            QueueModel.isStopped() ? "Stopped" :
                    Player.isPaused() ? "Paused" : "Playing"
    );
  }

  protected Variant<Map<String, Variant<?>>> getPlayerMetadata() {
    if (QueueModel.isPlayingTrack()) {
      File currentFile = QueueModel.getPlayingFile();
      if (currentFile != null) {
        return new Variant<>(buildMetadataVariantMap(currentFile), "a{sv}");
      }
    } else if (QueueModel.isPlayingRadio()) {
      return new Variant<>(buildMetadataVariantMapWebRadio(), "a{sv}");
    }
    return new Variant<>(new HashMap<>(), "a{sv}");
  }

  protected Map<String, Variant<?>> buildMetadataVariantMap(File file) {
    Track track = file.getTrack();
    Map<String, Variant<?>> metadata = new HashMap<>();

    metadata.put("mpris:trackid", new Variant<>(objectPath + "/track/" + track.getID()));
    metadata.put("mpris:length", new Variant<>(track.getDuration() * 1000000L));
    metadata.put("xesam:title", new Variant<>(track.getName()));

    if (track.getArtist() != null && !track.getArtist().seemsUnknown()) {
      metadata.put("xesam:artist", new Variant<>(new String[]{track.getArtist().getName()}));
    }

    if (track.getAlbum() != null && !track.getAlbum().seemsUnknown()) {
      metadata.put("xesam:album", new Variant<>(track.getAlbum().getName()));
    }

    // Get album cover art URL for MPRIS notification display
    if (track.getAlbum() != null) {
      java.io.File cover = track.getAlbum().findCover();
      if (cover != null) {
        try {
          String artUrl = cover.toURI().toURL().toString();
          metadata.put("mpris:artUrl", new Variant<>(artUrl));
        } catch (java.net.MalformedURLException e) {
          Log.warn("Cannot build artUrl for cover: " + cover, e);
        }
      }
    }
    return metadata;
  }

  protected Map<String, Variant<?>> buildMetadataVariantMapWebRadio() {
    WebRadio webRadio = QueueModel.getCurrentRadio();
    Map<String, Variant<?>> metadata = new HashMap<>();
    // Empty information
    metadata.put("mpris:trackid", new Variant<>(objectPath + "/track/" + 0));
    // Empty information
    metadata.put("mpris:length", new Variant<>(0));
    // The name of the web radio
    metadata.put("xesam:title", new Variant<>(webRadio.getName()));
    // Use of the title of the web radio as artist, and genre as album for MPRIS metadata
    metadata.put("xesam:artist", new Variant<>(new String[]{webRadio.getTitle()}));
    metadata.put("xesam:album", new Variant<>(webRadio.getGenre()));
    return metadata;
  }

  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();

    if (subject.equals(JajukEvents.FILE_LAUNCHED) ||
            subject.equals(JajukEvents.PLAYER_STOP) ||
            subject.equals(JajukEvents.WEBRADIO_LAUNCHED)) {
      emitMetadataChanged();
      emitPlaybackStatusChanged();
    } else if (subject.equals(JajukEvents.PLAYER_PAUSE) ||
            subject.equals(JajukEvents.PLAYER_RESUME)) {
      emitPlaybackStatusChanged();
    }
  }

  /**
   * Expose the MprisCompliantObject for testing purposes.
   *
   * @return the MprisCompliantObject instance
   */
  protected MprisCompliantObject getMprisCompliantObject() {
    return mprisCompliantObject;
  }

}
