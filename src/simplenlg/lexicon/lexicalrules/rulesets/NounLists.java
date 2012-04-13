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

import java.util.Arrays;
import java.util.List;

public class NounLists {

	public static List<String> NULL_PLURAL = Arrays.asList(
			"Bantu", "Bengalese", "Bengali", "Beninese", "Boche", "bonsai", "Burmese",
			"Chinese", "Congolese", "Gabonese", "Guyanese", "Japanese",
			"Javanese", "Lebanese", "Maltese", "Olympics", "Portuguese",
			"Senegalese", "Siamese", "Singhalese", "Sinhalese", "Sioux",
			"Sudanese", "Swiss", "Taiwanese", "Togolese",
			"Vietnamese", "aircraft", "anopheles", "apparatus", "asparagus",
			"barracks", "bellows", "bison", "bluefish", "bob", "bourgeois",
			"bream", "brill", "butterfingers", "carp", "catfish", "chassis",
			"clothes", "chub", "cod", "codfish", "coley", "contretemps", "corps",
			"crawfish", "crayfish", "crossroads", "cuttlefish", "dace", "dice",
			"dogfish", "doings", "dory", "downstairs", "eldest", "earnings",
			"economics", "electronics", "finnan",
			"firstborn", "fish", "flatfish", "flounder", "fowl", "fry",
			"fries", "works", "globefish", "goldfish", "grief", "grand",
			"gudgeon", "gulden", "haddock", "hake", "halibut", "headquarters",
			"herring", "hertz", "horsepower", "hovercraft", "hundredweight",
			"ironworks", "jackanapes", "kilohertz", "kurus", "kwacha", "ling",
			"lungfish", "mackerel", "means", "megahertz", "moorfowl",
			"moorgame", "mullet", "nepalese", "offspring", "pampas", "parr",
			"patois", "pekinese", "penn'orth", "people", "perch", "pickerel",
			"pike", "pince-nez", "plaice", "precis", "quid", "rand",
			"rendezvous", "revers", "roach", "roux", "salmon", "samurai",
			"series", "seychelles", "seychellois", "shad", "sheep",
			"shellfish", "smelt", "spacecraft", "species", "starfish", 
			"stockfish", "sunfish", "superficies", "sweepstakes",
			"swordfish", "tench", "tope", "triceps", "trout", "tuna",
			"tunafish", "tunny", "turbot", "undersigned", "veg", "waterfowl",
			"waterworks", "waxworks", "whiting", "wildfowl", "woodworm", "yen",
			"aries", "pisces", "forceps", "lieder"	);

}
