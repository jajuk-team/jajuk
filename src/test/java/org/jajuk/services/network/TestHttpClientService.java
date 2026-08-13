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
 */
package org.jajuk.services.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;


import java.io.File;
import java.io.IOException;
import java.net.Proxy;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Unit tests for HttpClientService.
 */
public class TestHttpClientService {

  private static final String URL = "http://www.google.com/";
  private static final String INVALID_URL = "ftp://www.google.com/"; // Should fail gracefully or throw

  private HttpClientService service;

  @BeforeEach
  public void setUp() {
    // Reset the singleton instance to ensure clean state
    HttpClientService.resetInstance();
    service = HttpClientService.getInstance();

    // Ensure internet access is allowed for tests
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
    service.updateInternetAccessSetting();

    // Clear any proxy config from previous runs
    service.setProxyConfig(null);
  }

  @AfterEach
  public void tearDown() {
    HttpClientService.resetInstance();
    // Restore original config if needed
    Conf.removeProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS);
  }

  /**
   * Test internet access disabled flag.
   */
  @Test
  public void testInternetAccessDisabled() {
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "true");
    service.updateInternetAccessSetting();

    assertFalse(service.isInternetAccessAllowed(), "Internet access should be disabled");

    // Calls should return null immediately without network call
    try {
      assertNull(service.readUrl(URL));
      assertNull(service.getImage(URL));
      assertNull(service.executeGetRequest(URL));
    } catch (IOException e) {
      fail("Should not throw IOException when internet is disabled: " + e.getMessage());
    }
  }

  /**
   * Test basic GET request success.
   */
  @Test
  public void testReadUrlSuccess() throws IOException {
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
    service.updateInternetAccessSetting();

    assertNotNull(service, "Service should be initialized");

    String content = service.readUrl(URL);
    assertNotNull(content, "Content should not be null for valid URL");
    assertTrue(StringUtils.isNotBlank(content), "Content should not be empty");
  }

  /**
   * Test invalid URL handling.
   */
  @Test
  public void testInvalidUrl() throws IOException {
    assertThrows(IllegalArgumentException.class, () -> service.readUrl(INVALID_URL));
  }

  /**
   * Test POST request.
   */
  @Test
  public void testPostUrl() {
    try {
      // postman-echo.com is just a site for test
      String result = service.postUrl("https://postman-echo.com/post", "test=data");
      // If successful, assert not null. If 4xx/5xx, IOException is thrown (as per implementation).
      assertNotNull(result);
    } catch (IOException e) {
      // We can't assume the site is up and running so if this happen, we log it but don't fail the test.
      Log.debug("POST test endpoint rejected or failed (expected in some environments): " + e.getMessage());
    }
  }

  /**
   * Test image download.
   */
  @Test
  public void testGetImage() throws IOException {
    // Use a known valid image URL for testing
    String githubLogoUrl = "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png";

    java.awt.Image img = service.getImage(githubLogoUrl);
    assertNotNull(img, "Image should not be null");
  }

  /**
   * Test file download.
   */
  @Test
  public void testDownloadToFile() throws IOException {
    File tempFile = File.createTempFile("jajuk-test-", ".txt");
    tempFile.deleteOnExit();

    String url = "http://jajuk.info";
    service.downloadToFile(url, tempFile);

    assertTrue(tempFile.exists(), "File should exist after download");
    assertTrue(tempFile.length() > 0, "File should not be empty");
  }

  /**
   * Test Proxy Configuration.
   */
  @Test
  public void testProxyConfiguration() {
    // Create a mock proxy config (no actual connection tested here, just setting)
    ProxyConfig config = new ProxyConfig(
            Proxy.Type.HTTP,
            "localhost",
            8080,
            "user",
            "password"
    );

    service.setProxyConfig(config);

    ProxyConfig retrieved = service.getProxyConfig();
    assertNotNull(retrieved, "Proxy config should be set");
    assertEquals("localhost", retrieved.getHost(), "Host should match");
    assertEquals(8080, retrieved.getPort(), "Port should match");
    assertTrue(retrieved.getUsername().isPresent(), "User should be present");
    assertTrue(retrieved.getPassword().isPresent(), "Password should be present");
  }

  /**
   * Test encodeString utility.
   */
  @Test
  public void testEncodeString() {
    assertEquals("teststring", service.encode("teststring"));
    // Check encoding of special characters
    String encoded = service.encode("test&!@#");
    assertTrue(encoded.contains("%26"), "Encoded string should contain %26 for &");
  }

  /**
   * Test HEAD request.
   */
  @Test
  public void testHeadRequest() throws IOException {
    var response = service.headRequest(URL);
    assertNotNull(response, "HEAD response should not be null");
    assertTrue(response.statusCode() >= 200 && response.statusCode() < 400, "Status code should be valid (e.g., 200)");
  }
}