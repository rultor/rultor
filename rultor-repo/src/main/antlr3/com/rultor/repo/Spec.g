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
grammar Spec;

@header {
    package com.rultor.repo;
    import com.jcabi.urn.URN;
    import com.rultor.spi.Variable;
    import java.util.Collection;
    import java.util.LinkedList;
    import java.util.Map;
    import java.util.TreeMap;
    import org.apache.commons.lang3.StringEscapeUtils;
}

@lexer::header {
    package com.rultor.repo;
}

@lexer::members {
    @Override
    public void emitErrorMessage(String msg) {
        throw new IllegalArgumentException(msg);
    }
}

@parser::members {
    private transient Grammar grammar;
    private transient URN owner;
    public void setGrammar(final Grammar grm) {
        this.grammar = grm;
    }
    public void setOwner(final URN urn) {
        this.owner = urn;
    }
    @Override
    public void emitErrorMessage(String msg) {
        throw new IllegalArgumentException(msg);
    }
}

spec returns [Variable<?> ret]
    :
    variable
    { $ret = $variable.ret; }
    EOF
    ;

variable returns [Variable<?> ret]
    :
    composite
    { $ret = $composite.ret; }
    |
    array
    { $ret = $array.ret; }
    |
    dictionary
    { $ret = $dictionary.ret; }
    |
    alter
    { $ret = $alter.ret; }
    |
    META
    { $ret = new Meta($META.text); }
    |
    ARG
    { $ret = new Arg($ARG.text); }
    |
    NAME arguments
    { $ret = new RefLocal(this.grammar, this.owner, $NAME.text, $arguments.ret); }
    |
    OWNER ':' NAME arguments
    { $ret = new RefForeign(this.grammar, URN.create($OWNER.text), $NAME.text, $arguments.ret); }
    |
    TEXT
    { $ret = new Text(StringEscapeUtils.unescapeJava($TEXT.text)); }
    |
    BIGTEXT
    { $ret = new BigText($BIGTEXT.text); }
    |
    BOOLEAN
    { $ret = new Constant<Boolean>(new Boolean($BOOLEAN.text.equals("TRUE"))); }
    |
    INTEGER
    { $ret = new Constant<Integer>(Integer.valueOf($INTEGER.text)); }
    |
    LONG
    { $ret = new Constant<Long>(Long.valueOf($LONG.text.substring(0, $LONG.text.length() - 1))); }
    |
    DOUBLE
    { $ret = new Constant<Double>(Double.valueOf($DOUBLE.text)); }
    ;

alter returns [Variable<String> ret]
    :
    '@'
    '('
    TEXT
    ')'
    { $ret = new Alter($TEXT.text); }
    ;

composite returns [Composite ret]
    @init { final Collection<Variable<?>> vars = new LinkedList<Variable<?>>(); }
    :
    TYPE
    arguments
    { $ret = new Composite($TYPE.text, $arguments.ret); }
    ;

arguments returns [Collection<Variable<?>> ret]
    @init { $ret = new LinkedList<Variable<?>>(); }
    :
    '('
    (
        first=variable
        { $ret.add($first.ret); }
        (
            ','
            next=variable
            { $ret.add($next.ret); }
        )*
    )*
    ')'
    ;

array returns [Chain ret]
    @init { final Collection<Variable<?>> vars = new LinkedList<Variable<?>>(); }
    :
    '['
    (
        first=variable
        { vars.add($first.ret); }
        (
            ','
            next=variable
            { vars.add($next.ret); }
        )*
    )*
    ']'
    { $ret = new Chain(vars); }
    ;

dictionary returns [Dictionary ret]
    @init { final Map<Variable<String>, Variable<?>> map = new TreeMap<Variable<String>, Variable<?>>(); }
    :
    '{'
    (
        first_key=key
        ':'
        first_value=variable
        { map.put($first_key.ret, $first_value.ret); }
        (
            ','
            second_key=key
            ':'
            second_value=variable
            { map.put($second_key.ret, $second_value.ret); }
        )*
    )*
    '}'
    { $ret = new Dictionary(map); }
    ;

key returns [Variable<String> ret]
    :
    TEXT
    { $ret = new Text($TEXT.text); }
    |
    alter
    { $ret = $alter.ret; }
    ;

BOOLEAN
    :
    'TRUE' | 'FALSE'
    ;

TYPE
    :
    PACKAGE ('.' PACKAGE)+
    ;

META
    :
    '$' '{' LETTER+ '}'
    ;

ARG
    :
    '$' '{' DIGIT+ (':' ~'}'+)* '}'
    ;

NAME
    :
    LETTER (LETTER | DIGIT | '-')+
    ;

OWNER
    :
    'urn' ':' ('facebook'|'github'|'google') ':' DIGIT+
    ;

TEXT
    :
    '"' ('\\"' | ~'"')* '"'
    { this.setText(this.getText().substring(1, this.getText().length() - 1).replace("\\\"", "\"")); }
    |
    '\'' ('\\\'' | ~'\'')* '\''
    { this.setText(this.getText().substring(1, this.getText().length() - 1).replace("\\'", "'")); }
    ;

BIGTEXT
    :
    '"""'
    .+
    '"""'
    { this.setText(this.getText().substring(3, this.getText().length() - 3).replace("\\\"", "\"")); }
    ;

INTEGER
    :
    ( '-' | '+' )? DIGIT+
    ;

LONG
    :
    ( '-' | '+' )? DIGIT+ ('L' | 'l')
    ;

DOUBLE
    :
    ( '-' | '+' )? DIGIT+ '.' DIGIT+
    ;

fragment LETTER
    :
    ( 'a' .. 'z' | 'A' .. 'Z')
    ;
fragment DIGIT
    :
    ( '0' .. '9' )
    ;
fragment PACKAGE
    :
    LETTER (LETTER | DIGIT | '$')*
    ;

SPACE
    :
    ( ' ' | '\t' | '\n' | '\r' )+
    { skip(); }
    ;
