/**
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
 */

/*globals $: false, moment: false, markdown: false, document: false */

var RULTOR = {
    format: function ($block) {
        "use strict";
        $block.find('span.timeago').each(
            function (span) {
                var iso = $(this).text();
                if (iso.match(/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?Z/)) {
                    $(this).text(moment(iso).fromNow());
                    $(this).attr('title', iso);
                }
            }
        );
        $block.find('span.markdown').each(
            function (span) {
                $(this).html(
                    markdown.toHTML($(this).text()).replace(/<\/?p *>/g, '')
                );
            }
        );
    }
};

$(document).ready(
    function () {
        "use strict";
        RULTOR.format($('body'));
    }
);

$(document).keyup(
    function (event) {
        "use strict";
        if (event.keyCode === 27) {
            $('.overlay').hide();
            $('.menu').hide();
        }
    }
);
