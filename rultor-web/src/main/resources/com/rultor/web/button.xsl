<?xml version="1.0"?>
<!--
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml" version="2.0"
                exclude-result-prefixes="xs">
    <xsl:template
        match="/page/widgets/widget[@class='com.rultor.widget.BuildHealth']/builds/build[coordinates/rule='%s'][1]">
        <svg xmlns="http://www.w3.org/2000/svg" font-size="10" viewBox="0 0 100 50">
            <defs>
                <style type="text/css"><![CDATA[
                    @font-face {
                        font-family: 'Rultor-Logo';
                        src: url('//img.rultor.com/rultor.eot');
                        src: url('//img.rultor.com/rultor.eot?#iefix') format('embedded-opentype'),
                            url('//img.rultor.com/rultor.woff') format('woff'),
                            url('//img.rultor.com/rultor.ttf') format('truetype'),
                            url('//img.rultor.com/rultor.svg?#rultor') format('svg');
                    }
                ]]></style>
            </defs>
            <rect x="0" y="0"
             width="100" height="50"
             fill="white" stroke="black"
             />
            <g>
                <text font-family="Rultor-Logo" font-size="40" x="82" xml:space="preserve" y="25" text-anchor="middle" dominant-baseline="middle">
                    <xsl:text>R</xsl:text>
                </text>
                <text y="10">
                    <tspan x="0"><xsl:value-of select="code"/></tspan>
                    <tspan x="0" dy="10"><xsl:value-of select="duration"/></tspan>
                    <tspan x="0" dy="10"><xsl:value-of select="health"/></tspan>
                </text>
            </g>
        </svg>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>
