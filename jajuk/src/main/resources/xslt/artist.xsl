<?xml version='1.0' encoding='UTF-8'?>
<!-- 
	Description: This XSLT transforms a xml file containing the tagging of an author into an html file.
	Author: The Jajuk Team
	Created: August 23, 2006
-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'>
	<xsl:template match='/'>
		<xsl:output method='xml' doctype-system='http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd' doctype-public='-//W3C//DTD XHTML 1.0 Strict//EN'/> 
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<meta http-equiv="Content-Type"
					content="text/html; charset=utf-8" />
				<title>Jajuk Music Report</title>
				<link rel="stylesheet" href="report-all.css" type="text/css" media="all"/>
				<link rel="stylesheet" href="report-print.css" type="text/css" media="print"/>
			</head>
			<body>
				<h1>
					<xsl:value-of
						select='/collection/i18n/ReportAction.1' />
				</h1>
				<p class="notice">
					<xsl:value-of
						select='/collection/i18n/ReportAction.2' />
				</p>
				<ul class="jumpto">
					<li class='.jumpto li'><xsl:value-of
								select='/collection/i18n/ReportAction.19' /></li>
					<li><a href="#a1"><xsl:value-of
								select='/collection/i18n/ReportAction.5' /></a></li>
					<li><a href="#a2"><xsl:value-of
								select='/collection/i18n/ReportAction.6' /></a></li>
    			</ul>
				<h2 id='a1'>
						<xsl:value-of
							select='/collection/i18n/ReportAction.5' />
				</h2>
				<xsl:apply-templates select='collection' />
				<h2 id='a2'>
					<xsl:value-of
							select='collection/i18n/ReportAction.6' />
				</h2>
				<xsl:apply-templates select='collection/author' />
			</body>
		</html>
	</xsl:template>

	<xsl:template match='collection'>
		<table border='0' cellspacing='5'>
			<xsl:for-each select='author'>
				<tr>
					<xsl:variable name='id' select='id' />
					<td width='100%'>
						<a href='#a{id}'>
							<xsl:value-of select='name' />
						</a>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template match='collection/author'>
		<xsl:variable name='id' select='id' />
		<h3 id='a{id}'>
				<xsl:value-of select='name' />
			</h3>
		<table border='0' cellspacing='5'>
			<tr>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.album' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.year' />
				</th>
				<th>
					<xsl:value-of
						select='/collection/i18n/ReportAction.style' />
				</th>
			</tr>
			<xsl:for-each select='album'>
				<tr>
					<td width='30%'>
						<xsl:value-of select='name' />
					</td>
					<td width='5%'>
						<xsl:value-of select='year' />
					</td>
					<td>
						<xsl:value-of select='style' />
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>