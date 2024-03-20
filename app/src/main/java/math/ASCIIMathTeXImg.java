/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Translated and refactored by:  Arnaud Roques
 * 
 *
 */

/*
This is a Java port of https://github.com/asciimath/asciimathml/blob/master/asciimath-based/ASCIIMathTeXImg.js

ASCIIMathTeXImg.js itself is based on ASCIIMathML, Version 1.4.7 Aug 30, 2005, (c) Peter Jipsen http://www.chapman.edu/~jipsen
Modified with TeX conversion for IMG rendering Sept 6, 2006 (c) David Lippman http://www.pierce.ctc.edu/dlippman
  Updated to match ver 2.2 Mar 3, 2014
  Latest at https://github.com/mathjax/asciimathml

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ASCIIMathTeXImg {

	private int AMnestingDepth;
	private Ttype AMpreviousSymbol;
	private Ttype AMcurrentSymbol;

	private String slice(String str, int start, int end) {
		if (end > str.length()) {
			return str.substring(start);
		}
		return str.substring(start, end);
	}

	private String slice(String str, int start) {
		return str.substring(start);
	}

	private String substr(String str, int pos, int len) {
		if (pos + len > str.length()) {
			return str.substring(pos);
		}
		return str.substring(pos, pos + len);
	}

	// Token types
	private static enum Ttype { 
		CONST, UNARY, BINARY, INFIX, LEFTBRACKET, RIGHTBRACKET,
		SPACE, UNDEROVER, DEFINITION, LEFTRIGHT, TEXT;
	}

	static class Tupple {
		private final String input;
		private final String tag;
		private final String output;
		private final String tex;
		private final Ttype ttype;

		private final String[] rewriteleftright;
		private final Collection<String> flags;

		private Tupple(String[] rewriteleftright, String input, String tag, String output, String tex, Ttype ttype,
				String... flags) {
			this.input = input;
			this.tag = tag;
			this.output = output;
			this.tex = tex;
			this.ttype = ttype;
			this.flags = Arrays.asList(flags);
			this.rewriteleftright = rewriteleftright;
		}

		private Tupple(String input, String tag, String output, String tex, Ttype ttype, String... flags) {
			this.input = input;
			this.tag = tag;
			this.output = output;
			this.tex = tex;
			this.ttype = ttype;
			this.flags = Arrays.asList(flags);
			this.rewriteleftright = null;
		}

		public boolean hasFlag(String flagName) {
			return flags.contains(flagName);
		}
	}

	private static final Tupple AMsqrt  = new Tupple("sqrt"    , "msqrt", "sqrt"    , null, Ttype.UNARY );
	private static final Tupple AMroot  = new Tupple("root"    , "mroot", "root"    , null, Ttype.BINARY);
	private static final Tupple AMfrac  = new Tupple("frac"    , "mfrac", "/"       , null, Ttype.BINARY);
	private static final Tupple AMdiv   = new Tupple("/"       , "mfrac", "/"       , null, Ttype.INFIX );
	private static final Tupple AMover  = new Tupple("stackrel", "mover", "stackrel", null, Ttype.BINARY);
	private static final Tupple AMsub   = new Tupple("_"       , "msub" , "_"       , null, Ttype.INFIX );
	private static final Tupple AMsup   = new Tupple("^"       , "msup" , "^"       , null, Ttype.INFIX );
	private static final Tupple AMtext  = new Tupple("text"    , "mtext", "text"    , null, Ttype.TEXT  );
	private static final Tupple AMmbox  = new Tupple("mbox"    , "mtext", "mbox"    , null, Ttype.TEXT  );
	private static final Tupple AMquote = new Tupple("\""      , "mtext", "mbox"    , null, Ttype.TEXT  );

	private static final List<Tupple> AMsymbols = new ArrayList<>(Arrays.asList(new Tupple[] { //
			// some greek symbols
			new Tupple("alpha"     , "mi", "\u03B1", null     , Ttype.CONST     ), //
			new Tupple("beta"      , "mi", "\u03B2", null     , Ttype.CONST     ), //
			new Tupple("chi"       , "mi", "\u03C7", null     , Ttype.CONST     ), //
			new Tupple("delta"     , "mi", "\u03B4", null     , Ttype.CONST     ), //
			new Tupple("Delta"     , "mo", "\u0394", null     , Ttype.CONST     ), //
			new Tupple("epsi"      , "mi", "\u03B5", "epsilon", Ttype.CONST     ), //
			new Tupple("varepsilon", "mi", "\u025B", null     , Ttype.CONST     ), //
			new Tupple("eta"       , "mi", "\u03B7", null     , Ttype.CONST     ), //
			new Tupple("gamma"     , "mi", "\u03B3", null     , Ttype.CONST     ), //
			new Tupple("Gamma"     , "mo", "\u0393", null     , Ttype.CONST     ), //
			new Tupple("iota"      , "mi", "\u03B9", null     , Ttype.CONST     ), //
			new Tupple("kappa"     , "mi", "\u03BA", null     , Ttype.CONST     ), //
			new Tupple("lambda"    , "mi", "\u03BB", null     , Ttype.CONST     ), //
			new Tupple("Lambda"    , "mo", "\u039B", null     , Ttype.CONST     ), //
			new Tupple("lamda"     , "mi", "lambda", null     , Ttype.DEFINITION), //
			new Tupple("Lamda"     , "mi", "Lambda", null     , Ttype.DEFINITION), //
			new Tupple("mu"        , "mi", "\u03BC", null     , Ttype.CONST     ), //
			new Tupple("nu"        , "mi", "\u03BD", null     , Ttype.CONST     ), //
			new Tupple("omega"     , "mi", "\u03C9", null     , Ttype.CONST     ), //
			new Tupple("Omega"     , "mo", "\u03A9", null     , Ttype.CONST     ), //
			new Tupple("phi"       , "mi", "\u03C6", null     , Ttype.CONST     ), //
			new Tupple("varphi"    , "mi", "\u03D5", null     , Ttype.CONST     ), //
			new Tupple("Phi"       , "mo", "\u03A6", null     , Ttype.CONST     ), //
			new Tupple("pi"        , "mi", "\u03C0", null     , Ttype.CONST     ), //
			new Tupple("Pi"        , "mo", "\u03A0", null     , Ttype.CONST     ), //
			new Tupple("psi"       , "mi", "\u03C8", null     , Ttype.CONST     ), //
			new Tupple("Psi"       , "mi", "\u03A8", null     , Ttype.CONST     ), //
			new Tupple("rho"       , "mi", "\u03C1", null     , Ttype.CONST     ), //
			new Tupple("sigma"     , "mi", "\u03C3", null     , Ttype.CONST     ), //
			new Tupple("Sigma"     , "mo", "\u03A3", null     , Ttype.CONST     ), //
			new Tupple("tau"       , "mi", "\u03C4", null     , Ttype.CONST     ), //
			new Tupple("theta"     , "mi", "\u03B8", null     , Ttype.CONST     ), //
			new Tupple("vartheta"  , "mi", "\u03D1", null     , Ttype.CONST     ), //
			new Tupple("Theta"     , "mo", "\u0398", null     , Ttype.CONST     ), //
			new Tupple("upsilon"   , "mi", "\u03C5", null     , Ttype.CONST     ), //
			new Tupple("xi"        , "mi", "\u03BE", null     , Ttype.CONST     ), //
			new Tupple("Xi"        , "mo", "\u039E", null     , Ttype.CONST     ), //
			new Tupple("zeta"      , "mi", "\u03B6", null     , Ttype.CONST     ), //

			// binary operation symbols
			new Tupple("*"        , "mo"     , "\u22C5"   , "cdot"     , Ttype.CONST                          ), //
			new Tupple("**"       , "mo"     , "\u2217"   , "ast"      , Ttype.CONST                          ), //
			new Tupple("***"      , "mo"     , "\u22C6"   , "star"     , Ttype.CONST                          ), //
			new Tupple("//"       , "mo"     , "/"        , "/"        , Ttype.CONST      , "val", "notexcopy"), //
			new Tupple("\\\\"     , "mo"     , "\\"       , "backslash", Ttype.CONST                          ), //
			new Tupple("setminus" , "mo"     , "\\"       , null       , Ttype.CONST                          ), //
			new Tupple("xx"       , "mo"     , "\u00D7"   , "times"    , Ttype.CONST                          ), //
			new Tupple("|><"      , "mo"     , "\u22C9"   , "ltimes"   , Ttype.CONST                          ), //
			new Tupple("><|"      , "mo"     , "\u22CA"   , "rtimes"   , Ttype.CONST                          ), //
			new Tupple("|><|"     , "mo"     , "\u22C8"   , "bowtie"   , Ttype.CONST                          ), //
			new Tupple("-:"       , "mo"     , "\u00F7"   , "div"      , Ttype.CONST                          ), //
			new Tupple("divide"   , "mo"     , "-:"       , null       , Ttype.DEFINITION                     ), //
			new Tupple("@"        , "mo"     , "\u2218"   , "circ"     , Ttype.CONST                          ), //
			new Tupple("o+"       , "mo"     , "\u2295"   , "oplus"    , Ttype.CONST                          ), //
			new Tupple("ox"       , "mo"     , "\u2297"   , "otimes"   , Ttype.CONST                          ), //
			new Tupple("o."       , "mo"     , "\u2299"   , "odot"     , Ttype.CONST                          ), //
			new Tupple("sum"      , "mo"     , "\u2211"   , null       , Ttype.UNDEROVER                      ), //
			new Tupple("prod"     , "mo"     , "\u220F"   , null       , Ttype.UNDEROVER                      ), //
			new Tupple("^^"       , "mo"     , "\u2227"   , "wedge"    , Ttype.CONST                          ), //
			new Tupple("^^^"      , "mo"     , "\u22C0"   , "bigwedge" , Ttype.UNDEROVER                      ), //
			new Tupple("vv"       , "mo"     , "\u2228"   , "vee"      , Ttype.CONST                          ), //
			new Tupple("vvv"      , "mo"     , "\u22C1"   , "bigvee"   , Ttype.UNDEROVER                      ), //
			new Tupple("nn"       , "mo"     , "\u2229"   , "cap"      , Ttype.CONST                          ), //
			new Tupple("nnn"      , "mo"     , "\u22C2"   , "bigcap"   , Ttype.UNDEROVER                      ), //
			new Tupple("uu"       , "mo"     , "\u222A"   , "cup"      , Ttype.CONST                          ), //
			new Tupple("uuu"      , "mo"     , "\u22C3"   , "bigcup"   , Ttype.UNDEROVER                      ), //
			new Tupple("overset"  , "mover"  , "stackrel" , null       , Ttype.BINARY                         ), //
			new Tupple("underset" , "munder" , "stackrel" , null       , Ttype.BINARY                         ), //

			// binary relation symbols
			new Tupple("!="   , "mo", "\u2260" , "ne"                , Ttype.CONST      ), //
			new Tupple(":="   , "mo", ":="     , null                , Ttype.CONST      ), //
			new Tupple("lt"   , "mo", "<"      , null                , Ttype.CONST      ), //
			new Tupple("gt"   , "mo", ">"      , null                , Ttype.CONST      ), //
			new Tupple("<="   , "mo", "\u2264" , "le"                , Ttype.CONST      ), //
			new Tupple("lt="  , "mo", "\u2264" , "leq"               , Ttype.CONST      ), //
			new Tupple("gt="  , "mo", "\u2265" , "geq"               , Ttype.CONST      ), //
			new Tupple(">="   , "mo", "\u2265" , "ge"                , Ttype.CONST      ), //
			new Tupple("-<"   , "mo", "\u227A" , "prec"              , Ttype.CONST      ), //
			new Tupple("-lt"  , "mo", "\u227A" , null                , Ttype.CONST      ), //
			new Tupple(">-"   , "mo", "\u227B" , "succ"              , Ttype.CONST      ), //
			new Tupple("-<="  , "mo", "\u2AAF" , "preceq"            , Ttype.CONST      ), //
			new Tupple(">-="  , "mo", "\u2AB0" , "succeq"            , Ttype.CONST      ), //
			new Tupple("in"   , "mo", "\u2208" , null                , Ttype.CONST      ), //
			new Tupple("!in"  , "mo", "\u2209" , "notin"             , Ttype.CONST      ), //
			new Tupple("sub"  , "mo", "\u2282" , "subset"            , Ttype.CONST      ), //
			new Tupple("sup"  , "mo", "\u2283" , "supset"            , Ttype.CONST      ), //
			new Tupple("sube" , "mo", "\u2286" , "subseteq"          , Ttype.CONST      ), //
			new Tupple("supe" , "mo", "\u2287" , "supseteq"          , Ttype.CONST      ), //
			new Tupple("-="   , "mo", "\u2261" , "equiv"             , Ttype.CONST      ), //
			new Tupple("~="   , "mo", "\u2245" , "stackrel{\\sim}{=}", Ttype.CONST      ), //
			new Tupple("cong" , "mo", "~="     , null                , Ttype.DEFINITION ), //
			new Tupple("~~"   , "mo", "\u2248" , "approx"            , Ttype.CONST      ), //
			new Tupple("prop" , "mo", "\u221D" , "propto"            , Ttype.CONST      ), //

			// logical symbols
			new Tupple("and"    , "mtext", "and"   , null            , Ttype.SPACE     ), //
			new Tupple("or"     , "mtext", "or"    , null            , Ttype.SPACE     ), //
			new Tupple("not"    , "mo"   , "\u00AC", "neg"           , Ttype.CONST     ), //
			new Tupple("=>"     , "mo"   , "\u21D2", "Rightarrow"    , Ttype.CONST     ), //
			new Tupple("implies", "mo"   , "=>"    , null            , Ttype.DEFINITION), //
			new Tupple("if"     , "mo"   , "if"    , null            , Ttype.SPACE     ), //
			new Tupple("<=>"    , "mo"   , "\u21D4", "Leftrightarrow", Ttype.CONST     ), //
			new Tupple("iff"    , "mo"   , "<=>"   , null            , Ttype.DEFINITION), //
			new Tupple("AA"     , "mo"   , "\u2200", "forall"        , Ttype.CONST     ), //
			new Tupple("EE"     , "mo"   , "\u2203", "exists"        , Ttype.CONST     ), //
			new Tupple("_|_"    , "mo"   , "\u22A5", "bot"           , Ttype.CONST     ), //
			new Tupple("TT"     , "mo"   , "\u22A4", "top"           , Ttype.CONST     ), //
			new Tupple("|--"    , "mo"   , "\u22A2", "vdash"         , Ttype.CONST     ), //
			new Tupple("|=="    , "mo"   , "\u22A8", "models"        , Ttype.CONST     ), //

			// grouping brackets
			new Tupple("(" , "mo", "("     , null    , Ttype.LEFTBRACKET , "val"      ), //
			new Tupple(")" , "mo", ")"     , null    , Ttype.RIGHTBRACKET, "val"      ), //
			new Tupple("[" , "mo", "["     , null    , Ttype.LEFTBRACKET , "val"      ), //
			new Tupple("]" , "mo", "]"     , null    , Ttype.RIGHTBRACKET, "val"      ), //
			new Tupple("{" , "mo", "{"     , "lbrace", Ttype.LEFTBRACKET              ), //
			new Tupple("}" , "mo", "}"     , "rbrace", Ttype.RIGHTBRACKET             ), //
			new Tupple("|" , "mo", "|"     , null    , Ttype.LEFTRIGHT   , "val"      ), //
			new Tupple("(:", "mo", "\u2329", "langle", Ttype.LEFTBRACKET              ), //
			new Tupple(":)", "mo", "\u232A", "rangle", Ttype.RIGHTBRACKET             ), //
			new Tupple("<<", "mo", "\u2329", "langle", Ttype.LEFTBRACKET              ), //
			new Tupple(">>", "mo", "\u232A", "rangle", Ttype.RIGHTBRACKET             ), //
			new Tupple("{:", "mo", "{:"    , null    , Ttype.LEFTBRACKET , "invisible"), //
			new Tupple(":}", "mo", ":}"    , null    , Ttype.RIGHTBRACKET, "invisible"), //

			// miscellaneous symbols
			new Tupple("int"      , "mo", "\u222B"                   , null       , Ttype.CONST                     ), //
			new Tupple("dx"       , "mi", "{:d x:}"                  , null       , Ttype.DEFINITION                ), //
			new Tupple("dy"       , "mi", "{:d y:}"                  , null       , Ttype.DEFINITION                ), //
			new Tupple("dz"       , "mi", "{:d z:}"                  , null       , Ttype.DEFINITION                ), //
			new Tupple("dt"       , "mi", "{:d t:}"                  , null       , Ttype.DEFINITION                ), //
			new Tupple("oint"     , "mo", "\u222E"                   , null       , Ttype.CONST                     ), //
			new Tupple("del"      , "mo", "\u2202"                   , "partial"  , Ttype.CONST                     ), //
			new Tupple("grad"     , "mo", "\u2207"                   , "nabla"    , Ttype.CONST                     ), //
			new Tupple("+-"       , "mo", "\u00B1"                   , "pm"       , Ttype.CONST                     ), //
			new Tupple("O/"       , "mo", "\u2205"                   , "emptyset" , Ttype.CONST                     ), //
			new Tupple("oo"       , "mo", "\u221E"                   , "infty"    , Ttype.CONST                     ), //
			new Tupple("aleph"    , "mo", "\u2135"                   , null       , Ttype.CONST                     ), //
			new Tupple("..."      , "mo", "..."                      , "ldots"    , Ttype.CONST                     ), //
			new Tupple(":."       , "mo", "\u2234"                   , "therefore", Ttype.CONST                     ), //
			new Tupple(":'"       , "mo", "\u2235"                   , "because"  , Ttype.CONST                     ), //
			new Tupple("/_"       , "mo", "\u2220"                   , "angle"    , Ttype.CONST                     ), //
			new Tupple("/_\\"     , "mo", "\u25B3"                   , "triangle" , Ttype.CONST                     ), //
			new Tupple("\\ "      , "mo", "\u00A0"                   , null       , Ttype.CONST      , "val"        ), //
			new Tupple("frown"    , "mo", "\u2322"                   , null       , Ttype.CONST                     ), //
			new Tupple("%"        , "mo", "%"                        , "%"        , Ttype.CONST      , "notexcopy"  ), //
			new Tupple("quad"     , "mo", "\u00A0\u00A0"             , null       , Ttype.CONST                     ), //
			new Tupple("qquad"    , "mo", "\u00A0\u00A0\u00A0\u00A0" , null       , Ttype.CONST                     ), //
			new Tupple("cdots"    , "mo", "\u22EF"                   , null       , Ttype.CONST                     ), //
			new Tupple("vdots"    , "mo", "\u22EE"                   , null       , Ttype.CONST                     ), //
			new Tupple("ddots"    , "mo", "\u22F1"                   , null       , Ttype.CONST                     ), //
			new Tupple("diamond"  , "mo", "\u22C4"                   , null       , Ttype.CONST                     ), //
			new Tupple("square"   , "mo", "\u25A1"                   , "boxempty" , Ttype.CONST                     ), //
			new Tupple("|__"      , "mo", "\u230A"                   , "lfloor"   , Ttype.CONST                     ), //
			new Tupple("__|"      , "mo", "\u230B"                   , "rfloor"   , Ttype.CONST                     ), //
			new Tupple("|~"       , "mo", "\u2308"                   , "lceil"    , Ttype.CONST                     ), //
			new Tupple("lceiling" , "mo", "|~"                       , null       , Ttype.DEFINITION                ), //
			new Tupple("~|"       , "mo", "\u2309"                   , "rceil"    , Ttype.CONST                     ), //
			new Tupple("rceiling" , "mo", "~|"                       , null       , Ttype.DEFINITION                ), //
			new Tupple("CC"       , "mo", "\u2102"                   , "mathbb{C}", Ttype.CONST      , "notexcopy"  ), //
			new Tupple("NN"       , "mo", "\u2115"                   , "mathbb{N}", Ttype.CONST      , "notexcopy"  ), //
			new Tupple("QQ"       , "mo", "\u211A"                   , "mathbb{Q}", Ttype.CONST      , "notexcopy"  ), //
			new Tupple("RR"       , "mo", "\u211D"                   , "mathbb{R}", Ttype.CONST      , "notexcopy"  ), //
			new Tupple("ZZ"       , "mo", "\u2124"                   , "mathbb{Z}", Ttype.CONST      , "notexcopy"  ), //
			new Tupple("f"        , "mi", "f"                        , null       , Ttype.UNARY      , "func", "val"), //
			new Tupple("g"        , "mi", "g"                        , null       , Ttype.UNARY      , "func", "val"), //
			new Tupple("''"       , "mo", "''"                       , null       , Ttype.CONST      , "val"        ), //
			new Tupple("'''"      , "mo", "'''"                      , null       , Ttype.CONST      , "val"        ), //
			new Tupple("''''"     , "mo", "''''"                     , null       , Ttype.CONST      , "val"        ), //

			// standard functions
			new Tupple("lim"   , "mo", "lim"   , null, Ttype.UNDEROVER        ), //
			new Tupple("Lim"   , "mo", "Lim"   , null, Ttype.UNDEROVER        ), //
			new Tupple("sin"   , "mo", "sin"   , null, Ttype.UNARY    , "func"), //
			new Tupple("cos"   , "mo", "cos"   , null, Ttype.UNARY    , "func"), //
			new Tupple("tan"   , "mo", "tan"   , null, Ttype.UNARY    , "func"), //
			new Tupple("arcsin", "mo", "arcsin", null, Ttype.UNARY    , "func"), //
			new Tupple("arccos", "mo", "arccos", null, Ttype.UNARY    , "func"), //
			new Tupple("arctan", "mo", "arctan", null, Ttype.UNARY    , "func"), //
			new Tupple("sinh"  , "mo", "sinh"  , null, Ttype.UNARY    , "func"), //
			new Tupple("cosh"  , "mo", "cosh"  , null, Ttype.UNARY    , "func"), //
			new Tupple("tanh"  , "mo", "tanh"  , null, Ttype.UNARY    , "func"), //
			new Tupple("cot"   , "mo", "cot"   , null, Ttype.UNARY    , "func"), //
			new Tupple("coth"  , "mo", "coth"  , null, Ttype.UNARY    , "func"), //
			new Tupple("sech"  , "mo", "sech"  , null, Ttype.UNARY    , "func"), //
			new Tupple("csch"  , "mo", "csch"  , null, Ttype.UNARY    , "func"), //
			new Tupple("sec"   , "mo", "sec"   , null, Ttype.UNARY    , "func"), //
			new Tupple("csc"   , "mo", "csc"   , null, Ttype.UNARY    , "func"), //
			new Tupple("log"   , "mo", "log"   , null, Ttype.UNARY    , "func"), //
			new Tupple("ln"    , "mo", "ln"    , null, Ttype.UNARY    , "func"), //
			new Tupple(new String[] { "|"       , "|"        }, "abs"  , "mo", "abs"  , null, Ttype.UNARY, "notexcopy"), //
			new Tupple(new String[] { "\\|"     , "\\|"      }, "norm" , "mo", "norm" , null, Ttype.UNARY, "notexcopy"), //
			new Tupple(new String[] { "\\lfloor", "\\rfloor" }, "floor", "mo", "floor", null, Ttype.UNARY, "notexcopy"), //
			new Tupple(new String[] { "\\lceil" , "\\rceil"  }, "ceil" , "mo", "ceil" , null, Ttype.UNARY, "notexcopy"), //
			new Tupple("Sin"   , "mo", "sin"   , null, Ttype.UNARY, "func"), //
			new Tupple("Cos"   , "mo", "cos"   , null, Ttype.UNARY, "func"), //
			new Tupple("Tan"   , "mo", "tan"   , null, Ttype.UNARY, "func"), //
			new Tupple("Arcsin", "mo", "arcsin", null, Ttype.UNARY, "func"), //
			new Tupple("Arccos", "mo", "arccos", null, Ttype.UNARY, "func"), //
			new Tupple("Arctan", "mo", "arctan", null, Ttype.UNARY, "func"), //
			new Tupple("Sinh"  , "mo", "sinh"  , null, Ttype.UNARY, "func"), //
			new Tupple("Sosh"  , "mo", "cosh"  , null, Ttype.UNARY, "func"), //
			new Tupple("Tanh"  , "mo", "tanh"  , null, Ttype.UNARY, "func"), //
			new Tupple("Cot"   , "mo", "cot"   , null, Ttype.UNARY, "func"), //
			new Tupple("Sec"   , "mo", "sec"   , null, Ttype.UNARY, "func"), //
			new Tupple("Csc"   , "mo", "csc"   , null, Ttype.UNARY, "func"), //
			new Tupple("Log"   , "mo", "log"   , null, Ttype.UNARY, "func"), //
			new Tupple("Ln"    , "mo", "ln"    , null, Ttype.UNARY, "func"), //
			new Tupple(new String[] { "|", "|" }, "Abs", "mo", "abs", null, Ttype.UNARY, "notexcopy"), //
			new Tupple("det", "mo", "det", null       , Ttype.UNARY    , "func"             ), //
			new Tupple("exp", "mo", "exp", null       , Ttype.UNARY    , "func"             ), //
			new Tupple("dim", "mo", "dim", null       , Ttype.CONST                         ), //
			new Tupple("mod", "mo", "mod", "text{mod}", Ttype.CONST    , "notexcopy"        ), //
			new Tupple("gcd", "mo", "gcd", null       , Ttype.UNARY    , "func"             ), //
			new Tupple("lcm", "mo", "lcm", "text{lcm}", Ttype.UNARY    , "func", "notexcopy"), //
			new Tupple("lub", "mo", "lub", null       , Ttype.CONST                         ), //
			new Tupple("glb", "mo", "glb", null       , Ttype.CONST                         ), //
			new Tupple("min", "mo", "min", null       , Ttype.UNDEROVER                     ), //
			new Tupple("max", "mo", "max", null       , Ttype.UNDEROVER                     ), //

			// arrows
			new Tupple("uarr", "mo", "\u2191", "uparrow"              , Ttype.CONST), //
			new Tupple("darr", "mo", "\u2193", "downarrow"            , Ttype.CONST), //
			new Tupple("rarr", "mo", "\u2192", "rightarrow"           , Ttype.CONST), //
			new Tupple("->"  , "mo", "\u2192", "to"                   , Ttype.CONST), //
			new Tupple(">->" , "mo", "\u21A3", "rightarrowtail"       , Ttype.CONST), //
			new Tupple("->>" , "mo", "\u21A0", "twoheadrightarrow"    , Ttype.CONST), //
			new Tupple(">->>", "mo", "\u2916", "twoheadrightarrowtail", Ttype.CONST), //
			new Tupple("|->" , "mo", "\u21A6", "mapsto"               , Ttype.CONST), //
			new Tupple("larr", "mo", "\u2190", "leftarrow"            , Ttype.CONST), //
			new Tupple("harr", "mo", "\u2194", "leftrightarrow"       , Ttype.CONST), //
			new Tupple("rArr", "mo", "\u21D2", "Rightarrow"           , Ttype.CONST), //
			new Tupple("lArr", "mo", "\u21D0", "Leftarrow"            , Ttype.CONST), //
			new Tupple("hArr", "mo", "\u21D4", "Leftrightarrow"       , Ttype.CONST), //

			// commands with argument
			AMsqrt, AMroot, AMfrac, AMdiv, AMover, AMsub, AMsup,
			new Tupple("cancel", "menclose", "cancel", null        , Ttype.UNARY       ), //
			new Tupple("Sqrt"  , "msqrt"   , "sqrt"  , null        , Ttype.UNARY       ), //
			new Tupple("hat"   , "mover"   , "\u005E", null        , Ttype.UNARY, "acc"), //
			new Tupple("bar"   , "mover"   , "\u00AF", "overline"  , Ttype.UNARY, "acc"), //
			new Tupple("vec"   , "mover"   , "\u2192", null        , Ttype.UNARY, "acc"), //
			new Tupple("tilde" , "mover"   , "~"     , null        , Ttype.UNARY, "acc"), //
			new Tupple("dot"   , "mover"   , "."     , null        , Ttype.UNARY, "acc"), //
			new Tupple("ddot"  , "mover"   , ".."    , null        , Ttype.UNARY, "acc"), //
			new Tupple("ul"    , "munder"  , "\u0332", "underline" , Ttype.UNARY, "acc"), //
			new Tupple("ubrace", "munder"  , "\u23DF", "underbrace", Ttype.UNARY, "acc"), //
			new Tupple("obrace", "mover"   , "\u23DE", "overbrace" , Ttype.UNARY, "acc"), //
			AMtext, AMmbox, AMquote, //
			new Tupple("color", "mstyle", null, null, Ttype.BINARY), //
	} //
	));

	private static String[] AMnames;

	private static void AMinitSymbols() {
		int symlen = AMsymbols.size();
		for (int i = 0; i < symlen; i++) {
			if (AMsymbols.get(i).tex != null && !(AMsymbols.get(i).hasFlag("notexcopy"))) {
				Tupple tmp = AMsymbols.get(i).hasFlag("acc")
						? new Tupple(AMsymbols.get(i).tex, AMsymbols.get(i).tag, AMsymbols.get(i).output, null,
								AMsymbols.get(i).ttype, "acc")
						: new Tupple(AMsymbols.get(i).tex, AMsymbols.get(i).tag, AMsymbols.get(i).output, null,
								AMsymbols.get(i).ttype);
				AMsymbols.add(tmp);
			}
		}
		refreshSymbols();

	}

	private static void refreshSymbols() {
		Collections.sort(AMsymbols, new Comparator<Tupple>() {
			public int compare(Tupple o1, Tupple o2) {
				return o1.input.compareTo(o2.input);
			}
		});
		AMnames = new String[AMsymbols.size()];
		for (int i = 0; i < AMsymbols.size(); i++)
			AMnames[i] = AMsymbols.get(i).input;

	}

	private String AMremoveCharsAndBlanks(String str, int n) {
		// remove n characters and any following blanks
		String st;
		if (str.length() > 1 && str.length() > n && str.charAt(n) == '\\' && str.charAt(n + 1) != '\\' && str.charAt(n + 1) != ' ')
			st = slice(str, n + 1);
		else
			st = slice(str, n);
		int i;
		for (i = 0; i < st.length() && st.charAt(i) <= 32; i = i + 1)
			;
		return slice(st, i);
	}

	private int AMposition(String[] arr, String str, int n) {
		// return position >=n where str appears or would be inserted
		// assumes arr is sorted
		int i = 0;
		if (n == 0) {
			int h, m;
			n = -1;
			h = arr.length;
			while (n + 1 < h) {
				m = (n + h) >> 1;
				if (arr[m].compareTo(str) < 0)
					n = m;
				else
					h = m;
			}
			return h;
		} else {
			for (i = n; i < arr.length && arr[i].compareTo(str) < 0; i++)
				;
		}
		return i; // i=arr.length || arr[i]>=str
	}

	private Tupple AMgetSymbol(String str) {
		// return maximal initial substring of str that appears in names
		// return null if there is none
		int k = 0; // new pos
		int j = 0; // old pos
		int mk = 0; // match pos
		String st;
		String tagst;
		String match = "";
		boolean more = true;
		for (int i = 1; i <= str.length() && more; i++) {
			st = str.substring(0, i); // initial substring of length i
			j = k;
			k = AMposition(AMnames, st, j);
			if (k < AMnames.length && slice(str, 0, AMnames[k].length()).equals(AMnames[k])) {
				match = AMnames[k];
				mk = k;
				i = match.length();
			}
			more = k < AMnames.length && slice(str, 0, AMnames[k].length()).compareTo(AMnames[k]) >= 0;
		}
		AMpreviousSymbol = AMcurrentSymbol;
		if (match.equals("") == false) {
			AMcurrentSymbol = AMsymbols.get(mk).ttype;
			return AMsymbols.get(mk);
		}
		// if str[0] is a digit or - return maxsubstring of digits.digits
		AMcurrentSymbol = Ttype.CONST;
		k = 1;
		st = slice(str, 0, 1);
		boolean integ = true;

		while ("0".compareTo(st) <= 0 && st.compareTo("9") <= 0 && k <= str.length()) {
			st = slice(str, k, k + 1);
			k++;
		}

		if (st.equals(".")) {
			st = slice(str, k, k + 1);
			if ("0".compareTo(st) <= 0 && st.compareTo("9") <= 0) {
				integ = false;
				k++;
				while ("0".compareTo(st) <= 0 && st.compareTo("9") <= 0 && k <= str.length()) {
					st = slice(str, k, k + 1);
					k++;
				}
			}
		}
		if ((integ && k > 1) || k > 2) {
			st = slice(str, 0, k - 1);
			tagst = "mn";
		} else {
			k = 2;
			st = slice(str, 0, 1); // take 1 character
			tagst = (("A".compareTo(st) > 0 || st.compareTo("Z") > 0)
					&& ("a".compareTo(st) > 0 || st.compareTo("z") > 0) ? "mo" : "mi");
		}
		if (st.equals("-") && AMpreviousSymbol == Ttype.INFIX) {
			AMcurrentSymbol = Ttype.INFIX;
			return new Tupple(st, tagst, st, null, Ttype.UNARY, "func", "val");
		}
		return new Tupple(st, tagst, st, null, Ttype.CONST, "val"); // added val bit

	}

	private String AMTremoveBrackets(String node) {
		String st;
		if (node.length() > 1 && node.charAt(0) == '{' && node.charAt(node.length() - 1) == '}') {
			int leftchop = 0;

			st = substr(node, 1, 5);
			if (st.equals("\\left")) {
				st = "" + node.charAt(6);
				if (st.equals("(") || st.equals("[") || st.equals("{")) {
					leftchop = 7;
				} else {
					st = substr(node, 6, 7);
					if (st.equals("\\lbrace")) {
						leftchop = 13;
					}
				}
			} else {
				st = "" + node.charAt(1);
				if (st.equals("(") || st.equals("[")) {
					leftchop = 2;
				}
			}
			if (leftchop > 0) {
				st = node.substring(node.length() - 8);
				if (st.equals("\\right)}") || st.equals("\\right]}") || st.equals("\\right.}")) {
					node = "{" + node.substring(leftchop);
					node = node.substring(0, node.length() - 8) + "}";
				} else if (st.equals("\\rbrace}")) {
					node = "{" + node.substring(leftchop);
					node = node.substring(0, node.length() - 14) + "}";
				}
			}
		}
		return node;
	}

	private String AMTgetTeXsymbol(Tupple symb) {
		String pre;
		if (symb.hasFlag("val")) {
			pre = "";
		} else {
			pre = "\\";
		}
		if (symb.tex == null) {
			// can't remember why this was here. Breaks /delta /Delta to removed
			// return (pre+(pre==''?symb.input:symb.input.toLowerCase()));
			return (pre + symb.input);
		} else {
			return (pre + symb.tex);
		}
	}

	private String[] AMTparseSexpr(String str) {
		Tupple symbol;
		int i;
		String node, st;
		String newFrag = "";
		String result[];
		str = AMremoveCharsAndBlanks(str, 0);
		symbol = AMgetSymbol(str); // either a token or a bracket or empty
		if (symbol == null || symbol.ttype == Ttype.RIGHTBRACKET && AMnestingDepth > 0) {
			return new String[] { null, str };
		}
		if (symbol.ttype == Ttype.DEFINITION) {
			str = symbol.output + AMremoveCharsAndBlanks(str, symbol.input.length());
			symbol = AMgetSymbol(str);
		}
		switch (symbol.ttype) {
		case UNDEROVER:
		case CONST:
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			String texsymbol = AMTgetTeXsymbol(symbol);
			if (texsymbol.isEmpty() || texsymbol.charAt(0) == '\\' || symbol.tag.equals("mo"))
				return new String[] { texsymbol, str };
			else {
				return new String[] { "{" + texsymbol + "}", str };
			}

		case LEFTBRACKET: // read (expr+)
			AMnestingDepth++;
			str = AMremoveCharsAndBlanks(str, symbol.input.length());

			result = AMTparseExpr(str, true);
			AMnestingDepth--;
			int leftchop = 0;
			if (substr(result[0], 0, 6).equals("\\right")) {
				st = "" + result[0].charAt(6);
				if (st.equals(")") || st.equals("]") || st.equals("}")) {
					leftchop = 6;
				} else if (st.equals(".")) {
					leftchop = 7;
				} else {
					st = substr(result[0], 6, 7);
					if (st.equals("\\rbrace")) {
						leftchop = 13;
					}
				}
			}
			if (leftchop > 0) {
				result[0] = result[0].substring(leftchop);
				if (symbol.hasFlag("invisible"))
					node = "{" + result[0] + "}";
				else {
					node = "{" + AMTgetTeXsymbol(symbol) + result[0] + "}";
				}
			} else {
				if (symbol.hasFlag("invisible"))
					node = "{\\left." + result[0] + "}";
				else {
					node = "{\\left" + AMTgetTeXsymbol(symbol) + result[0] + "}";
				}
			}
			return new String[] { node, result[1] };

		case TEXT:
			if (symbol != AMquote)
				str = AMremoveCharsAndBlanks(str, symbol.input.length());
			if (str.charAt(0) == '{')
				i = str.indexOf("}");
			else if (str.charAt(0) == '(')
				i = str.indexOf(")");
			else if (str.charAt(0) == '[')
				i = str.indexOf("]");
			else if (symbol == AMquote)
				i = str.substring(1).indexOf("\"") + 1;
			else
				i = 0;
			if (i == -1)
				i = str.length();
			if (i == 0) {
				newFrag = "\\text{" + str.charAt(0) + "}";
			} else {
				st = str.substring(1, i);
				if (st.charAt(0) == ' ') {
					newFrag = "\\ ";
				}
				newFrag += "\\text{" + st + "}";
				if (st.charAt(st.length() - 1) == ' ') {
					newFrag += "\\ ";
				}
			}
			if (i == str.length())
				i = i - 1;
			str = AMremoveCharsAndBlanks(str, i + 1);
			return new String[] { newFrag, str };

		case UNARY:
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			result = AMTparseSexpr(str);
			if (result[0] == null)
				return new String[] { "{" + AMTgetTeXsymbol(symbol) + "}", str };
			if (symbol.hasFlag("func")) { // functions hack
				st = "" + (str.isEmpty() ? "" : str.charAt(0));
				if (st.equals("^") || st.equals("_") || st.equals("/") || st.equals("|") || st.equals(",")
						|| (symbol.input.length() == 1 && symbol.input.matches("\\w") && !st.equals("("))) {
					return new String[] { "{" + AMTgetTeXsymbol(symbol) + "}", str };
				} else {
					node = "{" + AMTgetTeXsymbol(symbol) + "{" + result[0] + "}}";
					return new String[] { node, result[1] };
				}
			}
			result[0] = AMTremoveBrackets(result[0]);
			if (symbol.input.equals("sqrt")) { // sqrt
				return new String[] { "\\sqrt{" + result[0] + "}", result[1] };
			} else if (symbol.input.equals("cancel")) { // cancel
				return new String[] { "\\cancel{" + result[0] + "}", result[1] };
			} else if (symbol.rewriteleftright != null) { // abs, floor, ceil
				return new String[] { "{\\left" + symbol.rewriteleftright[0] + result[0] + "\\right"
						+ symbol.rewriteleftright[1] + '}', result[1] };
			} else if (symbol.hasFlag("acc")) { // accent
				return new String[] { AMTgetTeXsymbol(symbol) + "{" + result[0] + "}", result[1] };
			} else { // font change command
				return new String[] { "{" + AMTgetTeXsymbol(symbol) + "{" + result[0] + "}}", result[1] };
			}
		case BINARY:
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			result = AMTparseSexpr(str);
			if (result[0] == null)
				return new String[] { '{' + AMTgetTeXsymbol(symbol) + '}', str };
			result[0] = AMTremoveBrackets(result[0]);
			String[] result2 = AMTparseSexpr(result[1]);
			if (result2[0] == null)
				return new String[] { '{' + AMTgetTeXsymbol(symbol) + '}', str };
			result2[0] = AMTremoveBrackets(result2[0]);
			if (symbol.input.equals("color")) {
				newFrag = "{\\color{" + result[0].replaceAll("[\\{\\}]", "") + "}" + result2[0] + "}";
			} else if (symbol.input.equals("root")) {
				newFrag = "{\\sqrt[" + result[0] + "]{" + result2[0] + "}}";
			} else {
				newFrag = "{" + AMTgetTeXsymbol(symbol) + "{" + result[0] + "}{" + result2[0] + "}}";
			}
			return new String[] { newFrag, result2[1] };
		case INFIX:
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			return new String[] { symbol.output, str };
		case SPACE:
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			return new String[] { "{\\quad\\text{" + symbol.input + "}\\quad}", str };
		case LEFTRIGHT:
			AMnestingDepth++;
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			result = AMTparseExpr(str, false);
			AMnestingDepth--;
			st = "" + result[0].charAt(result[0].length() - 1);
			if (st.equals("|")) { // its an absolute value subterm
				node = "{\\left|" + result[0] + "}";
				return new String[] { node, result[1] };
			} else { // the "|" is a \mid
				node = "{\\mid}";
				return new String[] { node, str };
			}
		default:
			// alert("default");
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			return new String[] { "{" + AMTgetTeXsymbol(symbol) + "}", str };

		}
	}

	private String[] AMTparseIexpr(String str) {
		Tupple symbol, sym1, sym2;
		String result[];
		String node;
		str = AMremoveCharsAndBlanks(str, 0);
		sym1 = AMgetSymbol(str);
		result = AMTparseSexpr(str);
		node = result[0];
		str = result[1];
		symbol = AMgetSymbol(str);
		if (symbol.ttype == Ttype.INFIX && !symbol.input.equals("/")) {
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			result = AMTparseSexpr(str);
			if (result[0] == null) // show box in place of missing argument
				result[0] = "{}";
			else
				result[0] = AMTremoveBrackets(result[0]);
			str = result[1];
			if (symbol.input.equals("_")) {
				sym2 = AMgetSymbol(str);
				if (sym2.input.equals("^")) {
					str = AMremoveCharsAndBlanks(str, sym2.input.length());
					String[] res2 = AMTparseSexpr(str);
					res2[0] = AMTremoveBrackets(res2[0]);
					str = res2[1];
					node = "{" + node;
					node += "_{" + result[0] + "}";
					node += "^{" + res2[0] + "}";
					node += "}";
				} else {
					node += "_{" + result[0] + "}";
				}
			} else { // must be ^
				node = node + "^{" + result[0] + "}";
			}
			if (sym1.hasFlag("func")) {
				sym2 = AMgetSymbol(str);
				if (sym2.ttype != Ttype.INFIX && sym2.ttype != Ttype.RIGHTBRACKET) {
					result = AMTparseIexpr(str);
					node = "{" + node + result[0] + "}";
					str = result[1];
				}
			}
		}
		return new String[] { node, str };
	}

	private String[] AMTparseExpr(String str, boolean rightbracket) {
		String result[];
		Tupple symbol;
		String node;
		// var symbol, node, result, i, nodeList = [],
		String newFrag = "";
		boolean addedright = false;
		do {
			str = AMremoveCharsAndBlanks(str, 0);
			result = AMTparseIexpr(str);
			node = result[0];
			str = result[1];
			symbol = AMgetSymbol(str);

			if (symbol.ttype == Ttype.INFIX && symbol.input.equals("/")) {
				str = AMremoveCharsAndBlanks(str, symbol.input.length());
				result = AMTparseIexpr(str);

				if (result[0] == null) // show box in place of missing argument
					result[0] = "{}";
				else
					result[0] = AMTremoveBrackets(result[0]);
				str = result[1];
				node = AMTremoveBrackets(node);
				node = "\\frac" + "{" + node + "}";
				node += "{" + result[0] + "}";
				newFrag += node;
				symbol = AMgetSymbol(str);
			} else if (node != null)
				newFrag += node;

		} while ((((symbol.ttype != Ttype.RIGHTBRACKET) && (symbol.ttype != Ttype.LEFTRIGHT || rightbracket))
				|| AMnestingDepth == 0) && (symbol.output == null || symbol.output.equals("") == false));

		if (symbol.ttype == Ttype.RIGHTBRACKET || symbol.ttype == Ttype.LEFTRIGHT) {
			int len = newFrag.length();
			if (len > 2 && newFrag.charAt(0) == '{' && newFrag.indexOf(',') > 0) {
				char right = newFrag.charAt(len - 2);
				if (right == ')' || right == ']') {
					char left = newFrag.charAt(6);
					if ((left == '(' && right == ')' && !symbol.output.equals("}")) || (left == '[' && right == ']')) {
						String mxout = "\\begin{matrix}";
						List<Integer> pos = new ArrayList<>(); // position of commas
						pos.add(0);
						boolean matrix = true;
						int mxnestingd = 0;
						List<List<Integer>> subpos = new ArrayList<>();
						subpos.add(new ArrayList<>(Arrays.asList(0)));
						int lastsubposstart = 0;
						int mxanynestingd = 0;

						for (int i = 1; i < len - 1; i++) {
							if (newFrag.charAt(i) == left)
								mxnestingd++;
							if (newFrag.charAt(i) == right) {
								mxnestingd--;
								if (mxnestingd == 0 && i + 3 < newFrag.length() && newFrag.charAt(i + 2) == ','
										&& newFrag.charAt(i + 3) == '{') {
									pos.add(i + 2);
									lastsubposstart = i + 2;
									while (subpos.size() <= lastsubposstart)
										subpos.add(null);
									subpos.set(lastsubposstart, new ArrayList<>(Arrays.asList(i + 2)));
								}
							}
							if (newFrag.charAt(i) == '[' || newFrag.charAt(i) == '(' || newFrag.charAt(i) == '{') {
								mxanynestingd++;
							}
							if (newFrag.charAt(i) == ']' || newFrag.charAt(i) == ')' || newFrag.charAt(i) == '}') {
								mxanynestingd--;
							}
							if (newFrag.charAt(i) == ',' && mxanynestingd == 1) {
								subpos.get(lastsubposstart).add(i);
							}
							if (mxanynestingd < 0) { // happens at the end of the row
								if (lastsubposstart == i + 1) { // if at end of row, skip to next row
									i++;
								} else { // misformed something - abandon treating as a matrix
									matrix = false;
								}
							}
						}

						pos.add(len);
						int lastmxsubcnt = -1;
						if (mxnestingd == 0 && pos.size() > 0 && matrix) {
							for (int i = 0; i < pos.size() - 1; i++) {
								List<String> subarr = null;
								if (i > 0)
									mxout += "\\\\";
								if (i == 0) {
									// var subarr = newFrag.substr(pos[i]+7,pos[i+1]-pos[i]-15).split(',');
									if (subpos.get(pos.get(i)).size() == 1) {
										subarr = new ArrayList<String>(Arrays.asList(
												substr(newFrag, pos.get(i) + 7, pos.get(i + 1) - pos.get(i) - 15)));
									} else {
										subarr = new ArrayList<String>(Arrays.asList(
												newFrag.substring(pos.get(i) + 7, subpos.get(pos.get(i)).get(1))));
										for (int j = 2; j < subpos.get(pos.get(i)).size(); j++) {
											subarr.add(newFrag.substring(subpos.get(pos.get(i)).get(j - 1) + 1,
													subpos.get(pos.get(i)).get(j)));
										}
										subarr.add(newFrag.substring(
												subpos.get(pos.get(i)).get(subpos.get(pos.get(i)).size() - 1) + 1,
												pos.get(i + 1) - 8));
									}
								} else {
									// var subarr = newFrag.substr(pos[i]+8,pos[i+1]-pos[i]-16).split(',');
									if (subpos.get(pos.get(i)).size() == 1) {
										subarr = new ArrayList<String>(Arrays.asList(
												substr(newFrag, pos.get(i) + 8, pos.get(i + 1) - pos.get(i) - 16)));
									} else {
										subarr = new ArrayList<String>(Arrays.asList(
												newFrag.substring(pos.get(i) + 8, subpos.get(pos.get(i)).get(1))));
										for (int j = 2; j < subpos.get(pos.get(i)).size(); j++) {
											subarr.add(newFrag.substring(subpos.get(pos.get(i)).get(j - 1) + 1,
												subpos.get(pos.get(i)).get(j)));
										}
										subarr.add(newFrag.substring(
												subpos.get(pos.get(i)).get(subpos.get(pos.get(i)).size() - 1) + 1,
												pos.get(i + 1) - 8));
									}
								}
								if (lastmxsubcnt > 0 && subarr.size() != lastmxsubcnt) {
									matrix = false;
								} else if (lastmxsubcnt == -1) {
									lastmxsubcnt = subarr.size();
								}
								// mxout += subarr.join('&');
								for (int z = 0; z < subarr.size(); z++) {
									mxout += subarr.get(z);
									if (z < subarr.size() - 1)
										mxout += "&";
								}
							}
						}
						mxout += "\\end{matrix}";

						if (matrix) {
							newFrag = mxout;
						}

					}
				}
			}
			str = AMremoveCharsAndBlanks(str, symbol.input.length());
			if (!symbol.hasFlag("invisible")) {
				node = "\\right" + AMTgetTeXsymbol(symbol);
				newFrag += node;
				addedright = true;
			} else {
				newFrag += "\\right.";
				addedright = true;
			}
		}
		if (AMnestingDepth > 0 && !addedright) {
			newFrag += "\\right."; // adjust for non-matching left brackets
			// todo: adjust for non-matching right brackets
		}

		return new String[] { newFrag, str };
	}

	private String patchColor(String latex) {
		return latex.replace("\\color{", "\\textcolor{");
	}

	public String getTeX(String asciiMathInput) {
		AMnestingDepth = 0;
		AMpreviousSymbol = Ttype.CONST;
		AMcurrentSymbol = Ttype.CONST;
		final String result = AMTparseExpr(asciiMathInput, false)[0];
		return patchColor(result);
	}

	static {
		AMinitSymbols();
	}

}