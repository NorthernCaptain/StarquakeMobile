<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" version="1.0"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:ncs="http://schemas.android.com/apk/res/northern.captain.seabattle.pro">
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Created on:</xd:b> Oct 18, 2011</xd:p>
            <xd:p><xd:b>Author:</xd:b> leo</xd:p>
            <xd:p></xd:p>
        </xd:desc>
    </xd:doc>
        
        <xsl:output method="xml" indent="yes" encoding="UTF-8" />

    <xsl:param name="mulfactor">1.0</xsl:param>
    <xsl:param name="divfactor">1.0</xsl:param>
    
        <!-- Удаляем лишние пробелы -->
        <!--xsl:strip-space elements="*"/-->
        
        <!-- Шаблон для заголовка (root) -->
        <xsl:template match="/*">
            <xsl:call-template name="print-one"></xsl:call-template>
        </xsl:template>
        
         
    <xsl:template name="print-one">
        <xsl:element name="{name()}">
            
            <xsl:call-template name="print-attr"></xsl:call-template>
            
            <xsl:choose>            
                <xsl:when test="count(child::*) &gt; 0">
                    <xsl:for-each select="child::*">
                        <xsl:call-template name="print-one"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="string(number(.))!='NaN'">
                    <xsl:value-of select="round(number(.) * number($mulfactor) div number($divfactor))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>                    
                </xsl:otherwise>
            </xsl:choose>            
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="dpiconv">
        <xsl:value-of select="round(number(.) * number($mulfactor) div number($divfactor))"/>        
    </xsl:template>
    
        <!-- Prints one element and all sub-elements-->
        <xsl:template name="print-attr">
            <xsl:for-each select="attribute::*">
                
                <xsl:attribute name="{name()}">
                    <xsl:choose>
                        <xsl:when test="name() = 'fscale'">
                            <xsl:value-of select="."/>                            
                        </xsl:when>
                        <xsl:when test="name() = 'imflip'">
                            <xsl:value-of select="."/>                            
                        </xsl:when>
                        <xsl:when test="name() = 'name'">
                            <xsl:value-of select="."/>                            
                        </xsl:when>
                        <xsl:when test="name() = 'orx'">
                            <xsl:value-of select="."/>                            
                        </xsl:when>
                        <xsl:when test="name() = 'value'">
                            <xsl:value-of select="."/>                            
                        </xsl:when>
                        <xsl:when test="string(number(.))!='NaN'">
                            <xsl:value-of select="round(number(.) * number($mulfactor) div number($divfactor))"/>        
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="."/>                            
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:for-each>
        </xsl:template>
        
        
</xsl:stylesheet>
