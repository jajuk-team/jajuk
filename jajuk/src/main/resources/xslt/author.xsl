<?xml version='1.0' encoding='UTF-8'?>
<!-- 
	Description: This XSLT transforms a xml file containing the tagging of an author into an html file.
	Author: The Jajuk Team
	Created: August 23, 2006
-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'>
	<xsl:template match='/'>
		<html>
			<head>
				<title><xsl:value-of select='collection/ReportAction.1' /></title>
				<style type='text/css'>
					.style { background-color: #f9f7ed; font-weight:
					bold; padding: 3px; }

					.author { background-color: #c3d9ff; font-weight:
					bold; padding: 3px; }

					.album { background-color: #cdeb8b; font-weight:
					bold; padding: 3px; }

					.track { font-weight: bold; background-color:
					#eeeeee; padding: 3px; width: 120px; }
				</style>
			</head>
			<body>
				<h1><xsl:value-of select='/collection/i18n/ReportAction.1' /></h1>
				<p>
					<CAPTION>
						<xsl:value-of select='/collection/i18n/ReportAction.2' />
					</CAPTION>
				</p>
				<p>
					<ul>
						<li><a href='#1'><xsl:value-of select='/collection/i18n/ReportAction.5'/></a></li>
						<li><a href='#2'><xsl:value-of select='/collection/ReportAction.6'/></a></li>
					</ul>
				</p>
				<p>
					<h2 id='1'><xsl:value-of select='/collection/i18n/ReportAction.5'/></h2>
					<xsl:apply-templates select='collection' />
				</p>
				<p>
					<h2 id='2'><xsl:value-of select='collection/i18n/ReportAction.6'/></h2>
					<xsl:apply-templates select='collection/author' />
				</p>
			</body>
		</html>
	</xsl:template>

	<xsl:template match='collection'>
		<table border='0' cellspacing='5'>
			<xsl:for-each select='author'>
				<tr>
					<xsl:variable name='id' select='id' />
					<td width='100%'>
						<a href='#{id}'>
							<xsl:value-of select='name' />
						</a>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>

	<xsl:template match='collection/author'>
		<hr />

		<xsl:variable name='id' select='id' />
		<p id='{id}'>
			<b>
				<xsl:value-of select='name' />
			</b>
		</p>
		<table border='0' cellspacing='5'>
			<TR>
				<TH><xsl:value-of select='/collection/i18n/ReportAction.album'/></TH>
				<TH><xsl:value-of select='/collection/i18n/ReportAction.year'/></TH>
				<TH><xsl:value-of select='/collection/i18n/ReportAction.style'/></TH>
			</TR>
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