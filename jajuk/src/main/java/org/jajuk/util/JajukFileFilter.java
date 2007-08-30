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
 *  $$Revision$$
 */

package org.jajuk.util;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Advanced file filter
 * 
 * @see https://trac.jajuk.info/wiki/JajukDevGuide#Filesfilters for direction to
 *      use
 *      <p>
 *      Exemple: new
 *      JajukFilter(false,JajukFileFilter.DirectoryFilter.getInstance(),
 *      JajukFileFilter.AudioFilter.getInstance());
 *      </p>
 */

public class JajukFileFilter extends FileFilter implements java.io.FileFilter, ITechnicalStrings {

	/** Display files flag* */
	private boolean bFiles = true;

	/** Filters */
	private JajukFileFilter[] filters;

	/** And or OR applied to multi filters ? */
	private boolean bAND = true;

	/** Show directories (useful to allow user to navigate) */
	private boolean bShowDirectories = false;

	/**
	 * 
	 * Directory filter
	 * <p>
	 * Singleton
	 * </p>
	 */
	public static class DirectoryFilter extends JajukFileFilter {

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return Messages.getString("Item_Directory");
		}
	}

	/**
	 * 
	 * Any File filter
	 * <p>
	 * Singleton
	 * </p>
	 */
	public static class AnyFileFilter extends JajukFileFilter {
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "*.*";
		}
	}

	/**
	 * 
	 * Known type filter
	 */
	public static class KnownTypeFilter extends JajukFileFilter {
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
			return TypeManager.getInstance().isExtensionSupported(Util.getExtension(f));
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return TypeManager.getInstance().getTypeListString();
		}

	}

	/**
	 * 
	 * Audio filter
	 */
	public static class AudioFilter extends JajukFileFilter {
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
			if (TypeManager.getInstance().isExtensionSupported(Util.getExtension(f))) {
				// check it is an audio file
				return (Boolean) TypeManager.getInstance().getTypeByExtension(Util.getExtension(f))
						.getValue(XML_TYPE_IS_MUSIC);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			String sOut = "";
			for (Type type : TypeManager.getInstance().getAllMusicTypes()) {
				sOut += type.getExtension() + ',';
			}
			// Remove last coma
			sOut = sOut.substring(0, sOut.length() - 1);
			return sOut;
		}
	}

	/**
	 * 
	 * Image filter
	 */
	public static class ImageFilter extends JajukFileFilter {
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
			if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif")
					|| ext.equalsIgnoreCase("png")) {
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "gif,png,jpg,jpeg";
		}
	}

	/**
	 * 
	 * Not Audio file filter (must be a file)
	 */
	public static class NotAudioFilter extends JajukFileFilter {
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
			if (TypeManager.getInstance().isExtensionSupported(Util.getExtension(f))) {
				// check it is an audio file
				return !(Boolean) TypeManager.getInstance()
						.getTypeByExtension(Util.getExtension(f)).getValue(XML_TYPE_IS_MUSIC);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			// No need to translate, is is used internal only
			return "Not audio";
		}
	}

	/**
	 * 
	 * Playlist filter
	 */
	public static class PlaylistFilter extends JajukFileFilter {
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
			if (TypeManager.getInstance().isExtensionSupported(Util.getExtension(f))) {
				// check it is a playlist
				Type playlist = TypeManager.getInstance().getTypeByExtension(EXT_PLAYLIST);
				return TypeManager.getInstance().getTypeByExtension(Util.getExtension(f)).equals(
						playlist);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return EXT_PLAYLIST;
		}
	}

	/**
	 * 
	 * HTMLfilter
	 */
	public static class HTMLFilter extends JajukFileFilter {
		/** Self instance */
		private static HTMLFilter self = null;

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
			if ("html".equals(Util.getExtension(f).toLowerCase())) {
				return true;
			}
			return false;
		}

		/** No instanciation */
		private HTMLFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static HTMLFilter getInstance() {
			if (self == null) {
				self = new HTMLFilter();
			}
			return self;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "html";
		}
	}

	/**
	 * 
	 * XMLfilter
	 */
	public static class XMLFilter extends JajukFileFilter {
		/** Self instance */
		private static XMLFilter self = null;

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
			if ("xml".equals(Util.getExtension(f).toLowerCase())) {
				return true;
			}
			return false;
		}

		/** No instanciation */
		private XMLFilter() {
		}

		/**
		 * 
		 * @return singleton
		 */
		public static XMLFilter getInstance() {
			if (self == null) {
				self = new XMLFilter();
			}
			return self;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "xml";
		}
	}

	/**
	 * 
	 * Report filter (.html or XML file)
	 */
	public static class ReportFilter extends JajukFileFilter {
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
					|| "xml".equals(Util.getExtension(f).toLowerCase())) {
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "html,xml";
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
	public JajukFileFilter(JajukFileFilter... filters) {
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
	public JajukFileFilter(boolean bAND, JajukFileFilter... filters) {
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
		// Force directories acceptation if user wants to navigate into
		// directories
		if (bShowDirectories && f.isDirectory()) {
			return true;
		} else {
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
	}

	/**
	 * Force the filter to accept directories
	 * @param b
	 */
	public void setAcceptDirectories(boolean b) {
		this.bShowDirectories = b;
	}

	public String getDescription() {
		String sOut = ""; //$NON-NLS-1$
		// if only dirs, no description
		if (!bFiles) {
			return sOut;
		}
		// Add description of each filter separated by a coma
		for (int i = 0; i < filters.length; i++) {
			sOut += filters[i].getDescription() + ',';
		}
		// Remove last coma
		sOut = sOut.substring(0, sOut.length() - 1);
		return sOut;
	}
}
