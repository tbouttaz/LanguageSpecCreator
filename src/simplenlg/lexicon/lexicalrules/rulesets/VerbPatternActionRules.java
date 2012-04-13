/* ==========================================
 * Lexicon Generation API
 * ==========================================
 *
 * Copyright (c) 2007, the University of Aberdeen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted FOR RESEARCH PURPOSES ONLY, provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 * 		this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.
 * 3. Neither the name of the University of Aberdeen nor the names of its contributors 
 * 	  may be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 *    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 *    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 *    FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 *     ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *     EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *     
 *  Redistribution and use for purposes other than research requires special permission by the
 *  copyright holders and contributors. Please contact Ehud Reiter (ereiter@csd.abdn.ac.uk) for
 *  more information.
 *     
 *	   =================    
 *     Acknowledgements:
 *     =================
 *     This library contains a re-implementation of some rules derived from the MorphG package
 *     by Guido Minnen, John Carroll and Darren Pearce. You can find more information about MorphG
 *     in the following reference:
 *     	Minnen, G., Carroll, J., and Pearce, D. (2001). Applied Morphological Processing of English.
 *     		Natural Language Engineering 7(3): 207--223.
 *     Thanks to John Carroll for permission to re-use the MorphG rules. 
 */

package simplenlg.lexicon.lexicalrules.rulesets;

import simplenlg.lexicon.lexicalrules.PatternActionRule;

public class VerbPatternActionRules {

	/**
	 * Pattern-action rules to handle exceptional 3SG form in present tense
	 */
	public static PatternActionRule[] PRESENT_TENSE_RULES = new PatternActionRule[] {
			new PatternActionRule("^aby$", 0, "es", 0),
			new PatternActionRule("^bog-down$", 5, "s-down", 0),
			new PatternActionRule("^chivy$", 1, "vies", 0),
			new PatternActionRule("^gen-up$", 3, "s-up", 0),
			new PatternActionRule("^prologue$", 3, "gs", 0),
			new PatternActionRule("^picknic$", 0, "ks", 0),
			new PatternActionRule("^ko$", 0, "'s", 0),
			new PatternActionRule("[osz]$", 0, "es", 1),
			new PatternActionRule( "^have$", 2, "s", 0),
			new PatternActionRule(BasicPatterns.C + "y$", 1, "ies", 1),
			new PatternActionRule( "^be$", 2, "is" ),
			new PatternActionRule( "([zsx]|ch|sh)$", 0, "es", 1 )		
	};
			

	/**
	 * Default present 3sg rule
	 */
	public static PatternActionRule DEFAULT_PRESENT_TENSE = 
		new PatternActionRule(BasicPatterns.ANY_STEM, 0, "s", 2);
	
	/**
	 * Default pattern-action rules for ING-Form
	 */
	public static PatternActionRule[] ING_FORM_RULES = new PatternActionRule[] {
			new PatternActionRule(BasicPatterns.C + "ie$", 2, "ying", 1),
			new PatternActionRule("[^ie]e$", 1, "ing", 1),			
			new PatternActionRule("^bog-down$", 5, "ging-down", 0),
			new PatternActionRule("^chivy$", 1, "vying", 0),
			new PatternActionRule("^gen-up$", 3, "ning-up", 0),
			new PatternActionRule("^trek$", 1, "cking", 0),
			new PatternActionRule("^ko$", 0, "'ing", 0),
			new PatternActionRule( "(age|be)", 0, "ing", 0),			
	};
			

	/**
	 * Default ing-form rule
	 */
	public static PatternActionRule DEFAULT_ING_RULE = 
		new PatternActionRule(BasicPatterns.ANY_STEM, 0, "ing", 2);
	
	/**
	 * List of PastParticiple rules
	 */
	public static PatternActionRule[] PAST_PARTICIPLE_RULES = new PatternActionRule[] {
			new PatternActionRule("e$", 0, "d", 1),
			new PatternActionRule(BasicPatterns.C + "y$", 1, "ied", 1),
			
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX
					+ "?(take|rise|strew|blow|draw|drive|know|give|sake|"
					+ "arise|gnaw|grave|grow|hew|know|mow|see|sew|throw|"
					+ "partake|prove|saw|quartersaw|shake|shew|show|shrive|"
					+ "sightsee|strew|strive)$", 0, "n", 0),
			
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?[gd]o$", 0,"ne", 1),
			
			new PatternActionRule("^(beat|eat|be|fall)$", 0, "en", 0),

			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX + "?bid$", 0, "den", 0),			
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?[ls]ay$", 1, "id", 1),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX + "?have$", 2, "d", 0),
			new PatternActionRule("(sink|slink|drink)$", 3, "unk", 0),
			new PatternActionRule("(([sfc][twlp]?r?|w?r)ing|hang)$", 3, "ung", 0),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX
					+ "?(shear|swear|bear|wear|tear)$", 3, "orn", 0),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX
					+ "?(bend|spend|send|lend)$", 1, "t", 0),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX
					+ "?(weep|sleep|sweep|creep|keep$)$", 2, "pt", 0),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?(sell|tell)$",
					3, "old", 0),
			new PatternActionRule("^(outfight|beseech)$", 4, "ought", 0),
			new PatternActionRule("^bethink$", 3, "ought", 0),
			new PatternActionRule("^buy$", 2, "ought", 0),
			new PatternActionRule("^aby$", 1, "ought", 0),
			new PatternActionRule("^tarmac", 0, "ked", 0),
			new PatternActionRule("^abide$", 3, "ode", 0),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?(speak|(a?)wake|break)$", 3, "oken", 0),
			new PatternActionRule("^backbite$", 1, "ten", 0),
			new PatternActionRule("^backslide$", 1, "den", 0),
			new PatternActionRule("^become$", 3, "ame", 0),
			new PatternActionRule("^begird$", 3, "irt", 0),
			new PatternActionRule("^outlie$", 2, "ay", 0),
			new PatternActionRule("^rebind$", 3, "ound", 0),
			new PatternActionRule("^relay$", 2, "aid", 0),
			new PatternActionRule("^shit$", 3, "hat", 0),
			new PatternActionRule("^bereave$", 4, "eft", 0),
			new PatternActionRule("^foreswear$", 3, "ore", 0),
			new PatternActionRule("^overfly$", 1, "own", 0),
			new PatternActionRule("^beget$", 2, "otten", 0),
			new PatternActionRule("^begin$", 3, "gun", 0),
			new PatternActionRule("^bestride$", 1, "den", 0),			
			new PatternActionRule("^bite$", 1, "ten", 0),
			new PatternActionRule("^bleed$", 4, "led", 0),
			new PatternActionRule("^bog-down$", 5, "ged-down", 0),
			new PatternActionRule("^bind$", 3, "ound", 0),
			new PatternActionRule("^breastfeed$", 4, "fed", 0),
			new PatternActionRule("^breed$", 4, "red", 0),
			new PatternActionRule("^brei", 0, "d", 0),
			new PatternActionRule("^bring$", 3, "ought", 0),
			new PatternActionRule("^build$", 1, "t", 0),
			new PatternActionRule("^come", 0, "", 0),
			new PatternActionRule("^catch$", 3, "ught", 0),
			new PatternActionRule("^chivy$", 1, "vied", 0),
			new PatternActionRule("^choose$", 3, "sen", 0),
			new PatternActionRule("^cleave$", 4, "oven", 0),
			new PatternActionRule("^crossbreed$", 4, "red", 0),
			new PatternActionRule("^deal", 0, "t", 0),
			new PatternActionRule("^dow$", 1, "ught", 0),
			new PatternActionRule("^dream", 0, "t", 0),
			new PatternActionRule("^dig$", 3, "dug", 0),
			new PatternActionRule("^dwell$", 2, "lt", 0),
			new PatternActionRule("^enwind$", 3, "ound", 0),
			new PatternActionRule("^feed$", 4, "fed", 0),
			new PatternActionRule("^feel$", 3, "elt", 0),
			new PatternActionRule("^flee$", 2, "ed", 0),
			new PatternActionRule("^floodlight$", 5, "lit", 0),
			new PatternActionRule("^fly$", 1, "own", 0),
			new PatternActionRule("^forbear$", 3, "orne", 0),
			new PatternActionRule("^forerun$", 3, "ran", 0),
			new PatternActionRule("^forget$", 2, "otten", 0),
			new PatternActionRule("^fight$", 4, "ought", 0),
			new PatternActionRule("^find$", 3, "ound", 0),
			new PatternActionRule("^freeze$", 4, "ozen", 0),
			new PatternActionRule("^gainsay$", 2, "aid", 0),
			new PatternActionRule("^gin$", 3, "gan", 0),
			new PatternActionRule("^gen-up$", 3, "ned-up", 0),
			new PatternActionRule("^ghostwrite$", 1, "ten", 0),
			new PatternActionRule("^get$", 2, "otten", 0),
			new PatternActionRule("^grind$", 3, "ound", 0),
			new PatternActionRule("^hacksaw", 0, "n", 0),
			new PatternActionRule("^handfeed$", 4, "fed", 0),
			new PatternActionRule("^hear", 0, "d", 0),
			new PatternActionRule("^hold$", 3, "eld", 0),
			new PatternActionRule("^hide$", 1, "den", 0),
			new PatternActionRule("^honey$", 2, "ied", 0),
			new PatternActionRule("^inbreed$", 4, "red", 0),
			new PatternActionRule("^indwell$", 3, "elt", 0),
			new PatternActionRule("^interbreed$", 4, "red", 0),
			new PatternActionRule("^interweave$", 4, "oven", 0),
			new PatternActionRule("^inweave$", 4, "oven", 0),
			new PatternActionRule("^ken$", 2, "ent", 0),
			new PatternActionRule("^kneel$", 3, "elt", 0),
			new PatternActionRule("^lie$", 2, "ain", 0),
			new PatternActionRule("^leap$", 0, "t", 0),
			new PatternActionRule("^learn$", 0, "t", 0),
			new PatternActionRule("^lead$", 4, "led", 0),
			new PatternActionRule("^leave$", 4, "eft", 0),
			new PatternActionRule("^light$", 5, "lit", 0),
			new PatternActionRule("^lose$", 3, "ost", 0),
			new PatternActionRule("^make$", 3, "ade", 0),
			new PatternActionRule("^mean", 0, "t", 0),
			new PatternActionRule("^meet$", 4, "met", 0),
			new PatternActionRule("^misbecome$", 3, "ame", 0),
			new PatternActionRule("^misdeal$", 2, "alt", 0),
			new PatternActionRule("^mishear$", 1, "d", 0),
			new PatternActionRule("^mislead$", 4, "led", 0),
			new PatternActionRule("^misunderstand$", 3, "ood", 0),
			new PatternActionRule("^outbreed$", 4, "red", 0),
			new PatternActionRule("^outrun$", 3, "ran", 0),
			new PatternActionRule("^outride$", 1, "den", 0),
			new PatternActionRule("^outshine$", 3, "one", 0),
			new PatternActionRule("^outshoot$", 4, "hot", 0),
			new PatternActionRule("^outstand$", 3, "ood", 0),
			new PatternActionRule("^outthink$", 3, "ought", 0),
			new PatternActionRule("^outgo$", 2, "went", 0),
			new PatternActionRule("^overbear$", 3, "orne", 0),
			new PatternActionRule("^overbuild$", 3, "ilt", 0),
			new PatternActionRule("^overcome$", 3, "ame", 0),
			new PatternActionRule("^overfly$", 2, "lew", 0),
			new PatternActionRule("^overhear$", 2, "ard", 0),
			new PatternActionRule("^overlie$", 2, "ain", 0),
			new PatternActionRule("^overrun$", 3, "ran", 0),
			new PatternActionRule("^override$", 1, "den", 0),
			new PatternActionRule("^overshoot$", 4, "hot", 0),
			new PatternActionRule("^overwind$", 3, "ound", 0),
			new PatternActionRule("^overwrite$", 1, "ten", 0),
			new PatternActionRule("^run$", 3, "ran", 0),
			new PatternActionRule("^rebuild$", 3, "ilt", 0),
			new PatternActionRule("^red$", 3, "red", 0),
			new PatternActionRule("^redo$", 1, "one", 0),
			new PatternActionRule("^remake$", 3, "ade", 0),
			new PatternActionRule("^rerun$", 3, "ran", 0),
			new PatternActionRule("^resit$", 3, "sat", 0),
			new PatternActionRule("^rethink$", 3, "ought", 0),
			new PatternActionRule("^rewind$", 3, "ound", 0),
			new PatternActionRule("^rewrite$", 1, "ten", 0),
			new PatternActionRule("^ride$", 1, "den", 0),
			new PatternActionRule("^reeve$", 4, "ove", 0),
			new PatternActionRule("^sit$", 3, "sat", 0),
			new PatternActionRule("^shoe$", 3, "hod", 0),
			new PatternActionRule("^shine$", 3, "one", 0),
			new PatternActionRule("^shoot$", 4, "hot", 0),
			new PatternActionRule("^ski$", 1, "i'd", 0),
			new PatternActionRule("^slide$", 1, "den", 0),
			new PatternActionRule("^smite$", 1, "ten", 0),
			new PatternActionRule("^seek$", 3, "ought", 0),
			new PatternActionRule("^spit$", 3, "pat", 0),
			new PatternActionRule("^speed$", 4, "ped", 0),
			new PatternActionRule("^spellbind$", 3, "ound", 0),
			new PatternActionRule("^spoil$", 2, "ilt", 0),
			new PatternActionRule("^spotlight$", 5, "lit", 0),
			new PatternActionRule("^spin$", 3, "pun", 0),
			new PatternActionRule("^steal$", 3, "olen", 0),
			new PatternActionRule("^stand$", 3, "ood", 0),
			new PatternActionRule("^stave$", 3, "ove", 0),
			new PatternActionRule("^stride$", 1, "den", 0),
			new PatternActionRule("^strike$", 3, "uck", 0),
			new PatternActionRule("^stick$", 3, "uck", 0),
			new PatternActionRule("^swell$", 3, "ollen", 0),
			new PatternActionRule("^swim$", 3, "wum", 0),
			new PatternActionRule("^teach$", 4, "aught", 0),
			new PatternActionRule("^think$", 3, "ought", 0),
			new PatternActionRule("^tread$", 3, "odden", 0),
			new PatternActionRule("^typewrite$", 1, "ten", 0),
			new PatternActionRule("^unbind$", 3, "ound", 0),
			new PatternActionRule("^underbuy$", 2, "ought", 0),
			new PatternActionRule("^underfeed$", 4, "fed", 0),
			new PatternActionRule("^undergird$", 3, "irt", 0),
			new PatternActionRule("^undergo$", 1, "one", 0),
			new PatternActionRule("^underlie$", 2, "ain", 0),
			new PatternActionRule("^undershoot$", 4, "hot", 0),
			new PatternActionRule("^understand$", 3, "ood", 0),
			new PatternActionRule("^unfreeze$", 4, "ozen", 0),
			new PatternActionRule("^unlearn", 0, "t", 0),
			new PatternActionRule("^unmake$", 3, "ade", 0),
			new PatternActionRule("^unreeve$", 4, "ove", 0),
			new PatternActionRule("^unstick$", 3, "uck", 0),
			new PatternActionRule("^unteach$", 4, "aught", 0),
			new PatternActionRule("^unthink$", 3, "ought", 0),
			new PatternActionRule("^untread$", 3, "odden", 0),
			new PatternActionRule("^unwind$", 3, "ound", 0),
			new PatternActionRule("^upbuild$", 1, "t", 0),
			new PatternActionRule("^uphold$", 3, "eld", 0),
			new PatternActionRule("^upheave$", 4, "ove", 0),
			new PatternActionRule("^waylay$", 2, "ain", 0),
			new PatternActionRule("^whipsaw$", 2, "awn", 0),
			new PatternActionRule("^winterfeed$", 4, "fed", 0),
			new PatternActionRule("^withhold$", 3, "eld", 0),
			new PatternActionRule("^withstand$", 3, "ood", 0),
			new PatternActionRule("^win$", 3, "won", 0),
			new PatternActionRule("^wind$", 3, "ound", 0),
			new PatternActionRule("^weave$", 4, "oven", 0),
			new PatternActionRule("^write$", 1, "ten", 0),
			new PatternActionRule("^trek$", 1, "cked", 0),
			new PatternActionRule("^ko$", 1, "o'd", 0),			
			new PatternActionRule( "^win$", 2, "on", 0),
			
			//null past forms
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX +
					"?(cast|thrust|typeset|cut|bid|upset|wet|bet|cut|" +
					"hit|hurt|inset|let|cost|burst|beat|beset|set|upset|hit|" +
					"offset|put|quit|wed|typeset|wed|spread|split|slit|read|run|shut|shed)$", 0, "", 0)						
	};

	/**
	 * Default past participle rule
	 */
	public static PatternActionRule DEFAULT_PP_RULE = 
		new PatternActionRule(BasicPatterns.ANY_STEM, 0, "ed", 2);	
	
	/**
	 * List of Past Tense rules
	 */
	public static PatternActionRule[] PAST_TENSE_RULES = new PatternActionRule[] {						
			new PatternActionRule("e$", 0, "d", 1),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?[ls]ay$", 1, "id", 1),
			new PatternActionRule(BasicPatterns.C + "y$", 1, "ied", 1),
			
			new PatternActionRule("(([sfc][twlp]?r?|w?r)ing)$", 3, "ang", 1),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX
					+ "?(bend|spend|send|lend|spend)$", 1, "t", 0),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?lie$", 2,
					"ay", 0),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX
					+ "?(weep|sleep|sweep|creep|keep)$", 2, "pt", 0),
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?(sell|tell)$",
					3, "old", 0),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX + "?do$", 1, "id", 0),			
			new PatternActionRule("^" + BasicPatterns.VERBAL_PREFIX + "?dig$", 2, "ug", 0),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX + "?have$", 2, "d", 0),									
			new PatternActionRule("(sink|drink)$", 3, "ank", 0),
			new PatternActionRule("^be$", 2, "was", 0),
			new PatternActionRule("^outfight$", 4, "ought", 0),
			new PatternActionRule("^tarmac", 0, "ked", 0),
			new PatternActionRule("^abide$", 3, "ode", 0),
			new PatternActionRule("^aby$", 1, "ought", 0),
			new PatternActionRule("^become$", 3, "ame", 0),
			new PatternActionRule("^begird$", 3, "irt", 0),
			new PatternActionRule("^outlie$", 2, "ay", 0),
			new PatternActionRule("^rebind$", 3, "ound", 0),
			new PatternActionRule("^shit$", 3, "hat", 0),
			new PatternActionRule("^bereave$", 4, "eft", 0),
			new PatternActionRule("^foreswear$", 3, "ore", 0),
			new PatternActionRule("^bename$", 3, "empt", 0),
			new PatternActionRule("^beseech$", 4, "ought", 0),
			new PatternActionRule("^bethink$", 3, "ought", 0),
			new PatternActionRule("^bleed$", 4, "led", 0),
			new PatternActionRule("^bog-down$", 5, "ged-down", 0),
			new PatternActionRule("^buy$", 2, "ought", 0),
			new PatternActionRule("^bind$", 3, "ound", 0),
			new PatternActionRule("^breastfeed$", 4, "fed", 0),
			new PatternActionRule("^breed$", 4, "red", 0),
			new PatternActionRule("^brei$", 2, "eid", 0),
			new PatternActionRule("^bring$", 3, "ought", 0),
			new PatternActionRule("^build$", 3, "ilt", 0),
			new PatternActionRule("^come$", 3, "ame", 0),
			new PatternActionRule("^catch$", 3, "ught", 0),
			new PatternActionRule("^clothe$", 5, "lad", 0),
			new PatternActionRule("^crossbreed$", 4, "red", 0),
			new PatternActionRule("^deal$", 2, "alt", 0),
			new PatternActionRule("^dow$", 1, "ught", 0),
			new PatternActionRule("^dream$", 2, "amt", 0),
			new PatternActionRule("^dwell$", 3, "elt", 0),
			new PatternActionRule("^enwind$", 3, "ound", 0),
			new PatternActionRule("^feed$", 4, "fed", 0),
			new PatternActionRule("^feel$", 3, "elt", 0),
			new PatternActionRule("^flee$", 3, "led", 0),
			new PatternActionRule("^floodlight$", 5, "lit", 0),			
			new PatternActionRule("^arise$", 3, "ose", 0),
			new PatternActionRule("^eat$", 3, "ate", 0),
			new PatternActionRule("^awake$", 3, "oke", 0),
			new PatternActionRule("^backbite$", 4, "bit", 0),
			new PatternActionRule("^backslide$", 4, "lid", 0),			
			new PatternActionRule("^befall$", 3, "ell", 0),
			new PatternActionRule("^begin$", 3, "gan", 0),
			new PatternActionRule("^beget$", 3, "got", 0),
			new PatternActionRule("^behold$", 3, "eld", 0),
			new PatternActionRule("^bespeak$", 3, "oke", 0),
			new PatternActionRule("^bestride$", 3, "ode", 0),
			new PatternActionRule("^betake$", 3, "ook", 0),
			new PatternActionRule("^bite$", 4, "bit", 0),
			new PatternActionRule("^blow$", 3, "lew", 0),
			new PatternActionRule("^bear$", 3, "ore", 0),
			new PatternActionRule("^break$", 3, "oke", 0),
			new PatternActionRule("^choose$", 4, "ose", 0),
			new PatternActionRule("^cleave$", 4, "ove", 0),
			new PatternActionRule("^countersink$", 3, "ank", 0),
			new PatternActionRule("^drink$", 3, "ank", 0),
			new PatternActionRule("^draw$", 3, "rew", 0),
			new PatternActionRule("^drive$", 3, "ove", 0),
			new PatternActionRule("^fall$", 3, "ell", 0),
			new PatternActionRule("^fly$", 2, "lew", 0),
			new PatternActionRule("^flyblow$", 3, "lew", 0),
			new PatternActionRule("^forbid$", 2, "ade", 0),
			new PatternActionRule("^forbear$", 3, "ore", 0),						
			new PatternActionRule("^foreknow$", 3, "new", 0),
			new PatternActionRule("^foresee$", 3, "saw", 0),
			new PatternActionRule("^forespeak$", 3, "oke", 0),
			new PatternActionRule("^forego$", 2, "went", 0),
			new PatternActionRule("^forgive$", 3, "ave", 0),
			new PatternActionRule("^forget$", 3, "got", 0),
			new PatternActionRule("^forsake$", 3, "ook", 0),
			new PatternActionRule("^forspeak$", 3, "oke", 0),
			new PatternActionRule("^forswear$", 3, "ore", 0),
			new PatternActionRule("^forgo$", 2, "went", 0),
			new PatternActionRule("^fight$", 4, "ought", 0),
			new PatternActionRule("^find$", 3, "ound", 0),
			new PatternActionRule("^freeze$", 4, "oze", 0),
			new PatternActionRule("^give$", 3, "ave", 0),
			new PatternActionRule("^geld$", 3, "elt", 0),
			new PatternActionRule("^gen-up$", 3, "ned-up", 0),
			new PatternActionRule("^ghostwrite$", 3, "ote", 0),
			new PatternActionRule("^get$", 3, "got", 0),
			new PatternActionRule("^grow$", 3, "rew", 0),
			new PatternActionRule("^grind$", 3, "ound", 0),
			new PatternActionRule("^handfeed$", 4, "fed", 0),
			new PatternActionRule("^hear$", 2, "ard", 0),
			new PatternActionRule("^hold$", 3, "eld", 0),
			new PatternActionRule("^hide$", 4, "hid", 0),
			new PatternActionRule("^honey$", 2, "ied", 0),
			new PatternActionRule("^inbreed$", 4, "red", 0),
			new PatternActionRule("^indwell$", 3, "elt", 0),
			new PatternActionRule("^interbreed$", 4, "red", 0),
			new PatternActionRule("^interweave$", 4, "ove", 0),
			new PatternActionRule("^inweave$", 4, "ove", 0),
			new PatternActionRule("^ken$", 2, "ent", 0),
			new PatternActionRule("^kneel$", 3, "elt", 0),
			new PatternActionRule("^^know$$", 3, "new", 0),
			new PatternActionRule("^leap$", 2, "apt", 0),
			new PatternActionRule("^learn$", 2, "rnt", 0),
			new PatternActionRule("^lead$", 4, "led", 0),
			new PatternActionRule("^leave$", 4, "eft", 0),
			new PatternActionRule("^light$", 5, "lit", 0),
			new PatternActionRule("^lose$", 3, "ost", 0),
			new PatternActionRule("^make$", 3, "ade", 0),
			new PatternActionRule("^mean$", 2, "ant", 0),
			new PatternActionRule("^meet$", 4, "met", 0),
			new PatternActionRule("^misbecome$", 3, "ame", 0),
			new PatternActionRule("^misdeal$", 2, "alt", 0),
			new PatternActionRule("^misgive$", 3, "ave", 0),
			new PatternActionRule("^mishear$", 2, "ard", 0),
			new PatternActionRule("^mislead$", 4, "led", 0),
			new PatternActionRule("^mistake$", 3, "ook", 0),
			new PatternActionRule("^misunderstand$", 3, "ood", 0),
			new PatternActionRule("^outbreed$", 4, "red", 0),		
			new PatternActionRule("^outgrow$", 3, "rew", 0),			
			new PatternActionRule("^outride$", 3, "ode", 0),
			new PatternActionRule("^outshine$", 3, "one", 0),
			new PatternActionRule("^outshoot$", 4, "hot", 0),
			new PatternActionRule("^outstand$", 3, "ood", 0),
			new PatternActionRule("^outthink$", 3, "ought", 0),
			new PatternActionRule("^outgo$", 2, "went", 0),
			new PatternActionRule("^outwear$", 3, "ore", 0),
			new PatternActionRule("^overblow$", 3, "lew", 0),
			new PatternActionRule("^overbear$", 3, "ore", 0),
			new PatternActionRule("^overbuild$", 3, "ilt", 0),
			new PatternActionRule("^overcome$", 3, "ame", 0),			
			new PatternActionRule("^overdraw$", 3, "rew", 0),
			new PatternActionRule("^overdrive$", 3, "ove", 0),
			new PatternActionRule("^overfly$", 2, "lew", 0),
			new PatternActionRule("^overgrow$", 3, "rew", 0),
			new PatternActionRule("^overhear$", 2, "ard", 0),
			new PatternActionRule("^overpass$", 3, "ast", 0),		
			new PatternActionRule("^override$", 3, "ode", 0),
			new PatternActionRule("^oversee$", 3, "saw", 0),
			new PatternActionRule("^overshoot$", 4, "hot", 0),
			new PatternActionRule("^overthrow$", 3, "rew", 0),
			new PatternActionRule("^overtake$", 3, "ook", 0),
			new PatternActionRule("^overwind$", 3, "ound", 0),
			new PatternActionRule("^overwrite$", 3, "ote", 0),
			new PatternActionRule("^partake$", 3, "ook", 0),
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX + "?run$", 2, "an", 0),
			new PatternActionRule("^ring$", 3, "ang", 0),
			new PatternActionRule("^rebuild$", 3, "ilt", 0),
			new PatternActionRule("^red", 0, "", 0),			
			new PatternActionRule("^reave$", 4, "eft", 0),
			new PatternActionRule("^remake$", 3, "ade", 0),			
			new PatternActionRule("^resit$", 3, "sat", 0),
			new PatternActionRule("^rethink$", 3, "ought", 0),
			new PatternActionRule("^retake$", 3, "ook", 0),
			new PatternActionRule("^rewind$", 3, "ound", 0),
			new PatternActionRule("^rewrite$", 3, "ote", 0),
			new PatternActionRule("^ride$", 3, "ode", 0),
			new PatternActionRule("^rise$", 3, "ose", 0),
			new PatternActionRule("^reeve$", 4, "ove", 0),
			new PatternActionRule("^sing$", 3, "ang", 0),
			new PatternActionRule("^sink$", 3, "ank", 0),
			new PatternActionRule("^sit$", 3, "sat", 0),
			new PatternActionRule("^see$", 3, "saw", 0),
			new PatternActionRule("^shoe$", 3, "hod", 0),
			new PatternActionRule("^shine$", 3, "one", 0),
			new PatternActionRule("^shake$", 3, "ook", 0),
			new PatternActionRule("^shoot$", 4, "hot", 0),
			new PatternActionRule("^shrink$", 3, "ank", 0),
			new PatternActionRule("^shrive$", 3, "ove", 0),
			new PatternActionRule("^sightsee$", 3, "saw", 0),
			new PatternActionRule("^ski$", 1, "i'd", 0),
			new PatternActionRule("^skydive$", 3, "ove", 0),
			new PatternActionRule("^slay$", 3, "lew", 0),
			new PatternActionRule("^slide$", 4, "lid", 0),
			new PatternActionRule("^slink$", 3, "unk", 0),
			new PatternActionRule("^smite$", 4, "mit", 0),
			new PatternActionRule("^seek$", 3, "ought", 0),
			new PatternActionRule("^spit$", 3, "pat", 0),
			new PatternActionRule("^speed$", 4, "ped", 0),
			new PatternActionRule("^spellbind$", 3, "ound", 0),
			new PatternActionRule("^spoil$", 2, "ilt", 0),
			new PatternActionRule("^speak$", 3, "oke", 0),
			new PatternActionRule("^spotlight$", 5, "lit", 0),
			new PatternActionRule("^spring$", 3, "ang", 0),
			new PatternActionRule("^spin$", 3, "pun", 0),
			new PatternActionRule("^stink$", 3, "ank", 0),
			new PatternActionRule("^steal$", 3, "ole", 0),
			new PatternActionRule("^stand$", 3, "ood", 0),
			new PatternActionRule("^stave$", 3, "ove", 0),
			new PatternActionRule("^stride$", 3, "ode", 0),
			new PatternActionRule("^strive$", 3, "ove", 0),
			new PatternActionRule("^strike$", 3, "uck", 0),
			new PatternActionRule("^stick$", 3, "uck", 0),
			new PatternActionRule("^swim$", 3, "wam", 0),
			new PatternActionRule("^swear$", 3, "ore", 0),
			new PatternActionRule("^teach$", 4, "aught", 0),
			new PatternActionRule("^think$", 3, "ought", 0),
			new PatternActionRule("^throw$", 3, "rew", 0),
			new PatternActionRule("^take$", 3, "ook", 0),
			new PatternActionRule("^tear$", 3, "ore", 0),
			new PatternActionRule("^transship$", 4, "hip", 0),
			new PatternActionRule("^tread$", 4, "rod", 0),
			new PatternActionRule("^typewrite$", 3, "ote", 0),
			new PatternActionRule("^unbind$", 3, "ound", 0),
			new PatternActionRule("^unclothe$", 5, "lad", 0),
			new PatternActionRule("^underbuy$", 2, "ought", 0),
			new PatternActionRule("^underfeed$", 4, "fed", 0),
			new PatternActionRule("^undergird$", 3, "irt", 0),
			new PatternActionRule("^undershoot$", 4, "hot", 0),
			new PatternActionRule("^understand$", 3, "ood", 0),
			new PatternActionRule("^undertake$", 3, "ook", 0),
			new PatternActionRule("^undergo$", 2, "went", 0),
			new PatternActionRule("^underwrite$", 3, "ote", 0),			
			new PatternActionRule("^unfreeze$", 4, "oze", 0),
			new PatternActionRule("^unlearn$", 2, "rnt", 0),
			new PatternActionRule("^unmake$", 3, "ade", 0),
			new PatternActionRule("^unreeve$", 4, "ove", 0),
			new PatternActionRule("^unspeak$", 3, "oke", 0),
			new PatternActionRule("^unstick$", 3, "uck", 0),
			new PatternActionRule("^unswear$", 3, "ore", 0),
			new PatternActionRule("^unteach$", 4, "aught", 0),
			new PatternActionRule("^unthink$", 3, "ought", 0),
			new PatternActionRule("^untread$", 4, "rod", 0),
			new PatternActionRule("^unwind$", 3, "ound", 0),
			new PatternActionRule("^upbuild$", 3, "ilt", 0),
			new PatternActionRule("^uphold$", 3, "eld", 0),
			new PatternActionRule("^upheave$", 4, "ove", 0),
			new PatternActionRule("^uprise$", 3, "ose", 0),
			new PatternActionRule("^upspring$", 3, "ang", 0),
			new PatternActionRule("^go$", 2, "went", 0),
			new PatternActionRule("^winterfeed$", 4, "fed", 0),
			new PatternActionRule("^wiredraw$", 3, "rew", 0),
			new PatternActionRule("^withdraw$", 3, "rew", 0),
			new PatternActionRule("^withhold$", 3, "eld", 0),
			new PatternActionRule("^withstand$", 3, "ood", 0),
			new PatternActionRule("^wake$", 3, "oke", 0),
			new PatternActionRule("^win$", 3, "won", 0),
			new PatternActionRule("^wear$", 3, "ore", 0),
			new PatternActionRule("^wind$", 3, "ound", 0),
			new PatternActionRule("^weave$", 4, "ove", 0),
			new PatternActionRule("^write$", 3, "ote", 0),
			new PatternActionRule("^trek$", 1, "cked", 0),
			new PatternActionRule("^ko$", 1, "o'd", 0),
			new PatternActionRule( "^bid", 2, "ade", 0),
			new PatternActionRule( "^win$", 2, "on", 0),
			new PatternActionRule( "^swim", 2, "am", 0 ),
						
			//null past forms
			new PatternActionRule( "^" + BasicPatterns.VERBAL_PREFIX +
					"?(cast|thrust|typeset|cut|bid|upset|wet|bet|cut|" +
					"hit|hurt|inset|let|cost|burst|beat|beset|set|upset|hit|" +
					"offset|put|quit|wed|typeset|wed|spread|split|slit|read|run|shut|shed)$", 0, "", 0)		
	
	};
	
	
	/**
	 * Default past tense rule
	 */
	public static PatternActionRule DEFAULT_PAST_RULE = 
		new PatternActionRule(BasicPatterns.ANY_STEM, 0, "ed", 2);

}
