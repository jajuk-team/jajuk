#!/bin/sh 
#Dependency: apt-get install wget
## Configuration ##

#direcory of the config files
CONF="/data/jajuk-manual/"

SVN_DIR="svn-2be-committed"

#directory where the SVN to Be committed is:
BUILD_DIR="/data/jajuk-manual/tmp"
SVN_2BC="$BUILD_DIR/$SVN_DIR"
SVN_TMP="$BUILD_DIR/$SVN_DIR-tmp"

#directory of the default HTML directory
DEFAULT_HTML="$SVN_2BC/jajuk-hs/default/html/"
DEFAULT_HTML_FRENCH="$SVN_2BC/jajuk-hs/fr/html/"

#directory of the images for HTML pages
IMAGES_DIR="$SVN_2BC/images/"

#file containing the list of file to be wiki2html
FILE_LIST="$BUILD_DIR/jajuk-manual-wiki-files-list.txt"
FILE_LIST_FRENCH="$BUILD_DIR/jajuk-manual-wiki-files-list-french.txt"

#
#URL="http://wiki.jajuk.info/index.php/Special:Wiki2XML?doit=1&whatsthis=wikitext/articlelist&site=wiki.jajuk.info&output_format=docbook_html&text="
#document_title=
URL="http://wiki.jajuk.info/index.php/Special:Wiki2XML?doit=1&whatsthis=articlelist&site=wiki.jajuk.info/&output_format=xhtml&keep_categories=0&add_gfdl=0&keep_interlanguage=0&use_templates=none&text="


## End of Configuration ##
#Do some cleaning first?
#/bin/rm -rf $SVN_2BC
svn_co () {
    echo "=> svn co: (Be patient, this can take a few seconds...)"
    if [ ! -d $DEFAULT_HTML ]; then
	echo " creating: $DEFAULT_HTML"
    	mkdir -p $SVN_2BC
    fi
    if (! svn co https://jajuk.svn.sourceforge.net/svnroot/jajuk/trunk/jajuk/src/doc/ $SVN_2BC) ; then
	echo " error getting the svn, exiting."
    	exit 1
    fi
    #copy the SVN
    rm -rf $SVN_TMP
    cp -a $SVN_2BC $SVN_TMP
    cd $SVN_2BC
    if [ ! -d $DEFAULT_HTML ]; then
	echo " error in SVN: no $DEFAULT_HTML ,exiting."
    	exit 1
    fi
    if [ ! -d $IMAGES_DIR ]; then
	echo " error in SVN: no $IMAGES_DIR ,exiting."
    	exit 1
    fi
    #mkdir -p $IMAGES_DIR
    #mkdir -p $DEFAULT_HTML
}

get_page_name () {    
    echo "=> get_page_name: "
    cat $SVN_2BC/jajuk-hs/default/Map.jhm | grep mapID | cut -d'"' -f 2-2 > $FILE_LIST
    cat $SVN_2BC/jajuk-hs/fr/Map.jhm | grep mapID | cut -d'"' -f 2-2 > $FILE_LIST_FRENCH
}

#read file with the list of the Jajuk manual pages 
get_wiki_pages_in_html () {
    cd $DEFAULT_HTML
    echo "=> get_wiki_pages_in_html: "
    for i in $(cat $FILE_LIST | tr A-Z a-z | sed 's/ /_/g') ; do 
        echo " wget -q \"$URL$i\" -O $i.html" 
        if (! wget -q "$URL$i" -O $i.html ) ; then
	    echo " wget error getting $i : $URL$i (exiting)."
	    exit 1
	fi
    done    
}

format_html_source () {
    echo "=> format_html_source: "
    cd $DEFAULT_HTML
    for i in *.html ; do
        sed -i 's/>/>\n/g' $i
    done

}    

change_html_header () {
    echo "=> change_html_header: "
    cd $DEFAULT_HTML
    for i in *.html ; do
	#fisrt we make sure to remove the XML first line
	if (head -n 1 $i | grep -i xml >/dev/null) ; then
	    sed -i '1d' $i
        fi
    done
}

download_images () {    
    echo "=> download_images: "
    cd $DEFAULT_HTML
    #grep "wiki.jajuk.info/\/images/" * | cut -d\" -f 2-2 | sed 's/wiki.jajuk.info\/images/wiki.jajuk.info\/upload/g'> /tmp/images_list-stuff.txt
    grep href * | grep http://wiki.jajuk.info//images/ | cut -d\" -f 2-2 | sed 's/wiki.jajuk.info\/\/images/wiki.jajuk.info\/upload/g' > /tmp/images_list-stuff.txt
    if (! wget -q -i /tmp/images_list-stuff.txt --no-clobber --force-directories ) ; then
	echo " wget error getting images (exiting)."
	exit 1
    fi
    cp -va wiki.jajuk.info/upload/* $IMAGES_DIR
    rm -rf wiki.jajuk.info
}

correct_image_links () {
    echo "=> correct_image_links: "
    cd $DEFAULT_HTML
    for i in *.html ; do 
        sed -i 's/http:\/\/wiki.jajuk.info\/\/images\//..\/images\//g' $i
    done
}


# if we add more language in the wiki, it will be easy to adapt the following for other language
get_wiki_pages_in_html_french () {
    cd $DEFAULT_HTML_FRENCH
    echo "=> get_wiki_pages_in_html_french: "
    for i in $(cat $FILE_LIST_FRENCH | tr A-Z a-z | sed 's/ /_/g') ; do 
        echo " wget -q \"$URL$i\" -O $i.html" 
        if (! wget -q "$URL$i" -O $i.html ) ; then
	    echo " wget error getting $i : $URL$i (exiting)."
	    exit 1
	fi
    done    
}

format_html_source_french () {
    echo "=> format_html_source_french: "
    cd $DEFAULT_HTML_FRENCH
    for i in *.html ; do
        sed -i 's/>/>\n/g' $i
    done

}    

change_html_header_french () {
    echo "=> change_html_header: "
    cd $DEFAULT_HTML_FRENCH
    for i in *.html ; do
	#fisrt we make sure to remove the XML first line
	if (head -n 1 $i | grep -i xml >/dev/null ) ; then
	    sed -i '1d' $i
        fi
    done
}


correct_image_links_french () {
    echo "=> correct_image_links_french: "
    cd $DEFAULT_HTML_FRENCH
    for i in *.html ; do 
        sed -i 's/http:\/\/wiki.jajuk.info\/\/images\//..\/images\//g' $i
    done
}


check_for_new_files () {
    cd $SVN_2BC
    find . | sort > $BUILD_DIR/file-list-svn-2bc.txt

    cd $SVN_TMP
    find . | sort > $BUILD_DIR/file-list-svn-tmp.txt
    cd ..
   
    diff -u $BUILD_DIR/file-list-svn-tmp.txt $BUILD_DIR/file-list-svn-2bc.txt > $BUILD_DIR/file-list-svn.diff
    if [ "$(find svn-2be-committed-tmp/images/ | wc -l)" -ne "$(find svn-2be-committed/images/ | wc -l )" ] ; then
        echo "   It looks like there are new images!!!"
    fi

}

## MAIN ##
svn_co
get_page_name 
## default manual
get_wiki_pages_in_html
format_html_source
change_html_header
download_images
correct_image_links
## french manual
get_wiki_pages_in_html_french
format_html_source_french
change_html_header_french
correct_image_links_french

#backup this script in the SVN
cp $CONF/get_wiki2html.sh $SVN_2BC

#do some checks

cd $CONF
echo " "
echo "The following must be made by hand:"
echo "=> Any changes made? compare with previous SVN: $BUILD_DIR/complete-svn.diff" 
diff -u -r $SVN_TMP $SVN_2BC > $BUILD_DIR/complete-svn.diff
echo "=> Do we have new images, check in $BUILD_DIR/file-list-svn.diff "
check_for_new_files
echo "     To add new images, use:"
echo "       svn add $SVN_2BC/<image>"
echo "=> Did everything worked well? then you may want to commit:"
echo "    svn --username fsck222 commit $SVN_2BC -m \"manual update from wiki2html\" "
echo "=> You can safely clean (if you want): rm -rf $BUILD_DIR "
echo " "

#bug  svn propdel 'svn:eol-style' -R svn-2be-committed/jajuk-hs/default/html/
#--username --dry-run
#echo "    cd $SVN_DIR /images ; for i in * ; do svn add \$i ; done"

## END ##
