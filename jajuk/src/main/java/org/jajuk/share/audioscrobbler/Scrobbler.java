/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.share.audioscrobbler;

import org.jajuk.util.MD5Processor;
import org.jajuk.util.log.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Scrobbler is a client for <a href="http://www.audioscrobbler.com">Audio
 * Scrobbler</a>. Currently it speaks version 1.1 of the protocol.
 * 
 * This file is licensed under the terms of the LGPL, version 2.1 or later. See
 * http://www.gnu.org/licenses/lgpl.txt for more information.
 * 
 * Currently this class has no support for <a
 * href="http://www.musicbrainz.org/">Musicbrainz</a> IDs.
 * 
 * @author Original author: Stephen Crane (influenced by scrobbler.py) jscrane@gmail.com
 */
public class Scrobbler {
	private static final String url = "http://post.audioscrobbler.com";

	private String client = "jaj";

	private String version = "0.1";

	private String user;

	private String md5pass;

	private String md5;

	private String submit;

	/**
	 * Constructs a new Scrobbler.
	 * 
	 * @param user
	 *            The audioscrobbler username
	 * @param password
	 *            The corresponding password
	 */
	public Scrobbler(String user, String password) {
		this.user = user;
		this.md5pass = digestToString(digest(password));
	}

	/**
	 * Sets the client info for the player. (Each player must have a unique
	 * 3-character ID.)
	 * 
	 * @param client
	 *            The 3-character ID
	 * @param version
	 *            The client version number
	 */
	public void setClientInfo(String client, String version) {
		this.client = client;
		this.version = version;
	}

	private byte[] digest(String s) {
		return MD5Processor.hash(s.getBytes());
	}

	private String digestToString(byte[] digest) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			int b = digest[i] & 0xff;
			if (b < 0x10)
				s.append("0");
			s.append(Integer.toHexString(b));
		}
		Log.debug("s=[" + s + "] len=" + s.length() + " dlen=" + digest.length);
		return s.toString();
	}

	/**
	 * Performs the initial handshake with the server.
	 * 
	 * @throws IOException
	 *             If there is a communications' failure
	 * @return Whether the handshake was successful
	 */
	public boolean handshake() throws IOException {
		String encoded = url + "/?hs=true&p=1.1" + "&c=" + URLEncoder.encode(client, "UTF-8")
				+ "&v=" + URLEncoder.encode(version, "UTF-8") + "&u="
				+ URLEncoder.encode(user, "UTF-8");
		List lines = readFrom(encoded, null);
		String s = (String) lines.get(0);
		if (s.equals("BADUSER"))
			return false;
		if (s.equals("UPTODATE") || s.startsWith("UPDATE")) {
			lines.remove(0);
			return uptodate(lines);
		}
		if (s.startsWith("FAILED"))
			return failed(lines);
		return false;
	}

	/**
	 * Submits a single track to the server.
	 * 
	 * @param submission
	 *            The track to submit
	 * @throws IOException
	 *             If a communications' failure occurs
	 * @return Whether the track was submitted successfully
	 */
	public boolean submit(Submission submission) throws IOException {
		List<Submission> list = new ArrayList<Submission>();
		list.add(submission);
		return submit(list);
	}

	/**
	 * Submits a list of tracks to the server. (This is typically used to
	 * resubmit tracks following a communications' outage.)
	 * 
	 * @param tracks
	 *            The list of tracks (Submissions) to submit
	 * @throws IOException
	 *             If a communications' failure occurs
	 * @return Whether the tracks were submitted successfully
	 */
	public boolean submit(List tracks) throws IOException {
		byte[] digested = digest(md5pass + md5);
		String md5rsp = digestToString(digested);
		Log.debug(md5rsp);
		String post = "u=" + URLEncoder.encode(user, "UTF-8") + "&s=" + md5rsp;
		int count = 0;
		for (Iterator i = tracks.iterator(); i.hasNext();) {
			Submission s = (Submission) i.next();
			post += s.urlEncoded(count);
			count++;
		}
		Log.debug(post);
		List lines = readFrom(submit, post);
		String s = (String) lines.get(0);
		if (s.startsWith("FAILED")) {
			failed(lines);
			return false;
		}
		if (s.equals("OK")) {
			lines.remove(0);
			interval(lines);
			return true;
		}
		if (s.equals("BADAUTH")) {
			Log.warn("authorisation failure submitting [" + post + "] to [" + submit + "]");
			interval(lines);
			return false;
		}
		Log.warn("don't understand response " + s);
		return false;
	}

	private List readFrom(String encoded, String post) throws IOException {
		Log.debug("opening: " + encoded);
		URL u = new URL(encoded);
		URLConnection conn = u.openConnection();

		conn.setDoInput(true);
		if (post != null) {
			conn.setDoOutput(true);
			DataOutputStream output = new DataOutputStream(conn.getOutputStream());
			output.writeBytes(post);
			output.flush();
			output.close();
		}
		InputStream input = conn.getInputStream();
		BufferedReader r = new BufferedReader(new InputStreamReader(input));
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = r.readLine()) != null) {
			lines.add(line);
			Log.debug("read:" + line);
		}
		r.close();
		input.close();
		return lines;
	}

	private boolean uptodate(List lines) {
		this.md5 = (String) lines.remove(0);
		this.submit = (String) lines.remove(0);
		interval(lines);
		return true;
	}

	private boolean failed(List lines) {
		Log.warn("failed:" + (String) lines.remove(0));
		interval(lines);
		return false;
	}

	private void interval(List lines) {
		String interval = (String) lines.remove(0);
		if (interval.startsWith("INTERVAL")) {
			interval = interval.substring(interval.lastIndexOf(' ') + 1);
			int time = Integer.parseInt(interval) * 1000;
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Scrobbler scrobbler = new Scrobbler("user", "pass");
		if (scrobbler.handshake() && args.length > 0)
			scrobbler.submit(new Submission(args[0], args[1], args[2], args[3], 300 * 1000));
	}
}
