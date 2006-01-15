/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
package org.jajuk.base;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.Format;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Items root container
 * <p>
 * Singletton
 * 
 * @author Bertrand Florat 
 * @created 16 oct. 2003
 */
public class Collection extends DefaultHandler implements ITechnicalStrings, ErrorHandler,Serializable {
    /** Self instance */
    private static Collection collection;
    private static long lTime;
    /**Current Item manager*/
    private ItemManager manager;
    /**upgrade for track IDs*/
    private HashMap hmWrongRightID = new HashMap();
    /**Garbager activity flag*/
    private static volatile boolean bGarbaging = false;
    /**Auto commit thread*/
    private static Thread tAutoCommit = new Thread(){
        public void run(){
            while (!Main.isExiting()){
                try {
                    Thread.sleep(AUTO_COMMIT_DELAY);
                    Log.debug("Auto commit");
                    //commit collection at each refresh (can be useful if application is closed brutally with control-C or shutdown and that exit hook have no time to perform commit)
                    org.jajuk.base.Collection.commit(FILE_COLLECTION);
                }
                catch (Exception e) {
                    Log.error(e);
                }
            }
        }
    };
    
    /** Instance getter */
    public static synchronized Collection getInstance() {
        if (collection == null) {
            collection = new Collection();
        }
        return collection;
    }
    
    /** Hidden constructor */
    private Collection() {
    }
    
    /** Write current collection to collection file for persistence between sessions */
    public static void commit(String sFileName) throws IOException {
        long lTime = System.currentTimeMillis();
        String sCharset = ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sFileName), sCharset),1000000); //$NON-NLS-1$
        bw.write("<?xml version='1.0' encoding='"+sCharset+"'?>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        bw.write("<"+XML_COLLECTION+" "+XML_VERSION+"='"+JAJUK_VERSION+"'>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        //types
        bw.write(TypeManager.getInstance().toXML()); //$NON-NLS-1$
        Iterator it = null;
        synchronized(TypeManager.getInstance().getLock()){
            it = TypeManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Type type = (Type) it.next();
                bw.write(type.toXml());
            }
        }
        bw.write("\t</"+TypeManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //devices
        bw.write(DeviceManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(DeviceManager.getInstance().getLock()){
            it = DeviceManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Device device = (Device) it.next();
                bw.write(device.toXml());
            }
        }
        bw.write("\t</"+DeviceManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //styles
        bw.write(StyleManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(StyleManager.getInstance().getLock()){
            it = StyleManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Style style = (Style) it.next();
                bw.write(style.toXml());
            }
        }
        bw.write("\t</"+StyleManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //authors
        bw.write(AuthorManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(AuthorManager.getInstance().getLock()){
            it = AuthorManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Author author = (Author) it.next();
                bw.write(author.toXml());
            }
        }
        bw.write("\t</"+AuthorManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //albums
        bw.write(AlbumManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(AlbumManager.getInstance().getLock()){
            it = AlbumManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Album album = (Album) it.next();
                bw.write(album.toXml());
            }
        }
        bw.write("\t</"+AlbumManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //tracks
        bw.write(TrackManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(TrackManager.getInstance().getLock()){
            it = TrackManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Track track = (Track) it.next();
                if (track.getFiles().size() > 0) { //this way we clean up all orphan tracks
                    bw.write(track.toXml());
                }
            }
        }
        bw.write("\t</"+TrackManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //directories
        bw.write(DirectoryManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(DirectoryManager.getInstance().getLock()){
            it = DirectoryManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Directory directory = (Directory) it.next();
                bw.write(directory.toXml());
            }
        }
        bw.write("\t</"+DirectoryManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //files
        bw.write(FileManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(FileManager.getInstance().getLock()){
            it = FileManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                org.jajuk.base.File file = (org.jajuk.base.File) it.next();
                bw.write(file.toXml());
            }
        }
        bw.write("\t</"+FileManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //playlist files
        bw.write(PlaylistFileManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(PlaylistFileManager.getInstance().getLock()){
            it = PlaylistFileManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                PlaylistFile playlistFile = (PlaylistFile) it.next();
                bw.write(playlistFile.toXml());
            }
        }
        bw.write("\t</"+PlaylistFileManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        //playlist
        bw.write(PlaylistManager.getInstance().toXML()); //$NON-NLS-1$
        synchronized(PlaylistManager.getInstance().getLock()){
            it = PlaylistManager.getInstance().getItems().iterator();
            while (it.hasNext()) {
                Playlist playlist = (Playlist) it.next();
                if (playlist.getPlaylistFiles().size() > 0) { //this way we clean up all orphan playlists
                    bw.write(playlist.toXml());
                }
            }
        }
        bw.write("\t</"+PlaylistManager.getInstance().getIdentifier()+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        bw.write("</"+XML_COLLECTION+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        bw.flush();
        bw.close();
        Log.debug("Collection commited in "+(System.currentTimeMillis()-lTime)+" ms");//$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Parse collection.xml file and put all collection information into memory
     *  
     */
    public static void load(String sFile) throws Exception {
        lTime = System.currentTimeMillis();
        //make sure to clean everything in memory
        cleanup();
        DeviceManager.getInstance().cleanAllDevices();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(false);
        SAXParser saxParser = spf.newSAXParser();
        File frt = new File(sFile);
        if (!frt.exists()){
            throw new JajukException("005"); //$NON-NLS-1$
        }
        saxParser.parse(frt.toURL().toString(),getInstance());
        //start auto commit thread
        tAutoCommit.start();
    }
    
    /**
     * Perform a collection clean up for logical items ( delete orphan data )
     * 
     * @return
     */
    public static synchronized void cleanup() {
        //Tracks cleanup
        TrackManager.getInstance().cleanup();
        //Styles cleanup
        StyleManager.getInstance().cleanup();
        //Authors cleanup
        AuthorManager.getInstance().cleanup();
        //albums cleanup
        AlbumManager.getInstance().cleanup();
        //Playlists cleanup
        PlaylistManager.getInstance().cleanup();
    }
    
    
    /**
     * parsing warning
     * 
     * @param spe
     * @exception SAXException
     */
    public void warning(SAXParseException spe) throws SAXException {
        throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
    
    /**
     * parsing error
     * 
     * @param spe
     * @exception SAXException
     */
    public void error(SAXParseException spe) throws SAXException {
        throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
    
    /**
     * parsing fatal error
     * 
     * @param spe
     * @exception SAXException
     */
    public void fatalError(SAXParseException spe) throws SAXException {
        throw new SAXException(Messages.getErrorMessage("005") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
    
    /**
     * Called at parsing start
     */
    public void startDocument() {
        Log.debug("Starting collection file parsing..."); //$NON-NLS-1$
    }
    
    /**
     * Called at parsing end
     */
    public void endDocument() {
        Log.debug("Collection file parsing done : " + (System.currentTimeMillis() - lTime) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Called when we start an element
     *  
     */
    public void startElement(String sUri, String s, String sQName, Attributes attributes) throws SAXException {
        try{
            if (XML_DEVICES.equals(sQName)){
                manager = DeviceManager.getInstance();
            }
            else if (XML_ALBUMS.equals(sQName)){
                manager = AlbumManager.getInstance();
            }
            else if (XML_AUTHORS.equals(sQName)){
                manager = AuthorManager.getInstance();
            }
            else if (XML_DIRECTORIES.equals(sQName)){
                manager = DirectoryManager.getInstance();
            }
            else if (XML_FILES.equals(sQName)){
                manager = FileManager.getInstance();
            }
            else if (XML_PLAYLISTS.equals(sQName)){
                manager = PlaylistManager.getInstance();
            }
            else if (XML_PLAYLIST_FILES.equals(sQName)){
                manager = PlaylistFileManager.getInstance();
            }
            else if (XML_STYLES.equals(sQName)){
                manager = StyleManager.getInstance();
            }
            else if (XML_TRACKS.equals(sQName)){
                manager = TrackManager.getInstance();
            }
            else if (XML_TYPES.equals(sQName)){
                manager = TypeManager.getInstance();
            }
            else if (XML_PROPERTY.equals(sQName)){ //A property description
                String sPropertyName = attributes.getValue(attributes.getIndex(XML_NAME));
                boolean bCustom = Boolean.parseBoolean(attributes.getValue(attributes.getIndex(XML_CUSTOM)));
                boolean bConstructor = Boolean.parseBoolean(attributes.getValue(attributes.getIndex(XML_CONSTRUCTOR)));
                boolean bShouldBeDisplayed = Boolean.parseBoolean(attributes.getValue(attributes.getIndex(XML_VISIBLE)));
                boolean bEditable = Boolean.parseBoolean(attributes.getValue(attributes.getIndex(XML_EDITABLE)));
                boolean bUnique = Boolean.parseBoolean(attributes.getValue(attributes.getIndex(XML_UNIQUE)));
                Class cType = Class.forName(attributes.getValue(attributes.getIndex(XML_TYPE)));
                String sFormat = attributes.getValue(attributes.getIndex(XML_FORMAT));
                Format format = null;
                if (sFormat != null && !sFormat.trim().equals("")){ //$NON-NLS-1$
                    format = PropertyMetaInformation.getDateFormat(sFormat);
                }
                String sDefaultValue = attributes.getValue(attributes.getIndex(XML_DEFAULT_VALUE));
                Object oDefaultValue = null;
                if (sDefaultValue != null && sDefaultValue.trim().length() > 0){ //$NON-NLS-1$
                    oDefaultValue = Util.parse(sDefaultValue,cType,format);
                }
                PropertyMetaInformation meta = new PropertyMetaInformation(
                    sPropertyName,bCustom,bConstructor,bShouldBeDisplayed,bEditable,bUnique,cType,format,oDefaultValue);
                if (manager.getMetaInformation(sPropertyName) == null){ //standard properties are already loaded
                    manager.registerProperty(meta);    
                }
            }
            else if (XML_DEVICE.equals(sQName)){
                Device device = null;
                String sId = attributes.getValue(attributes.getIndex(XML_ID));
                String sItemName = attributes.getValue(attributes.getIndex(XML_NAME));
                long lType=  Long.parseLong(attributes.getValue(attributes.getIndex(XML_TYPE)));
                String sURL = attributes.getValue(attributes.getIndex(XML_URL));
                device = DeviceManager.getInstance().registerDevice(sId, sItemName,lType,sURL);
                if (device != null){
                    device.populateProperties(attributes);
                }
            }
            else if (XML_STYLE.equals(sQName)){
                String sId = attributes.getValue(attributes.getIndex(XML_ID));
                String sItemName = attributes.getValue(attributes.getIndex(XML_NAME));
                Style style = StyleManager.getInstance().registerStyle(sId,sItemName);
                if (style != null){
                    style.populateProperties(attributes);
                }
            } 
            else if (XML_AUTHOR.equals(sQName)){
                String sId = attributes.getValue(attributes.getIndex(XML_ID));
                String sItemName = attributes.getValue(attributes.getIndex(XML_NAME));
                Author author = AuthorManager.getInstance().registerAuthor(sId,sItemName);
                if (author != null){
                    author.populateProperties(attributes);
                }
            }
            else if (XML_ALBUM.equals(sQName)){
                String sId = attributes.getValue(attributes.getIndex(XML_ID));
                String sItemName = attributes.getValue(attributes.getIndex(XML_NAME));
                Album album = AlbumManager.getInstance().registerAlbum(sId, sItemName);
                if (album != null){
                    album.populateProperties(attributes);	
                }
            }
            else if (XML_TRACK.equals(sQName)){
                String sId = attributes.getValue(attributes.getIndex(XML_ID));
                String sTrackName = attributes.getValue(attributes.getIndex(XML_TRACK_NAME));
                Album album = (Album)AlbumManager.getInstance().getItem(attributes.getValue(attributes.getIndex(XML_TRACK_ALBUM)));
                Style style = (Style)StyleManager.getInstance().getItem(attributes.getValue(attributes.getIndex(XML_TRACK_STYLE)));
                Author author =(Author) AuthorManager.getInstance().getItem(attributes.getValue(attributes.getIndex(XML_TRACK_AUTHOR)));
                long length = Long.parseLong(attributes.getValue(attributes.getIndex(XML_TRACK_LENGTH)));
                Type type = (Type)TypeManager.getInstance().getItem(attributes.getValue(attributes.getIndex(XML_TYPE)));
                //more checkups
                if (album == null || author == null || style == null || type == null){
                    return;
                }
                //Get year: we check number format mainly for the case of upgrade from <1.0
                long lYear = 0;
                try{
                    lYear = Integer.parseInt(attributes.getValue(attributes.getIndex(XML_TRACK_YEAR)));
                }
                catch(Exception e){
                    if (Log.isDebugEnabled()){
                        Log.debug(Messages.getString("Error.137")+ ":" +sTrackName); //wrong format //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                //Idem for order
                long lOrder = 0l;
                try{
                    lOrder = Long.parseLong(attributes.getValue(attributes.getIndex(XML_TRACK_ORDER)));
                }
                catch(Exception e){
                    if (Log.isDebugEnabled()){
                        Log.debug(Messages.getString("Error.137")+ ":" +sTrackName); //wrong format //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                //UPGRADE --For jajuk == 1.0.1 to 1.0.2 : Track id changed and used deep hashcode, not used after
                String sRightID = TrackManager.getHashcode(sTrackName, album, style, author, length, lYear,lOrder, type);
                //Date format should be OK
                Date dAdditionDate = Util.getAdditionDateFormat().parse(attributes.getValue(attributes.getIndex(XML_TRACK_ADDED)));
                Track track = TrackManager.getInstance().registerTrack(sRightID, sTrackName, album, style, author, length, lYear,lOrder, type);
                track.setRate(Long.parseLong(attributes.getValue(attributes.getIndex(XML_TRACK_RATE))));
                track.setHits(Long.parseLong(attributes.getValue(attributes.getIndex(XML_TRACK_HITS))));
                track.setAdditionDate(dAdditionDate);
                String sComment = attributes.getValue(attributes.getIndex(XML_TRACK_COMMENT));
                if (sComment == null){
                    sComment = ""; //$NON-NLS-1$
                }
                track.setComment(sComment);
                track.populateProperties(attributes);
                //display a message if Id had a problem
                if (!sId.equals(TrackManager.getHashcode(sTrackName, album, style, author, length, lYear,lOrder,type))){
                    Log.debug("** Wrong Track Id, upgraded: " +track); //$NON-NLS-1$
                    hmWrongRightID.put(sId,sRightID);
                }
            }
            else if (XML_DIRECTORY.equals(sQName)){
                Directory dParent = null;
                String sParentId = attributes.getValue(attributes.getIndex(XML_DIRECTORY_PARENT));
                if (!"-1".equals(sParentId)) { //$NON-NLS-1$
                    dParent = (Directory)DirectoryManager.getInstance().getItem(sParentId); //Parent directory should be already referenced because of order conservation
                    if (dParent == null){ //check directory is exists
                        return;
                    }				
                }
                String sDevice = attributes.getValue(attributes.getIndex(XML_DEVICE));
                Device device = (Device)DeviceManager.getInstance().getItem(sDevice);
                if (device == null){ //check device exists
                    return;
                }
                String sID = attributes.getValue(attributes.getIndex(XML_ID));
                String sItemName = attributes.getValue(attributes.getIndex(XML_NAME));
                Directory directory = DirectoryManager.getInstance().registerDirectory(sID, sItemName,dParent,device);
                directory.populateProperties(attributes);
            }
            else if (XML_FILE.equals(sQName)){
                String sTrackId = attributes.getValue(attributes.getIndex(XML_TRACK));
                //UPGRADE check if track Id is right
                if (hmWrongRightID.size() > 0){
                    //replace wrong by right ID
                    if (hmWrongRightID.containsKey(sTrackId)){
                        sTrackId = (String)hmWrongRightID.get(sTrackId);
                    }
                }
                Track track = (Track)TrackManager.getInstance().getItem(sTrackId);
                Directory dParent = (Directory)DirectoryManager.getInstance().getItem(attributes.getValue(attributes.getIndex(XML_DIRECTORY)));
                if (dParent == null || track == null){ //more checkups
                    return;
                }
                String sItemName = attributes.getValue(attributes.getIndex(XML_NAME));
                long lSize = Long.parseLong(attributes.getValue(attributes.getIndex(XML_SIZE)));
                //Quality analyze, handle format problems (mainly for upgrades)
                long lQuality = 0;
                try{
                    lQuality = Long.parseLong(attributes.getValue(attributes.getIndex(XML_QUALITY)));
                }
                catch(Exception e){
                    if (Log.isDebugEnabled()){
                        Log.debug(Messages.getString("Error.137")+ ":" +sItemName); //wrong format //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                String sID = attributes.getValue(attributes.getIndex(XML_ID)); 
                org.jajuk.base.File file = FileManager.getInstance().registerFile(sID, sItemName, dParent, track, lSize, lQuality);
                file.populateProperties(attributes);
            }
            else if (XML_PLAYLIST_FILE.equals(sQName)){
                String sDir = attributes.getValue(attributes.getIndex(XML_DIRECTORY));
                Directory dParent = (Directory)DirectoryManager.getInstance().getItem(sDir);
                if (dParent == null){ //check directory is exists
                    return;
                }
                String sID= attributes.getValue(attributes.getIndex(XML_ID));
                String sItemName= attributes.getValue(attributes.getIndex(XML_NAME));
                String sHashcode= attributes.getValue(attributes.getIndex(XML_HASHCODE));
                PlaylistFile plf = PlaylistFileManager.getInstance().registerPlaylistFile(sID, sItemName,sHashcode,dParent);
                if (plf != null){
                    plf.populateProperties(attributes);
                    dParent.addPlaylistFile(plf);
                }
            }
            else if (XML_PLAYLIST.equals(sQName)){
                String sPlaylistFiles = attributes.getValue(attributes.getIndex(XML_PLAYLIST_FILES));
                StringTokenizer st = new StringTokenizer(sPlaylistFiles, ","); //playlist file list with ',' //$NON-NLS-1$
                Playlist playlist = null;
                if (st.hasMoreTokens()) { //if none mapped file, ignore it so it will be removed at next commit
                    do{
                        PlaylistFile plFile = (PlaylistFile)PlaylistFileManager.getInstance().getItem((String) st.nextElement());
                        if (plFile != null){
                            playlist = PlaylistManager.getInstance().registerPlaylist(plFile);
                        }
                    }
                    while (st.hasMoreTokens());
                    if ( playlist != null ){
                        playlist.populateProperties(attributes);
                    }
                }
            }
            else if (XML_TYPE.equals(sQName)){
                String sId = attributes.getValue(attributes.getIndex(XML_ID));
                /* we ignore classes given in collection file and we keep default types registrated at startup in Main class. 
                 * But we want to make possible
                 * the adding of new types from an external source, so we accept types for sequential id >=
                 * number of registrated types
                 */ 
                if ( Integer.parseInt(sId)>=TypeManager.getInstance().getElementCount()){
                    String sTypeName = attributes.getValue(attributes.getIndex(XML_NAME));
                    String sExtension = attributes.getValue(attributes.getIndex(XML_TYPE_EXTENSION));
                    Class cPlayer = null;
                    String sPlayer = attributes.getValue(attributes.getIndex(XML_TYPE_PLAYER_IMPL));
                    if (sPlayer ==null || sPlayer.trim().equals("")){ //$NON-NLS-1$
                        cPlayer = null;
                    }
                    else{
                        cPlayer = Class.forName(sPlayer);
                    }
                    Class cTag = null;
                    String sTag = attributes.getValue(attributes.getIndex(XML_TYPE_TAG_IMPL));
                    if (sTag == null || sTag.trim().equals("")){ //$NON-NLS-1$
                        cTag = null;
                    }
                    else{
                        cTag = Class.forName(sTag);
                    }
                    Type type = TypeManager.getInstance().registerType(sId, sTypeName, sExtension, cPlayer,cTag);
                    if (type != null){
                        type.populateProperties(attributes);
                    }
                }
            }
        }
        catch(Exception re){
            String sAttributes = ""; //$NON-NLS-1$
            for (int i=0;i<attributes.getLength();i++){
                sAttributes += "\n"+attributes.getQName(i)+"="+attributes.getValue(i); //$NON-NLS-1$ //$NON-NLS-2$
            }
            Log.error("005",sAttributes,re); //$NON-NLS-1$
        }
    }
    
        
}
