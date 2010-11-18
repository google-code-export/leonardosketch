<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>

    <!-- identity transform -->
    <xsl:template match="@*|node()">
       <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>


    <!-- turn sketchy into sketchy/page into baz -->

    <xsl:template match="sketchy">
        <sketchy version='0'>
            <info backgroundFill="#ffffffff"></info>
            <page>
                <xsl:apply-templates/>
            </page>
        </sketchy>
    </xsl:template>

</xsl:stylesheet>
