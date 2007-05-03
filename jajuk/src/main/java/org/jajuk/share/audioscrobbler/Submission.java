/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  A submission
 */
public class Submission
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
}
