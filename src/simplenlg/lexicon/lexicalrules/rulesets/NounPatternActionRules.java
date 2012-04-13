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

public class NounPatternActionRules {		
	
	public static final PatternActionRule[] PLURAL_RULES =
		new PatternActionRule[] {
		//new PatternActionRule( "(um|(^ti)on)$", 2, "a", 1 ),
		//new PatternActionRule( "us$", 2, "i", 1 ),
		//new PatternActionRule( BasicPatterns.V+"x$", 1, "ces", 0 ),
		//new PatternActionRule( BasicPatterns.C + BasicPatterns.V + "$", 0, "es", 1),
			new PatternActionRule( BasicPatterns.C + "o$", 0, "es", 1),
			new PatternActionRule( BasicPatterns.C+"y$", 1, "ies", 1 ),						
			new PatternActionRule( "([zsx]|ch|sh)$", 0, "es", 1 ),			
			new PatternActionRule( BasicPatterns.VL+"fe$", 2, "ves", 1),
			new PatternActionRule( BasicPatterns.VL+"f$", 1, "ves", 1),	
			new PatternActionRule( "(eu|eau)$", 0, "x", 1),		
			new PatternActionRule( "(man|woman)$", 2, "en", 1),
			
			new PatternActionRule( "money$", 2, "ies", 0 ),
			new PatternActionRule( "motif$", 0, "s", 0),
			new PatternActionRule( "^meninx|phalanx$", 1, "ges", 0),			
			new PatternActionRule( "(xis|sis)$", 2, "es", 0 ),
			new PatternActionRule( "schema$", 0, "ta", 0),						
			new PatternActionRule( "^bus$", 0, "ses", 0),
			new PatternActionRule( "child$", 0, "ren", 0),
			new PatternActionRule( "^(curi|formul|vertebr|larv|uln|alumn|signor|alg)a$", 0, "e", 0 ), 			
			new PatternActionRule( "^corpus$", 2, "ora", 0 ),
			new PatternActionRule( "^(maharaj|raj|myn|mull)a$", 0, "hs", 0 ),	
			new PatternActionRule( "^aide-de-camp$", 8, "s-de-camp", 0),			
			new PatternActionRule( "^apex|cortex$", 2, "ices", 0),
			new PatternActionRule( "^weltanschauung$", 0, "en", 0 ),
			new PatternActionRule( "^lied$", 0, "er"),
			new PatternActionRule( "^tooth$", 4, "eeth", 0),
			new PatternActionRule( "^[lm]ouse$", 4, "ice"),
			new PatternActionRule( "^foot$", 3, "eet", 0 ),
			new PatternActionRule( "femur", 2, "ora", 0),
			new PatternActionRule( "goose", 4, "eese", 0),			
			new PatternActionRule( "(human|german|roman)$", 0, "s", 0),
			new PatternActionRule( "^(monarch|loch|stomach)$", 0, "s", 0),
			new PatternActionRule( "^(taxi|chief|proof|ref|relief|roof|belief)$", 0, "s", 0),			
			new PatternActionRule( "^(co|no)$", 0, "'s", 0),
			new PatternActionRule( "^(person)$", 4, "ople", 0),
			
			//Latin stems
			new PatternActionRule( "^(memorandum|bacterium|curriculum|minimum|" +
					"maximum|referendum|spectrum|phenomenon|criterion)$", 2, "a", 0),
			new PatternActionRule( "^(index|matrix)", 1, "ces", 0),
			new PatternActionRule( "^(stimulus|alumnus)$", 2, "i", 0),
			
			//Null Plural
			new PatternActionRule( "^(Bantu|Bengalese|Bengali|Beninese|Boche|bonsai|" +
					"Burmese|Chinese|Congolese|Gabonese|Guyanese|Japanese|Javanese|" +
					"Lebanese|Maltese|Olympics|Portuguese|Senegalese|Siamese|Singhalese|" +
					"Sinhalese|Sioux|Sudanese|Swiss|Taiwanese|Togolese|Vietnamese|aircraft|" +
					"anopheles|apparatus|asparagus|barracks|bellows|bison|bluefish|bob|bourgeois|" +
					"bream|brill|butterfingers|carp|catfish|chassis|clothes|chub|cod|codfish|" +
					"coley|contretemps|corps|crawfish|crayfish|crossroads|cuttlefish|dace|dice|documentation|" +
					"dogfish|doings|dory|downstairs|eldest|earnings|economics|electronics|finnan|" +
					"firstborn|fish|flatfish|flounder|fowl|fry|fries|works|globefish|goldfish|grief|golf|" +
					"grand|gudgeon|gulden|haddock|hake|halibut|headquarters|herring|hertz|horsepower|" +
					"goods|hovercraft|hundredweight|ironworks|jackanapes|kilohertz|kurus|kwacha|ling|lungfish|" +
					"mackerel|means|megahertz|moorfowl|moorgame|mullet|nepalese|offspring|pampas|parr|(pants$)|" +
					"patois|pekinese|penn'orth|perch|pickerel|pike|pince-nez|plaice|precis|quid|rand|" +
					"rendezvous|revers|roach|roux|salmon|samurai|series|seychelles|seychellois|shad|" +
					"sheep|shellfish|smelt|spacecraft|species|starfish|stockfish|sunfish|superficies|" +
					"sweepstakes|swordfish|tench|tennis|tope|triceps|trout|tuna|tunafish|tunny|turbot|trousers|" +
					"undersigned|veg|waterfowl|waterworks|waxworks|whiting|wildfowl|woodworm|" +
					"yen|aries|pisces|forceps|lieder|jeans|physics|mathematics|news|odds|politics|remains|" +
					"surroundings|thanks|statistics|goods|aids)$", 0, "", 0 )	
		};

	public static final PatternActionRule DEFAULT_PLURAL_RULE = 
		new PatternActionRule( BasicPatterns.ANY_STEM, 0, "s", 2 );

	
}
