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
package org.jajuk.services.players;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Jajuk web radio player implementation based on Mplayer.
 */
public class WebRadioPlayerImpl extends AbstractMPlayerImpl {
  /**
   * Reader : read information from mplayer like position.
   */
  private class ReaderThread extends Thread {
    /**
     * Implemented to set a useful thread name.
     */
    public ReaderThread() {
      super("WebRadio Reader Thread");
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        for (;;) {
          line = in.readLine();
          if (line == null) {
            break;
          }
          if (line.startsWith(("ICY Info:"))) {
            //Send an event that web radio info has been updated
            Properties pDetails = new Properties();
            String radioTrackDetail = "";
            // Some stations doesn't include the StreamUrl in the ICY line. Sample :
            //ICY Info: StreamTitle='-- Now On Air: URB Non-Stop :: Playing: xxx Cilmi - Sweet About Me :: Email the station xxx@yyy.uk --';
            if (line.contains("';StreamUrl")) {
              radioTrackDetail = line.substring(line.indexOf("StreamTitle='") + 13,
                  line.indexOf("';StreamUrl"));
              // Otherwise, the line should ends with ','
            } else if (line.endsWith("';")) {
              radioTrackDetail = line.substring(line.indexOf("StreamTitle='") + 13,
                  line.length() - 2);
            } else {
              // Just in case, we also handle the case where the line doesn't ends with ';
              radioTrackDetail = line.substring(line.indexOf("StreamTitle='") + 13,
                  line.length() - 2);
            }
            String currentRadioTrack = QueueModel.getCurrentRadio().getName();
            if (StringUtils.isNotEmpty(radioTrackDetail)) {
              currentRadioTrack += ":: " + radioTrackDetail;
            }
            pDetails.put(Const.DETAIL_CONTENT, QueueModel.getCurrentRadio());
            pDetails.put(Const.CURRENT_RADIO_TRACK, currentRadioTrack);
            ObservationManager.notify(new JajukEvent(JajukEvents.WEBRADIO_INFO_UPDATED, pDetails));
          }
          bOpening = false;
          // Search for Exiting (...) pattern
          if (line.matches(".*\\x2e\\x2e\\x2e.*\\(.*\\).*")) {
            bEOF = true;
          }
        }
        // can reach this point at the end of file
        in.close();
        bEOF = true;
        return;
      } catch (Exception e) {
        Log.error(e);
      }
    }
  }

  /**
   * (non-Javadoc).
   * 
   * @param radio 
   * @param fVolume 
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JajukException the jajuk exception
   * 
   * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long,
   * float)
   */
  @Override
  public void play(WebRadio radio, float fVolume) throws IOException, JajukException {
    this.fVolume = fVolume;
    this.bOpening = true;
    this.bEOF = false;
    // Start
    ProcessBuilder pb = new ProcessBuilder(buildCommand(radio.getUrl()));
    Log.debug("Using this Mplayer command: {{" + pb.command() + "}}");
    // Set all environment variables format: var1=xxx var2=yyy
    try {
      Map<String, String> env = pb.environment();
      StringTokenizer st = new StringTokenizer(Conf.getString(Const.CONF_ENV_VARIABLES), " ");
      while (st.hasMoreTokens()) {
        StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
        env.put(st2.nextToken(), st2.nextToken());
      }
      // If needed, set proxy settings in format:
      // http_proxy=http://username:password@proxy.example.org:8080
      if (Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
        String sLogin = Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN).trim();
        String sHost = Conf.getString(Const.CONF_NETWORK_PROXY_HOSTNAME).trim();
        int port = Conf.getInt(Const.CONF_NETWORK_PROXY_PORT);
        // Non anonymous proxy
        if (!StringUtils.isBlank(sLogin)) {
          String sPwd = UtilString.rot13(Conf.getString(Const.CONF_NETWORK_PROXY_PWD));
          String sProxyConf = "http://" + sLogin + ':' + sPwd + '@' + sHost + ':' + port;
          env.put("http_proxy", sProxyConf);
          Log.debug("Using these proxy settings: " + sProxyConf);
        }
        // Anonymous proxy
        else {
          String sProxyConf = "http://" + sHost + ':' + port;
          env.put("http_proxy", sProxyConf);
          Log.debug("Using these proxy settings: " + sProxyConf);
        }
      }
    } catch (Exception e) {
      Log.error(e);
    }
    // Start mplayer
    proc = pb.start();
    // start mplayer replies reader thread
    new ReaderThread().start();
    // if opening, wait, 30 secs max
    int i = 0;
    while (bOpening && i < 30) {
      try {
        Thread.sleep(1000);
        i++;
      } catch (InterruptedException e) {
        Log.error(e);
      }
    }
    // If end of file already reached, it means that file cannot be read
    if (bEOF) {
      throw new JajukException(7);
    }
    // Get track length
    sendCommand("get_time_length");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.players.AbstractMPlayerImpl#play(org.jajuk.base.File, float,
   *      long, float)
   */
  @Override
  public void play(File file, float position, long length, float volume) {
    // nothing to do here...
  }

  /* (non-Javadoc)
  * @see org.jajuk.services.players.IPlayerImpl#getActuallyPlayedTimeMillis()
  */
  @Override
  public long getActuallyPlayedTimeMillis() {
    // makes no sense for webradios
    return 0;
  }
}
