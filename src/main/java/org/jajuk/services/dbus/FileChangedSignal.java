package org.jajuk.services.dbus;

import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Custom signal for file change notifications.
 * Must extend DBusSignal to be compatible with dbus-java 5.2 (hypfvieh)
 */
public class FileChangedSignal extends DBusSignal {

  private final String filepath;

  /**
   * Constructs a new FileChangedSignal.
   * The path parameter must match the export path "/JajukDBus".
   *
   * @param filepath the file path information to send
   * @throws DBusException if there's an error constructing the signal
   */
  public FileChangedSignal(String filepath) throws DBusException {
    // Super constructor takes: path, ...args
    // The first arg after path is typically implicit for standard DBusSignal
    super("/JajukDBus", filepath);
    this.filepath = filepath;
  }

  /**
   * Gets the file path stored in this signal.
   * Used for serialization/deserialization by dbus-java.
   *
   * @return the filepath
   */
  public String getFilepath() {
    return filepath;
  }
}