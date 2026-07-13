package org.jajuk.services.mpris;

import org.freedesktop.dbus.TypeRef;
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
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.log.Log;

import java.util.*;

/**
 * Implements MPRIS 2.3 specification for Linux desktop integration.
 * Provides standardized D-Bus interface for media playback control.
 */
public class MprisService2 implements Observer {

  private final String serviceName;
  private final String objectPath = "/org/mpris/MediaPlayer2";
  private final DBusConnection connection;

  // MPRIS MediaPlayer2 properties state
  private boolean canQuit = true;
  private boolean canRaise = false;  // Swing apps cannot raise
  private boolean hasTrackList = false;
  private String identity = "Jajuk Music Player";
  private String desktopEntry = "jajuk";
  private String[] supportedUriSchemes = {"file", "http"};
  private String[] supportedMimeTypes = {"audio/mpeg", "audio/flac", "audio/ogg", "audio/wav"};

  // MPRIS Player properties state
  private double volume = 0.5;
  private double rate = 1.0;
  private int minimumRate = 1;
  private int maximumRate = 1;
  private String loopStatus = "None";
  private boolean shuffle = false;

  public MprisService2(String playerName, DBusConnection connection) throws DBusException {
    this.connection = connection;
    this.serviceName = "org.mpris.MediaPlayer2." + playerName.toLowerCase();

    // Request unique bus name (will fail if another instance runs)
    connection.requestBusName(serviceName);

    // Export ONLY ONE combined object
    connection.exportObject(objectPath, new MprisCompliantObject());

    // Register as observer for player events
    ObservationManager.register(this);

  }

  // =====================================================
  // SINGLE COMBINED CLASS WITH ALL INTERFACES
  // =====================================================

  private class MprisCompliantObject implements Introspectable, Properties {

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
                  <property name="MinimumRate" type="i" access="read"/>
                  <property name="MaximumRate" type="i" access="read"/>
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
    @SuppressWarnings("unchecked")
    public <A> void Set(String _interfaceName, String _propertyName, A _value) {
      try {
        if (!"org.mpris.MediaPlayer2.Player".equals(_interfaceName)) {
          throw new RuntimeException("Cannot set properties on " + _interfaceName);
        }

        if ("Volume".equals(_propertyName)) {
          volume = Math.max(0.0, Math.min(1.0, (Double) _value));
          syncVolumeToJajuk(volume);
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
//          props.put("Metadata", getPlayerMetadata());
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
//        case "Metadata" -> getPlayerMetadata();
        case "Volume" -> getPlayerVolume();
        case "Position" -> getPlayerPosition();
        case "Rate" -> new Variant<>(rate);
        case "Shuffle" -> new Variant<>(shuffle);
        case "LoopStatus" -> new Variant<>(loopStatus);
        case "MinimumRate" -> new Variant<>(minimumRate);
        case "MaximumRate" -> new Variant<>(maximumRate);
        case "CanGoNext" -> new Variant<>(true);
        case "CanGoPrevious" -> new Variant<>(true);
        case "CanPlay" -> new Variant<>(true);
        case "CanPause" -> new Variant<>(true);
        case "CanSeek" -> new Variant<>(false);
        case "CanControl" -> new Variant<>(true);
        default -> throw new RuntimeException("Unknown property: " + propertyName);
      };
    }

    // =====================================================
    // CONTROL METHODS - Both Interfaces
    // =====================================================

    // MediaPlayer2 methods
    public void Quit() {
      Log.info("MPRIS Quit requested");
      invokeAction(JajukActions.EXIT);
    }

    public void Raise() {
      Log.info("MPRIS Raise requested");
      // Bring window to front
    }

    // Player methods
    public void Next() {
      Log.info("MPRIS Next called");
      invokeAction(JajukActions.NEXT_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Previous() {
      Log.info("MPRIS Previous called");
      invokeAction(JajukActions.PREVIOUS_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Pause() {
      Log.info("MPRIS Pause called");
      invokeAction(JajukActions.PAUSE_RESUME_TRACK);
      emitPlaybackStatusChanged();
    }

    public void PlayPause() {
      Log.info("MPRIS PlayPause called");
      invokeAction(JajukActions.PAUSE_RESUME_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Stop() {
      Log.info("MPRIS Stop called");
      invokeAction(JajukActions.STOP_TRACK);
      emitPlaybackStatusChanged();
    }

    public void Play() {
      Log.info("MPRIS Play called");
      if (QueueModel.isStopped()) {
        invokeAction(JajukActions.PAUSE_RESUME_TRACK);
      }
      emitPlaybackStatusChanged();
    }

    public void Seek(long offset) {
      Log.info("MPRIS Seek called: " + offset);
      // Not implemented
    }

    public void SetPosition(String trackId, long position) {
      Log.info("MPRIS SetPosition called: " + position);
      // Not implemented
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

    interface PropertyMetadataType extends TypeRef<Map<String, Variant<?>>> {
    }

    @SuppressWarnings("unchecked")
    private Map<String, Variant<?>> getPlayerMetadata() {
      File currentFile = QueueModel.getPlayingFile();
      if (currentFile != null) {
        return buildMetadataVariantMap(currentFile);
      }
      return new HashMap<>();
    }

    private Map<String, Variant<?>> buildMetadataVariantMap(File file) {
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

      return metadata;
    }

    // =====================================================
    // SIGNALS & SYNCHRONIZATION
    // =====================================================

    private void emitPlaybackStatusChanged() {
      try {
        Map<String, Variant<?>> changed = new HashMap<>();
        changed.put("PlaybackStatus", getPlayerPlaybackStatus());
        List<String> removed = new ArrayList<>();
        Properties.PropertiesChanged signal =
                new Properties.PropertiesChanged(objectPath, "org.mpris.MediaPlayer2.Player", changed, removed);
        // TODO
        //connection.sendSignal(signal);
        Log.debug("Emitted PropertiesChanged for PlaybackStatus");
      } catch (DBusException e) {
        Log.error("Failed to emit signal: " + e.getMessage());
      }
    }

    private void syncVolumeToJajuk(double volume) {
      Log.debug("Syncing volume: " + (volume * 100) + "%");
      // Implement your volume control API
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
        ActionManager.getAction(action).perform(null);
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
    return keys;
  }

  /**
   * Emit PropertiesChanged signal when playback status changes.
   */
  protected void emitPlaybackStatusChanged() {
    try {
      Map<String, Variant<?>> changed = new HashMap<>();
      changed.put("PlaybackStatus", getPlayerPlaybackStatus());
      List<String> removed = new ArrayList<>();
      Properties.PropertiesChanged signal =
              new Properties.PropertiesChanged(objectPath, "org.mpris.MediaPlayer2.Player", changed, removed);
      // TODO
      //connection.sendSignal(signal);
      Log.debug("Emitted PropertiesChanged for PlaybackStatus");
    } catch (DBusException e) {
      Log.error("Failed to emit signal: " + e.getMessage());
    }
  }

  /**
   * Emit PropertiesChanged signal when metadata changes.
   */
  protected void emitMetadataChanged() {
    try {
      Map<String, Variant<?>> changed = new HashMap<>();
      changed.put("Metadata", getPlayerMetadataInOuterClass());
      List<String> removed = new ArrayList<>();
      Properties.PropertiesChanged signal =
              new Properties.PropertiesChanged(objectPath, "org.mpris.MediaPlayer2.Player", changed, removed);
      // TODO
      // connection.sendSignal(signal);
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
                    QueueModel.isPlayingTrack() ? "Playing" : "Paused"
    );
  }

  protected Variant<Double> getPlayerVolume() {
    try {
      float vol = Player.getCurrentVolume() / 100.0f;
      volume = vol;
      return new Variant<>((double) vol);
    } catch (Exception e) {
      return new Variant<>(volume);
    }
  }

  protected Variant<Long> getPlayerPosition() {
    try {
      long seconds = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
      return new Variant<>(seconds * 1000000L);
    } catch (Exception e) {
      return new Variant<>(0L);
    }
  }

  protected Variant<Map<String, Variant<?>>> getPlayerMetadataInOuterClass() {
    File currentFile = QueueModel.getPlayingFile();
    if (currentFile != null) {
      return new Variant<>((Map) buildMetadataVariantMap(currentFile));
    }
    return new Variant<>(new HashMap<>());
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

    return metadata;
  }

  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();

    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      File currentFile = QueueModel.getPlayingFile();
      if (currentFile != null) {
        try {
          Map<String, Variant<?>> changed = getPlayerMetadataInMprisObject();
          List<String> removed = new ArrayList<>();
          Properties.PropertiesChanged signal =
                  new Properties.PropertiesChanged(objectPath, "org.mpris.MediaPlayer2.Player", changed, removed);
          // TODO
          //connection.sendSignal(signal);
        } catch (DBusException e) {
          Log.error("Failed to emit metadata changed: " + e.getMessage());
        }
      }
    } else if (subject.equals(JajukEvents.PLAYER_PAUSE) ||
            subject.equals(JajukEvents.PLAYER_RESUME) ||
            subject.equals(JajukEvents.PLAYER_STOP)) {
      emitPlaybackStatusChanged();
    }
  }

  private Map<String, Variant<?>> getPlayerMetadataInMprisObject() {
    File currentFile = QueueModel.getPlayingFile();
    if (currentFile != null) {
      Map<String, Variant<?>> metadata = buildMetadataVariantMap(currentFile);
      return metadata;
    }
    return new HashMap<>();
  }

  public void cleanup() {
    try {
      if (connection != null && connection.isConnected()) {
        connection.disconnect();
        connection.close();
      }
      Log.info("MPRIS service cleaned up");
    } catch (Exception e) {
      Log.error("Error during cleanup", e);
    }
  }
}
