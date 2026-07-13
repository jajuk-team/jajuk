package org.jajuk.services.mpris;

import org.freedesktop.dbus.annotations.DBusProperties;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.jajuk.util.log.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MPRIS 2.3 compliant media controller for Jajuk.
 * Implementation compatible with dbus-java 5.2.0 using @DBusExposed annotation.
 */
public class MprisService implements Properties {

  private final DBusConnection connection;
  private static final String OBJECT_PATH = "/org/mpris/MediaPlayer2";
  private static final String BUS_NAME = "org.mpris.MediaPlayer2.jajuk";

  // State
  private final Map<String, Object> metadata = new ConcurrentHashMap<>();
  private volatile String playbackState = "Stopped";
  private volatile String loopStatus = "None";
  private volatile double volume = 1.0;
  private volatile boolean shuffle = false;

  public MprisService(DBusConnection connection) throws DBusException {
    this.connection = connection;
    Log.info("Initializing MPRIS service...");

    resetMetadata();


    // =====================================================
    // EXPLOITATION DES SOURCES FOURNIES - EXPORT CORRECT
    // =====================================================
    try {
      // ÉTAPE 1 : Obtenir le champ 'exportedObjects' de AbstractConnection (parent)
      Field exportedObjectsField = connection.getClass()
              .getSuperclass()  // AbstractConnection
              .getDeclaredField("exportedObjects");
      exportedObjectsField.setAccessible(true);

      // ÉTAPE 2 : Récupérer la map d'objets exportés
      @SuppressWarnings("unchecked")
      Map<String, Object> exportedObjects =
              (Map<String, Object>) exportedObjectsField.get(connection);

      // ÉTAPE 3 : Créer l'objet ExportedObject wrapper
      // Import depuis vos sources : org.freedesktop.dbus.messages.ExportedObject
      Class<?> exportedObjectClass = Class.forName("org.freedesktop.dbus.messages.ExportedObject");
      Object exported = exportedObjectClass.getDeclaredConstructor(
              org.freedesktop.dbus.interfaces.DBusInterface.class
      ).newInstance(this);

      // ÉTAPE 4 : Ajouter à la map
      exportedObjects.put(OBJECT_PATH, exported);

      Log.info("MPRIS service registered at " + OBJECT_PATH);

    } catch (Exception e) {
      Log.error("Failed to export MPRIS service via reflection", e);
      throw new DBusException("Export failed: " + e.getMessage(), e);
    }

    Log.info("MPRIS service exported at " + OBJECT_PATH);
  }

  private void resetMetadata() {
    metadata.put("mpris:length", 0L);
    metadata.put("xesam:title", "");
    metadata.put("xesam:artist", new String[0]);
  }

  // ============================================
  // DBusInterface Required Method
  // ============================================

  @Override
  public String getObjectPath() {
    return OBJECT_PATH;
  }

  // ============================================
  // Properties Interface - SIGNATURES EXACTES
  // ============================================

  @Override
  public Map<String, Variant<?>> GetAll(String interfaceName) {
    if (interfaceName == null || !interfaceName.contains("org.mpris")) {
      return new HashMap<>();
    }

    Map<String, Variant<?>> props = new HashMap<>();

    if ("org.mpris.MediaPlayer2".equals(interfaceName)) {
      props.put("CanQuit", new Variant<>(true));
      props.put("CanRaise", new Variant<>(true));
      props.put("HasTrackList", new Variant<>(false));
      props.put("Identity", new Variant<>("Jajuk Music Player"));
      props.put("DesktopEntry", new Variant<>("jajuk"));
    }

    if ("org.mpris.MediaPlayer2.Player".equals(interfaceName)) {
      props.put("PlaybackStatus", new Variant<>(playbackState));
      props.put("LoopStatus", new Variant<>(loopStatus));
      props.put("Shuffle", new Variant<>(shuffle));
      props.put("Volume", new Variant<>(volume));
      props.put("Position", new Variant<>(0L));
      props.put("Metadata", new Variant<>(metadata));
      props.put("CanControl", new Variant<>(true));
      props.put("CanPlay", new Variant<>(true));
      props.put("CanPause", new Variant<>(true));
      props.put("CanGoNext", new Variant<>(true));
      props.put("CanGoPrevious", new Variant<>(true));
      props.put("CanSeek", new Variant<>(false));
    }

    return props;
  }

  @Override
  public <A> A Get(String _interfaceName, String _propertyName) {
    Map<String, Variant<?>> all = GetAll(_interfaceName);
    Variant<?> variant = all.get(_propertyName);
    if (variant == null) {
      return null;
    }
    return (A) variant.getValue();
  }

  @Override
  public <A> void Set(String _interfaceName, String _propertyName, A _value) {
    Log.info("Property set: " + _propertyName + " = " + _value);

    if ("org.mpris.MediaPlayer2.Player".equals(_interfaceName)) {
      switch (_propertyName) {
        case "Volume":
          volume = ((Number) _value).doubleValue();
          updateVolume(volume);
          break;
        case "Shuffle":
          shuffle = (Boolean) _value;
          break;
        case "LoopStatus":
          loopStatus = (String) _value;
          break;
      }
    }

    Log.debug("Property changed: " + _propertyName + " in " + _interfaceName);
    // Note: Signal emission optionnel pour les fonctionnalités de base
  }

  // ============================================
  // MediaPlayer2 Interface Methods (@DBusExposed automatic)
  // ============================================

  public void Quit() {
    Log.info("Quit() called via MPRIS");
  }

  public void Raise() {
    Log.info("Raise() called via MPRIS");
  }

  public void PlayPause() {
    Log.info("PlayPause() called via MPRIS");
    // TODO: Delegate to actual player
    if ("Playing".equals(playbackState)) {
      playbackState = "Paused";
    } else {
      playbackState = "Playing";
    }
  }

  public void Play() {
    Log.info("Play() called via MPRIS");
    playbackState = "Playing";
  }

  public void Pause() {
    Log.info("Pause() called via MPRIS");
    playbackState = "Paused";
  }

  public void Stop() {
    Log.info("Stop() called via MPRIS");
    playbackState = "Stopped";
  }

  public void Next() {
    Log.info("Next() called via MPRIS");
  }

  public void Previous() {
    Log.info("Previous() called via MPRIS");
  }

  // ============================================
  // Helper Methods
  // ============================================

  private void updateVolume(double newVolume) {
    Log.info("Volume updated to: " + newVolume);
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