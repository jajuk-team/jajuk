package org.jajuk.base;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.jajuk.util.log.Log;

/**
 * Scrobbler is a client for <a href="http://www.audioscrobbler.com">Audio
 * Scrobbler</a>. Currently it speaks version 1.1 of the protocol.
 *
 * This file is licensed under the terms of the LGPL, version 2.1 or later.
 * See http://www.gnu.org/licenses/lgpl.txt for more information.
 *
 * Currently this class has no support for
 * <a href="http://www.musicbrainz.org/">Musicbrainz</a> IDs.
 *
 * @author Stephen Crane (influenced by scrobbler.py) jscrane@gmail.com
 */
public class Scrobbler
{
    private static final String url = "http://post.audioscrobbler.com";
    private String client = "jaj";
    private String version = "0.1";

    private String user;
    private String md5pass;
    private String md5;
    private String submit;
   
    /**
     * Constructs a new Scrobbler.
     * @param user The audioscrobbler username
     * @param password The corresponding password
     */
    public Scrobbler(String user, String password)
    {
        this.user = user;
        this.md5pass = digestToString(digest(password));
    }

    /**
     * Sets the client info for the player. (Each player must have
     * a unique 3-character ID.)
     * @param client The 3-character ID
     * @param version The client version number
     */
    public void setClientInfo(String client, String version)
    {
        this.client = client;
        this.version = version;
    }
    
    private byte[] digest(String s)
    {
        MD5Digest d = new MD5Digest();
        byte[] p = s.getBytes();
        d.update(p, 0, p.length);
        byte[] pd = new byte[16];
        d.doFinal(pd, 0);
        return pd;
    }
    
    private String digestToString(byte[] digest)
    {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            int b = digest[i] & 0xff;
            if (b < 0x10)
                s.append("0");
            s.append(Integer.toHexString(b));
        }
        Log.debug("s=["+s+"] len="+s.length()+" dlen="+digest.length);
        return s.toString();
    }
   
    /**
     * Performs the initial handshake with the server.
     * @throws IOException If there is a communications' failure
     * @return Whether the handshake was successful
     */
    public boolean handshake()
        throws IOException
    {
        String encoded = url + "/?hs=true&p=1.1" +
            "&c=" + URLEncoder.encode(client, "UTF-8") +
            "&v=" + URLEncoder.encode(version, "UTF-8") +
            "&u=" + URLEncoder.encode(user, "UTF-8");
        List lines = readFrom(encoded, null);
        String s = (String)lines.get(0);
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
     * @param submission The track to submit
     * @throws IOException If a communications' failure occurs
     * @return Whether the track was submitted successfully
     */
    public boolean submit(Submission submission)
        throws IOException
    {
        List <Submission>list = new ArrayList<Submission>();
        list.add(submission);
        return submit(list);
    }
    
    /**
     * Submits a list of tracks to the server. (This is typically used
     * to resubmit tracks following a communications' outage.)
     * @param tracks The list of tracks (Submissions) to submit
     * @throws IOException If a communications' failure occurs
     * @return Whether the tracks were submitted successfully
     */
    public boolean submit(List tracks)
        throws IOException
    {
        byte[] digested = digest(md5pass + md5);
        String md5rsp = digestToString(digested);
        Log.debug(md5rsp);
        String post = "u="+URLEncoder.encode(user, "UTF-8")+"&s="+md5rsp;
        int count = 0;
        for (Iterator i = tracks.iterator(); i.hasNext(); ) {
            Submission s = (Submission)i.next();
            post += s.urlEncoded(count);
            count++;
        }
        Log.debug(post);
        List lines = readFrom(submit, post);
        String s = (String)lines.get(0);
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
            Log.warn("authorisation failure submitting ["+post+"] to ["+submit+"]");
            interval(lines);
            return false;
        }
        Log.warn("don't understand response "+s);
        return false;
    }
   
    private List readFrom(String encoded, String post)
        throws IOException
    {
        Log.debug("opening: "+encoded);
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
        List <String>lines = new ArrayList<String>();
        String line;
        while ((line = r.readLine()) != null) {
            lines.add(line);
            Log.debug("read:"+line);
        }
        r.close();
        input.close();
        return lines;
    }

    private boolean uptodate(List lines)
    {
        this.md5 = (String)lines.remove(0);
        this.submit = (String)lines.remove(0);
        interval(lines);
        return true;
    }

    private boolean failed(List lines)
    {
        Log.warn("failed:"+(String)lines.remove(0));
        interval(lines);
        return false;
    }

    private void interval(List lines)
    {
        String interval = (String)lines.remove(0);
        if (interval.startsWith("INTERVAL")) {
            interval = interval.substring(interval.lastIndexOf(' ')+1);
            int time = Integer.parseInt(interval) * 1000;
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {}
        }
    }
   
    public static void main(String[] args)
        throws IOException
    {
        Scrobbler scrobbler = new Scrobbler("user", "pass");
        if (scrobbler.handshake() && args.length > 0)
            scrobbler.submit(new Submission(args[0], args[1], args[2], args[3], 300 * 1000));
    }
}

class Submission
{
    /**
     * Constructs a new Submission with the relevant details.
     */
    public Submission(String artist, String track, String album,
                      String length, long submitDelay)
    {
        setArtist(artist);
        setTrack(track);
        setAlbum(album);
        setLength(length);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String when = sdf.format(new Date());
        setWhen(when);
    }

    /**
     * Constructs a Submission as a bean, for persistence purposes.
     */
    public Submission()
    {
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public String getArtist()
    {
        return artist;
    }
    
    public void setAlbum(String album)
    {
        this.album = album;
    }

    public String getAlbum()
    {
        return album;
    }

    public void setTrack(String track)
    {
        this.track = track;
    }

    public String getTrack()
    {
        return track;
    }

    public void setLength(String length)
    {
        this.length = length;
    }

    public String getLength()
    {
        return length;
    }

    public void setWhen(String when)
    {
        this.when = when;
    }

    public String getWhen()
    {
        return when;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }
        
    String urlEncoded(int count)
        throws UnsupportedEncodingException
    {
        String i = "["+count+"]";
        return "&a"+i+"="+URLEncoder.encode(artist, "UTF-8")+
            "&t"+i+"="+URLEncoder.encode(track, "UTF-8")+
            "&b"+i+"="+URLEncoder.encode(album, "UTF-8")+
            "&l"+i+"="+URLEncoder.encode(length, "UTF-8")+
            "&i"+i+"="+URLEncoder.encode(when, "UTF-8");
    }

    private String artist, track, album, length, when;
    private int id = -1;
    
    public String toXml (){
        String sXml = "\t<submission ";       
        sXml += " artist='"+getArtist()+"'";
        sXml += " track='"+getTrack()+"'";
        sXml += " album='"+getAlbum()+"'";
        sXml += " length='"+getLength()+"'";
        sXml += " when='"+getWhen()+"'";
        sXml += " />\n";
        return sXml;
    }
};
