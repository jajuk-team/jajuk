<?xml version='1.0' encoding='UTF-8'?>

<!-- 
	Description: This XSLT transforms a xml file containing the tagging of a directory into an html file.
	Author: The Jajuk Team
	Created: August 23, 2006
-->

<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'>

	<xsl:template match='/'>
		<xsl:output method='xml' doctype-system='http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd' doctype-public='-//W3C//DTD XHTML 1.0 Strict//EN'/> 
		<html xmlns='http://www.w3.org/1999/xhtml'>
		<head>
				<meta http-equiv='Content-Type'
					content='text/html; charset=utf-8' />
				<title>Jajuk Music Report</title>
				<link rel='stylesheet' href='report-all.css' type='text/css' media='all'/>
				<link rel='stylesheet' href='report-print.css' type='text/css' media='print'/>
			</head>
			<body>
				<h1>
					<xsl:value-of
						select='/collection/i18n/ReportAction.1' />
				</h1>
			    			<p class='notice'>
						<xsl:value-of
							select='/collection/i18n/ReportAction.2' />
				</p>
				<ul class='jumpto'>
					<li class='.jumpto li'><xsl:value-of
								select='/collection/i18n/ReportAction.19' /></li>
					<li class='.jumpto li'><a href='#a1'><xsl:value-of
								select='/collection/i18n/ReportAction.10' /></a></li>
					<li class='.jumpto li'><a href='#a2'><xsl:value-of
								select='/collection/i18n/ReportAction.11' /></a></li>
				</ul>
				<h2 id='a1'>
						<xsl:value-of
							select='/collection/i18n/ReportAction.10' />
					</h2>
				<xsl:call-template name='directories_list' />
				<h2 id='a2'>
					<xsl:value-of
						select='/collection/i18n/ReportAction.11' />
				</h2>
				<xsl:apply-templates select='*/directory' />
			</body>
		</html>
	</xsl:template>

	<xsl:template name='directories_list'>
		<xsl:for-each select='*/directory'>
			<xsl:variable name='id' select='id' />
				<h3>
					<a href='#a{id}'>
						<xsl:value-of select='path' />
					</a>
				</h3>
			<xsl:call-template name='directories_list' />
		</xsl:for-each>
	</xsl:template>



<xsl:template match='*/directory'>
		<xsl:variable name='id' select='id' />
		<h2 id='a{id}'>
			<xsl:value-of select='path' />
		</h2>
		<xsl:apply-templates select='directory' />
			<table border='0' cellspacing='5'>
			<tr>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.12' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.order' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.13' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.style' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.author' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.album' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.length' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.rate' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.comment' />
				</th>
			</tr>

			<xsl:for-each select='file'>
				<tr>
					<td>
						<xsl:value-of select='name' />
					</td>
					<td>
						<xsl:value-of select='track/order' />
					</td>
					<td class='track'>
						<xsl:value-of select='track/name' />
					</td>
					<td class='style'>
						<xsl:value-of select='track/style' />
					</td>
					<td class='author'>
						<xsl:value-of select='track/author' />
					</td>
					<td class='album'>
						<xsl:value-of select='track/album' />
					</td>
					<td>
						<xsl:value-of select='track/length' />
					</td>
					<td>
						<xsl:value-of select='track/rate' />
					</td>
					<td>
						<xsl:value-of select='track/comment' />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>


</xsl:stylesheet>