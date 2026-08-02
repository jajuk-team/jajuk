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
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.types.Variant;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for MprisService functionality.
 * Uses JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class TestMprisService {

  @Mock
  private DBusConnection mockConnection;

  @Mock
  private JajukAction mockAction;

  private MprisService testService;
  private MockedStatic<ActionManager> mockedActionManager;

  /**
   * Setup test environment before each test.
   */
  @BeforeEach
  public void specificSetUp() throws Exception {
    // Mock the static ActionManager.getAction() method
    // Create a static mock with lenient settings so unused stubbings won't fail tests.
    mockedActionManager = mockStatic(ActionManager.class, withSettings().lenient());
    mockedActionManager.when(() -> ActionManager.getAction(JajukActions.NEXT_TRACK))
            .thenReturn(mockAction);

    // Stub DBusConnection methods
    doNothing().when(mockConnection).requestBusName(anyString());
    doNothing().when(mockConnection).exportObject(anyString(), any());
    doAnswer(invocation -> {
      // Capture sendMessage calls
      return null;
    }).when(mockConnection).sendMessage(any());

    // No need to stub perform() here; verification is enough and strict stubbing
    // will complain if a stub is unused. Keep the mock, but do not create
    // an unnecessary stubbing.

    // Create service with mock connection
    testService = new MprisService("org.mpris.MediaPlayer2.JajukTest", mockConnection);
    assertNotNull(testService, "MprisService should instantiate");
  }

  /**
   * Cleanup after each test.
   */
  @AfterEach
  public void tearDown() {
    if (mockedActionManager != null) {
      mockedActionManager.close();
    }
  }

  /**
   * Test Next() invokes NEXT_TRACK action correctly.
   */
  @Test
  public void testForward_ActionInvocation() throws Exception {
    // Arrange: Access inner object
    MprisService.MprisCompliantObject compliantObj =
            testService.getMprisCompliantObject();
    assertNotNull(compliantObj, "MprisCompliantObject accessible");

    // Act: Call Next() (MPRIS forward equivalent)
    compliantObj.Next();

    // Assert: Verify action was invoked
    verify(mockAction).perform(null);
  }

  /**
   * Test Next() sends PropertiesChanged signal via DBus.
   */
  @Test
  public void testForward_SignalSent() {
    // Arrange
    MprisService.MprisCompliantObject compliantObj =
            testService.getMprisCompliantObject();
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

    // Act
    compliantObj.Next();

    // Assert: Verify sendMessage called with correct message
    verify(mockConnection).sendMessage(messageCaptor.capture());
    org.freedesktop.dbus.messages.Message capturedMessage = messageCaptor.getValue();
    assertNotNull(capturedMessage, "Signal message captured");

    // Verify it's a PropertiesChanged signal
    assertTrue(
            capturedMessage.getClass().getSimpleName().contains("PropertiesChanged") ||
                    capturedMessage instanceof Properties.PropertiesChanged,
            "Should send PropertiesChanged signal");
  }

  /**
   * Test Next() sends correct interface signal (org.mpris.MediaPlayer2.Player).
   */
  @Test
  public void testForward_InterfaceCorrect() {
    // Arrange
    MprisService.MprisCompliantObject compliantObj =
            testService.getMprisCompliantObject();
    ArgumentCaptor<Properties.PropertiesChanged> signalCaptor =
            ArgumentCaptor.forClass(Properties.PropertiesChanged.class);

    // Act
    compliantObj.Next();

    // Assert
    verify(mockConnection).sendMessage(signalCaptor.capture());
    Properties.PropertiesChanged signal = signalCaptor.getValue();

    // Extract interface via reflection (dbus-java internals)
    String interfaceName = extractInterface(signal);
    assertEquals("org.mpris.MediaPlayer2.Player", interfaceName, "Should signal Player interface");
  }

  /**
   * Test Next() includes PlaybackStatus in changed properties.
   */
  @Test
  public void testForward_PlaybackStatusIncluded() {
    // Arrange
    MprisService.MprisCompliantObject compliantObj =
            testService.getMprisCompliantObject();
    ArgumentCaptor<Properties.PropertiesChanged> signalCaptor =
            ArgumentCaptor.forClass(Properties.PropertiesChanged.class);

    // Act
    compliantObj.Next();

    // Assert
    verify(mockConnection).sendMessage(signalCaptor.capture());
    Properties.PropertiesChanged signal = signalCaptor.getValue();

    Map<String, Variant<?>> changedProps = extractChangedProperties(signal);
    assertNotNull(changedProps, "Changed properties map exists");
    assertTrue(
            changedProps.containsKey("PlaybackStatus"),
            "PlaybackStatus property should be in signal");
  }

  /**
   * Test service remains functional after multiple Next() calls.
   */
  @Test
  public void testForward_ServiceIntegrity() {
    // Arrange
    MprisService.MprisCompliantObject compliantObj =
            testService.getMprisCompliantObject();

    // Act: Call Next() multiple times
    for (int i = 0; i < 5; i++) {
      try {
        compliantObj.Next();
      } catch (Exception e) {
        // Expected in test env - D-Bus may be unavailable
        org.jajuk.util.log.Log.debug("Next() iteration " + i + ": " + e.getMessage());
      }
    }

    // Assert: Service still accessible
    assertNotNull(testService.getMprisCompliantObject(),
            "MprisCompliantObject still accessible after multiple calls");

    // Verify sendMessage called 5 times
    verify(mockConnection, times(5)).sendMessage(any());
  }

  /**
   * Test Next() handles missing action gracefully.
   */
  @Test
  public void testForward_NoActionGracefulFail() throws Exception {
    // Arrange: Override to return null action
    mockedActionManager.when(() -> ActionManager.getAction(JajukActions.NEXT_TRACK))
            .thenReturn(null);

    // Create fresh service instance
    testService = new MprisService("org.mpris.MediaPlayer2.JajukTest2", mockConnection);
    MprisService.MprisCompliantObject compliantObj =
            testService.getMprisCompliantObject();

    // Act: Should not throw exception (handled internally)
    Exception thrown = null;
    try {
      compliantObj.Next();
    } catch (Exception e) {
      thrown = e;
    }

    // Assert: No crash, maybe logged error
    assertNull(thrown, "Should handle missing action gracefully");
  }

  /**
   * Helper: Extract interface name from PropertiesChanged signal.
   */
  private String extractInterface(Properties.PropertiesChanged signal) {
    try {
      java.lang.reflect.Field ifaceField = signal.getClass().getDeclaredField("interfaceName");
      ifaceField.setAccessible(true);
      return (String) ifaceField.get(signal);
    } catch (Exception e) {
      org.jajuk.util.log.Log.debug("Could not extract interface: " + e.getMessage());
      return null;
    }
  }

  /**
   * Helper: Extract changed properties map from signal.
   */
  private Map<String, Variant<?>> extractChangedProperties(Properties.PropertiesChanged signal) {
    try {
      java.lang.reflect.Field propsField = signal.getClass().getDeclaredField("propertiesChanged");
      propsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, Variant<?>> props = (Map<String, Variant<?>>) propsField.get(signal);
      return props;
    } catch (Exception e) {
      org.jajuk.util.log.Log.debug("Could not extract properties: " + e.getMessage());
      return null;
    }
  }
}