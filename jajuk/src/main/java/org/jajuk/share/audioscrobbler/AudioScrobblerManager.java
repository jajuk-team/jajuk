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

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Player;
import org.jajuk.base.Track;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *  Type description
 */
public class AudioScrobblerManager extends DefaultHandler implements ITechnicalStrings, Observer{

    /** Self instance */
    private static AudioScrobblerManager singleton;

    private static final String APP = "jaj";

    private static final String APP_VERSION = "0.1";    

    private static boolean HANDSHAKE = false;

    private static final String user = ConfigurationManager.getProperty(CONF_OPTIONS_AUDIOSCROBBLER_USER);

    private static final String password = ConfigurationManager.getProperty(CONF_OPTIONS_AUDIOSCROBBLER_PASSWORD);

    private static Scrobbler scrobbler;

    private static List<Submission> lTracks = new ArrayList<Submission>();

    /**
     * @return singleton
     */
    public static AudioScrobblerManager getInstance() {
        if (singleton == null) {
            singleton = new AudioScrobblerManager();
        }
        return singleton;
    }

    public Set<EventSubject> getRegistrationKeys() {
        HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
        eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
        return eventSubjectSet;
    }   
    
    /** Auto commit thread */
    private static Thread tAutoCommit = new Thread() {
        public void run() {
            while (!Main.isExiting()) {
                try {
                    Thread.sleep(AUTO_AUDIOSCROBBLER_COMMIT_DELAY);
                    if (!lTracks.isEmpty()){
                        Log.debug("Auto commit scrobbler"); 
                        org.jajuk.share.audioscrobbler.AudioScrobblerManager.commit(FILE_AUDIOSCROBBLER);
                    }
                } catch (Exception e) {
                    Log.error(e);
                }
            }
        }
    };

    private AudioScrobblerManager (){
        ObservationManager.register(this);
        scrobbler = new Scrobbler(user,password);
        scrobbler.setClientInfo(APP, APP_VERSION);        
    }


    public void startup(){
        File fAudioScrobbler = Util.getConfFileByPath(FILE_AUDIOSCROBBLER);
        if (fAudioScrobbler.exists()){
            try {
                AudioScrobblerManager.load(Util.getConfFileByPath(FILE_AUDIOSCROBBLER));
            } catch (Exception e){
                Log.error(e);
            }
        }
        tAutoCommit.start();
        new Thread() {
            public void run() {
                while (!HANDSHAKE) {
                    HANDSHAKE = handshake(user,password);
                    if (!HANDSHAKE){
                        try {
                            Thread.sleep(1800000);
                        } catch (Exception e) {
                            Log.error(e);
                        }
                    }
                }
            }
        }.start();
    }
    
    public boolean handshake (String sUser, String sPassword){
        scrobbler = new Scrobbler(sUser,sPassword);
        try {
            AudioScrobblerManager.HANDSHAKE = scrobbler.handshake();
            /** Try to submit track backup */
            if (HANDSHAKE && !lTracks.isEmpty()){
                boolean bSub = scrobbler.submit(lTracks);
                if (bSub){
                    lTracks.clear();
                    File fAS = Util.getConfFileByPath(FILE_AUDIOSCROBBLER);
                    if (fAS.exists()){
                        fAS.delete();
                    }
                }
            } 
        } catch (Exception ie){
            Log.error(ie);          
        }
        return HANDSHAKE;
    }
    
   

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event) {
        EventSubject subject = event.getSubject();
        if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
            try {
                org.jajuk.base.File fCurrent = FIFO.getInstance().getCurrentFile();
                Track tCurrent = fCurrent.getTrack();
                if (tCurrent.getLength() > 30){
                    while (Player.isPlaying() && !Player.isSeeking()){
                        float fPos = JajukTimer.getInstance().getCurrentTrackPosition();
                        float fElapsed = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
                        if (fPos >= 0.5 || fElapsed >= 120){
                            Log.debug("[AudioScrobbler] try to scrobble : "+tCurrent.getName());
                            Submission sub = new Submission(
                                    tCurrent.getAuthor().getName2(),
                                    tCurrent.getName(),
                                    tCurrent.getAlbum().getName2(),
                                    tCurrent.getLength()+"",
                                    0);
                            lTracks.add(sub);
                            boolean success = false;  
                            if (HANDSHAKE){
                                success = scrobbler.submit(sub);
                            }
                            if (success){
                                lTracks.remove(sub);
                            } else {
                                Log.debug("Store current track for later submission.");
                                Thread.sleep(120000);
                            }
                        } else {
                            Thread.sleep(800);
                        }                    
                    }    
                } else {

                }
            } catch (Exception e){
                Log.error(e);
            }
        } 
    }

    public static void commit(String sFile) throws IOException {
        String sCharset = ConfigurationManager
        .getProperty(CONF_COLLECTION_CHARSET);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(sFile), sCharset), 1000000); //$NON-NLS-1$

        bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        Iterator it = lTracks.iterator();
        while (it.hasNext()) {
            Submission sub = (Submission) it.next();
            bw.write(sub.toXml());            
        }
        bw.flush();
    }

    public static void load(File frt) throws Exception{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(false);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(frt.toURI().toURL().toString(), getInstance());
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) {
        if (qName.equals(XML_SUBMISSION)) {
            Submission sub = new Submission ();
            sub.setArtist(atts.getValue("","artist"));
            sub.setAlbum(atts.getValue("","album"));
            sub.setLength(atts.getValue("", "length"));
            sub.setTrack(atts.getValue("", "track"));
            sub.setWhen(atts.getValue("", "when"));
            lTracks.add(sub);
        }
    }

}
