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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

  @Before
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

  @After
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

    assertFalse("Internet access should be disabled", service.isInternetAccessAllowed());

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

    assertNotNull("Service should be initialized", service);

    String content = service.readUrl(URL);
    assertNotNull("Content should not be null for valid URL", content);
    assertTrue("Content should not be empty", StringUtils.isNotBlank(content));
  }

  /**
   * Test invalid URL handling.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUrl() throws IOException {
    service.readUrl(INVALID_URL);
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
    assertNotNull("Image should not be null", img);
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

    assertTrue("File should exist after download", tempFile.exists());
    assertTrue("File should not be empty", tempFile.length() > 0);
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
    assertNotNull("Proxy config should be set", retrieved);
    assertEquals("Host should match", "localhost", retrieved.getHost());
    assertEquals("Port should match", 8080, retrieved.getPort());
    assertTrue("User should be present", retrieved.getUsername().isPresent());
    assertTrue("Password should be present", retrieved.getPassword().isPresent());
  }

  /**
   * Test encodeString utility.
   */
  @Test
  public void testEncodeString() {
    assertEquals("teststring", service.encode("teststring"));
    // Check encoding of special characters
    String encoded = service.encode("test&!@#");
    assertTrue("Encoded string should contain %26 for &", encoded.contains("%26"));
  }

  /**
   * Test HEAD request.
   */
  @Test
  public void testHeadRequest() throws IOException {
    var response = service.headRequest(URL);
    assertNotNull("HEAD response should not be null", response);
    assertTrue("Status code should be valid (e.g., 200)", response.statusCode() >= 200 && response.statusCode() < 400);
  }
}