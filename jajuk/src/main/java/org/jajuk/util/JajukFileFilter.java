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

package org.jajuk.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.filechooser.FileFilter;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;

/**
 * Music oriented file filter ( mp3, ogg.. )
 * 
 * @author Bertrand Florat
 * @created 22 oct. 2003
 */

public class JajukFileFilter extends FileFilter implements java.io.FileFilter,
		ITechnicalStrings {
	/** Display directories flag* */
	private boolean bDirectories = true;

	/** Display files flag* */
	private boolean bFiles = true;

	/** Accepted types* */
	private ArrayList<Type> alTypes;

	/** Filters */
	private java.io.FileFilter[] filters;

	/** And or OR applied to multi filters ? */
	private boolean bAND = true;

	/**
	 * 
	 * Directory filter
	 * <p>
	 * Singleton
	 * </p>
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class DirectoryFilter implements java.io.FileFilter {

		/** Self instance */
		private static DirectoryFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			return f.isDirectory();
		}

		/** No instanciation */
		private DirectoryFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static DirectoryFilter getInstance() {
			if (self == null) {
				self = new DirectoryFilter();
			}
			return self;
		}
	}

	/**
	 * 
	 * Any File filter
	 * <p>
	 * Singleton
	 * </p>
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class AnyFileFilter implements java.io.FileFilter {
		/** Self instance */
		private static AnyFileFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			return !f.isDirectory();
		}

		/** No instanciation */
		private AnyFileFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static AnyFileFilter getInstance() {
			if (self == null) {
				self = new AnyFileFilter();
			}
			return self;
		}
	}

	/**
	 * 
	 * Known type filter
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class KnownTypeFilter implements java.io.FileFilter {
		/** Self instance */
		private static KnownTypeFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}
			return TypeManager.getInstance().isExtensionSupported(
					Util.getExtension(f));
		}

		/** No instanciation */
		private KnownTypeFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static KnownTypeFilter getInstance() {
			if (self == null) {
				self = new KnownTypeFilter();
			}
			return self;
		}
	}

	/**
	 * 
	 * Audio filter
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class AudioFilter implements java.io.FileFilter {
		/** Self instance */
		private static AudioFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}
			// check extension is known
			if (TypeManager.getInstance().isExtensionSupported(
					Util.getExtension(f))) {
				// check it is an audio file
				return (Boolean) TypeManager.getInstance().getTypeByExtension(
						Util.getExtension(f)).getValue(XML_TYPE_IS_MUSIC);
			}
			return false;
		}

		/** No instanciation */
		private AudioFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static AudioFilter getInstance() {
			if (self == null) {
				self = new AudioFilter();
			}
			return self;
		}
	}

	/**
	 * 
	 * Image filter
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class ImageFilter implements java.io.FileFilter {
		/** Self instance */
		private static ImageFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}
			// check extension is known
			String ext = Util.getExtension(f);
			if (ext.equalsIgnoreCase("jpg") || //$NON-NLS-1$
					ext.equalsIgnoreCase("gif") || //$NON-NLS-1$
					ext.equalsIgnoreCase("png")) { //$NON-NLS-1$
				return true;
			}
			return false;
		}

		/** No instanciation */
		private ImageFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static ImageFilter getInstance() {
			if (self == null) {
				self = new ImageFilter();
			}
			return self;
		}
	}

	/**
	 * 
	 * Not Audio file filter (must be a file)
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class NotAudioFilter implements java.io.FileFilter {
		/** Self instance */
		private static NotAudioFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}
			// check extension is known
			if (TypeManager.getInstance().isExtensionSupported(
					Util.getExtension(f))) {
				// check it is an audio file
				return !(Boolean) TypeManager.getInstance().getTypeByExtension(
						Util.getExtension(f)).getValue(XML_TYPE_IS_MUSIC);
			}
			// unknown type : not an audio file
			return true;
		}

		/** No instanciation */
		private NotAudioFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static NotAudioFilter getInstance() {
			if (self == null) {
				self = new NotAudioFilter();
			}
			return self;
		}
	}

	/**
	 * 
	 * Playlist filter
	 * 
	 * @author Bertrand Florat
	 * @created 25 may 2006
	 */
	public static class PlaylistFilter implements java.io.FileFilter {
		/** Self instance */
		private static PlaylistFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}
			// check extension is known
			if (TypeManager.getInstance().isExtensionSupported(
					Util.getExtension(f))) {
				// check it is a playlist
				Type playlist = TypeManager.getInstance().getTypeByExtension(
						EXT_PLAYLIST);
				return TypeManager.getInstance().getTypeByExtension(
						Util.getExtension(f)).equals(playlist);
			}
			return false;
		}

		/** No instanciation */
		private PlaylistFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static PlaylistFilter getInstance() {
			if (self == null) {
				self = new PlaylistFilter();
			}
			return self;
		}
	}
	
	/**
	 * 
	 * Report filter (.html or XML file)
	 * 
	 * @author Bertrand Florat
	 * @created 19 april 2007
	 */
	public static class ReportFilter implements java.io.FileFilter {
		/** Self instance */
		private static ReportFilter self = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}
			// check extension is known
			if ("html".equals(Util.getExtension(f).toLowerCase())
					||"xml".equals(Util.getExtension(f).toLowerCase())){
				return true;
			}
			return false;
		}

		/** No instanciation */
		private ReportFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static ReportFilter getInstance() {
			if (self == null) {
				self = new ReportFilter();
			}
			return self;
		}
	}

	/**
	 * Filter constructor
	 * 
	 * @param filters
	 *            undefined list of jajuk filter to be applied (logical AND
	 *            applied between filters)
	 *            <p>
	 *            Example: only audio files new
	 *            JajukFilter(JajukFileFilter.AudioFilter.getInstance());
	 *            </p>
	 */
	public JajukFileFilter(java.io.FileFilter... filters) {
		this(true, filters);
	}

	/**
	 * Filter constructor
	 * 
	 * @param filters
	 *            undefined list of jajuk filter to be applied (logical AND
	 *            applied between filters)
	 * @param bAND:
	 *            should be applied an AND or an OR between filters ?
	 *            <p>
	 *            Example: audio files or directories: new
	 *            JajukFilter(false,JajukFileFilter.DirectoryFilter.getInstance(),JajukFileFilter.AudioFilter.getInstance());
	 *            </p>
	 */
	public JajukFileFilter(boolean bAND, java.io.FileFilter... filters) {
		this.bAND = bAND;
		this.filters = filters;
	}

	/**
	 * Return file test. Apply all filters with a logical AND
	 * 
	 * @param f
	 *            file to test
	 */
	public boolean accept(File f) {
		if (bAND) {
			boolean bResu = true;
			for (int i = 0; i < filters.length; i++) {
				bResu = bResu & filters[i].accept(f);
			}
			return bResu;
		} else { // OR applied
			boolean bResu = false;
			for (int i = 0; i < filters.length; i++) {
				bResu = bResu | filters[i].accept(f);
			}
			return bResu;
		}

	}

	public String getDescription() {
		String sOut = ""; //$NON-NLS-1$
		if (!bFiles) { // only dirs
			return sOut;
		}
		if (alTypes != null) {
			Iterator it = alTypes.iterator();
			while (it.hasNext()) {
				Type type = (Type) it.next();
				sOut += type.getExtension() + ',';
			}
			sOut = sOut.substring(0, sOut.length() - 1);
		}
		return sOut;
	}
}
