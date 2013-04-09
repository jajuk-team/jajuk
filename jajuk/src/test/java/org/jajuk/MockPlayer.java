/*
 *  Jajuk
 *  Copyright (C) 2003-2012 The Jajuk Team
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
package org.jajuk;

import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.webradio.WebRadio;

public class MockPlayer implements IPlayerImpl {
  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#stop()
   */
  @Override
  public void stop() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#setVolume(float)
   */
  @Override
  public void setVolume(float fVolume) throws Exception {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#seek(float)
   */
  @Override
  public void seek(float fPosition) {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#resume()
   */
  @Override
  public void resume() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#play(org.jajuk.services.webradio.WebRadio, float)
   */
  @Override
  public void play(WebRadio radio, float fVolume) throws Exception {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#play(org.jajuk.base.File, float, long, float)
   */
  @Override
  public void play(org.jajuk.base.File file, float fPosition, long length, float fVolume)
      throws Exception {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#pause()
   */
  @Override
  public void pause() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#getState()
   */
  @Override
  public int getState() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#getElapsedTime()
   */
  @Override
  public long getElapsedTimeMillis() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#getCurrentVolume()
   */
  @Override
  public float getCurrentVolume() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#getCurrentPosition()
   */
  @Override
  public float getCurrentPosition() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#getCurrentLength()
   */
  @Override
  public long getDurationSec() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.players.IPlayerImpl#getActuallyPlayedTimeMillis()
   */
  @Override
  public long getActuallyPlayedTimeMillis() {
    // TODO Auto-generated method stub
    return 0;
  }
}
