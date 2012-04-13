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

package simplenlg.lexicon;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import simplenlg.features.Number;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.lexicon.lexicalitems.Adjective;
import simplenlg.lexicon.lexicalitems.Category;
import simplenlg.lexicon.lexicalitems.Determiner;
import simplenlg.lexicon.lexicalitems.LexicalItem;
import simplenlg.lexicon.lexicalitems.Noun;
import simplenlg.lexicon.lexicalitems.Pronoun;
import simplenlg.lexicon.lexicalitems.Symbol;
import simplenlg.lexicon.lexicalitems.Verb;
import simplenlg.lexicon.lexicalrules.MorphologicalRule;
import simplenlg.lexicon.lexicalrules.rulesets.SymbolLists;
import simplenlg.lexicon.lexicalrules.rulesets.VerbLists;

/**
 * This class implements the <code>LexiconInterface</code>, providing a
 * number of additional methods which operate either directly on strings, or on
 * LexicalItems. The <code>Lexicon</code> is therefore both a repository of
 * lexical information, and the source from which a finite set of morphological
 * rules can be applied to strings.
 * <p>
 * The <code>Lexicon</code> comes pre-loaded with a few simplenlg.exception lists,
 * expecially for verbs which either take no affixation in any form, or have
 * suppletive forms, or take consonant doubling. It also loads a small set of
 * symbols. For all other exceptions to the morphological rules defined here, it
 * relies on the predefined pattern-action rules in the package
 * {@link simplenlg.lexicon.lexicalrules.rulesets}.
 * <p>
 * A <code>Lexicon</code> contains separate stores for the four classes that
 * inherit from {@link simplenlg.lexicon.lexicalitems.ContentWord}, namely:
 * {@link simplenlg.lexicon.lexicalitems.Verb}s, {@link simplenlg.lexicon.lexicalitems.Noun}s,
 * {@link simplenlg.lexicon.lexicalitems.Adjective}s and
 * {@link simplenlg.lexicon.lexicalitems.Symbol}s. The function words defined in this
 * package, namely {@link simplenlg.lexicon.lexicalitems.Determiner} and
 * {@link simplenlg.lexicon.lexicalitems.Pronoun}, are not stored directly, as these are
 * constants. The <code>Lexicon</code> has access to these items, however.
 * 
 * @author albert gatt
 */
public class Lexicon implements LexiconInterface {

	private TreeMap<String, Noun> nouns;

	private TreeMap<String, Adjective> adjectives;

	private TreeMap<String, Verb> verbs;

	private TreeMap<String, Symbol> symbols;

	private PreparedStatement retrieveItems;

	private TreeMap<String, MorphologicalRule> rules;

	public static final Verb NULL_VERB = new Verb("");
		
	/**
	 * Creates a new instance of <code>DefaultLexicon</code>. The constructor
	 * initialises four maps, <code>verbs</code>, <code>nouns</code>,
	 * <code>adjectives</code> and <code>symbols</code>, which store
	 * <code>ContentWord</code>s. It also loads a list of verb exceptions and
	 * symbols at construction time. These are found in
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbLists} and
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.SymbolLists}.
	 */
	public Lexicon() {
		nouns = new TreeMap<String, Noun>();
		verbs = new TreeMap<String, Verb>();
		adjectives = new TreeMap<String, Adjective>();
		symbols = new TreeMap<String, Symbol>();
		addVerbExceptions();
		addSymbols();
		addNounExceptions();
	}

	public void addItem(Category cat, String word) {

		switch (cat) {

		case NOUN:
			nouns.put(word, new Noun(word));
			break;

		case VERB:
			Verb v = new Verb(word);

			if (VerbLists.NULL_AFFIX_VERB_LIST.contains(word)) {
				v.setIsNullAffixVerb(true);
			}

			verbs.put(word, v);
			break;

		case ADJECTIVE:
			adjectives.put(word, new Adjective(word));
			break;

		case SYMBOL:
			symbols.put(word, new Symbol(word));

		default:
			; // do nothing

		}
	}

	public void addItem(LexicalItem lex) {

		switch (lex.getCategory()) {

		case NOUN:
			nouns.put(lex.getBaseForm().toString(), (Noun) lex);
			break;

		case VERB:
			Verb v = (Verb) lex;

			if (VerbLists.NULL_AFFIX_VERB_LIST.contains(v.getBaseForm())) {
				v.setIsNullAffixVerb(true);
			}

			verbs.put(v.getBaseForm().toString(), v);
			break;

		case ADJECTIVE:
			adjectives.put(lex.getBaseForm().toString(), (Adjective) lex);
			break;

		case PRONOUN:
		case DETERMINER:
		case CONJUNCTION:
		default:
			; // do nothing
		}
	}

	public LexicalItem getItem(String base) {

		if (nouns.containsKey(base)) {
			return nouns.get(base);

		} else if (verbs.containsKey(base)) {
			return verbs.get(base);

		} else if (adjectives.containsKey(base)) {
			return adjectives.get(base);

		} else {
			return null;
		}
	}

	public LexicalItem getItem(Category cat, String word) {

		switch (cat) {

		case NOUN:
			return nouns.get(word);

		case ADJECTIVE:
			return adjectives.get(word);

		case VERB:
			return verbs.get(word);

		case SYMBOL:
			return symbols.get(word);

		case PRONOUN:
			return Pronoun.getPronoun(word);

		case DETERMINER:
			return Determiner.getDeterminer(word);

		default:
			return null;
		}
	}

	public boolean hasItem(Category cat, String word) {

		switch (cat) {

		case NOUN:
			return nouns.containsKey(word);

		case ADJECTIVE:
			return adjectives.containsKey(word);

		case VERB:
			return verbs.containsKey(word);

		case SYMBOL:
			return symbols.containsKey(word);

		case DETERMINER:
			return true;

		case PRONOUN:
			return true;

		default:
			return false;
		}
	}

	public boolean hasItem(String word) {

		if (Pronoun.getPronoun(word) != null) {
			return true;

		} else if (Determiner.getDeterminer(word) != null) {
			return true;

		} else {
			return (nouns.containsKey(word) || adjectives.containsKey(word)
					|| verbs.containsKey(word) || symbols.containsKey(word));
		}
	}

	/**
	 * Get a complete list of the nouns in this Lexicon
	 * 
	 * @return A <code>java.util.List<Noun></code> of <code>Noun</code>s
	 *         contained in this <code>DefaultLexicon</code>.
	 */
	public List<Noun> getNouns() {
		return new ArrayList<Noun>(nouns.values());
	}

	/**
	 * Get a complete list of the verbs in this Lexicon
	 * 
	 * @return A <code>java.util.List<Verb></code> of <code>Verb</code>s
	 *         contained in this <code>DefaultLexicon</code>.
	 */
	public List<Verb> getVerbs() {
		return new ArrayList<Verb>(verbs.values());
	}

	/**
	 * Get a complete list of the adjectives in this Lexicon
	 * 
	 * @return A <code>java.util.List<Adjective></code> of
	 *         <code>Adjective</code>s contained in this
	 *         <code>DefaultLexicon</code>.
	 */
	public List<Adjective> getAdjectives() {
		return new ArrayList<Adjective>(adjectives.values());
	}

	/**
	 * Get a complete list of the adjectives in this Lexicon
	 * 
	 * @return A <code>java.util.List<Adjective></code> of
	 *         <code>Symbol</code>s contained in this
	 *         <code>DefaultLexicon</code>.
	 */
	public List<Symbol> getSymbols() {
		return new ArrayList<Symbol>(symbols.values());
	}

	/**
	 * Populates the <code>DefaultLexicon</code> with items from a file. Files
	 * MUST be in the format "word fieldsep category", where
	 * <code>fieldsep</code> is a field-separator (comma, space, tab or
	 * whatever), which is specified as an argument, <code>category</code> is
	 * one of "noun", "adj", "verb", "sym"
	 * 
	 * <p>
	 * Note that this method can be called several times. Calling it multiple
	 * times causes the <code>DefaultLexicon</code> to add items, but none of
	 * the previously inserted items will be deleted.
	 * 
	 * @param filename
	 *            The path to the file.
	 * @param fieldSeparator
	 *            The symbol separating words and categories in the file.
	 * @throws <code>IOException</code> if the file is not found.
	 */
	public void populateFromFile(String filename, String fieldSeparator)
			throws IOException {
		try {
			File f = new File(filename);
			FileReader file = new FileReader(f.getAbsolutePath());

			BufferedReader reader = new BufferedReader(file);

			while (reader.ready()) {
				String nextLine = reader.readLine().trim();
				StringTokenizer tok = new StringTokenizer(nextLine,
						fieldSeparator);

				if (tok.countTokens() > 2) {
					throw new InternalError(
							"Cannot process files with more than two fields per line");
				}

				String word = null;
				int i = 0;

				while (tok.hasMoreTokens()) {
					i++;

					if (i == 1) {
						word = tok.nextToken();

					} else if (i == 2) {
						addItem(Category.getCategory(tok.nextToken()), word);
					}
				}
			}

		} catch (IOException ioe) {
			throw new IOException(ioe.getMessage());
		}
	}

	/**
	 * Retrieve lexical items from a database. The method expects three String
	 * arguments: a table name, the name of a word field, and the name of a
	 * category field. The word field should contain the lemma or string
	 * representation of the word; the category field should specify whether
	 * it's a noun, verb or adjective.
	 * <p>
	 * Example: Suppose you have a database "wordDB", containing table "myTable"
	 * which looks like this:
	 * <p>
	 * -------------------<br> | word | category |<br>
	 * ------------------- <br>
	 * |dog | noun | <br>
	 * |cat | noun | <br>
	 * |run | verb | <br>
	 * ------------------<br>
	 * <p>
	 * You can load these into the simplenlg.lexicon using this method, giving it
	 * arguments "myTable", "word", "category".
	 * 
	 * @param con
	 *            A database connection handler. This should have already
	 *            established a connection to the database successfully.
	 * @param table
	 *            The table name.
	 * @param wordField
	 *            The name of the field in <code>table</code> which contains
	 *            words.
	 * @param categoryField
	 *            The name of the field in <code>table</code> which contains
	 *            grammatical category.
	 * @throws an
	 *             <code>SQLException</code> if the execution of the SQL
	 *             Statement fails.
	 */
	public void populateFromDatabase(Connection con, String table,
			String wordField, String categoryField) throws SQLException {

		try {

			retrieveItems = con.prepareStatement("SELECT " + wordField + ", "
					+ categoryField + "FROM " + table + " WHERE 1");
			ResultSet result = retrieveItems.executeQuery();

			while (result.next()) {
				String word = result.getString(0).trim();
				Category cat = Category.getCategory(result.getString(1).trim());
				addItem(cat, word);
			}

		} catch (SQLException sqlex) {
			throw new SQLException(sqlex.getMessage());
		}
	}

	/**
	 * Gets the plural form of a word. In actual fact, the
	 * <code>DefaultLexicon</code> checks whether it already contains a
	 * <code>Noun</code> whose baseform is <code>word</code>. If that is
	 * the case, it returns the plural form by calling
	 * {@link simplenlg.lexicon.lexicalitems.Noun#getPlural()} from the <code>Noun</code>.
	 * If not, it creates a new instance of <code>Noun</code>, which gets
	 * stored in this instance of <code>DefaultLexicon</code>, returning the
	 * plural form.
	 * 
	 * @param word
	 *            The baseform of some word that needs to be pluralised
	 * @return A <code>String</code> which is the plural of <code>word</code>
	 */
	public String getPlural(String word) {
		//if it's a phrase containing >1 word, only check the last word
		String expr = word;
		int idx = word.lastIndexOf(" ");
		if (idx > -1)
			expr = word.substring(idx + 1);
		
		String plural = null;
		if (hasItem(Category.NOUN, expr)) {
			plural = this.nouns.get(expr).getPlural();

		} else {
			Noun noun = new Noun(expr);
			nouns.put(expr, noun);
			plural = noun.getPlural();
		}
		String first = word.substring(0, idx + 1);
		return new String(first + plural); 
	}

	/**
	 * Gets the comparative form of an adjective. In actual fact, the
	 * <code>DefaultLexicon</code> checks whether it already contains a
	 * <code>Adjective</code> whose baseform is <code>word</code>. If that
	 * is the case, it returns the comparative form by calling
	 * {@link simplenlg.lexicon.lexicalitems.Adjective#getComparative()} from the
	 * <code>Adjective</code>. If not, it creates a new instance of
	 * <code>Adjective</code>, which gets stored in this instance of
	 * <code>DefaultLexicon</code>, returning the comparative form.
	 * 
	 * @param word -
	 *            The baseform of some adjective
	 * @return A <code>String</code> which is the comparative form of
	 *         <code>word</code>
	 */
	public String getComparative(String word) {

		if (hasItem(Category.ADJECTIVE, word)) {
			return adjectives.get(word).getComparative();

		} else {
			Adjective adj = new Adjective(word);
			adjectives.put(word, adj);
			return adj.getComparative();
		}

	}

	/**
	 * Gets the superlative form of an adjective. In actual fact, the
	 * <code>DefaultLexicon</code> checks whether it already contains a
	 * <code>Adjective</code> whose baseform is <code>word</code>. If that
	 * is the case, it returns the superlative form by calling
	 * {@link simplenlg.lexicon.lexicalitems.Adjective#getSuperlative()} from the
	 * <code>Adjective</code>. If not, it creates a new instance of
	 * <code>Adjective</code>, which gets stored in this instance of
	 * <code>DefaultLexicon</code>, returning the superlative form.
	 * 
	 * @param word -
	 *            The baseform of some adjective
	 * @return A <code>String</code> which is the comparative form of
	 *         <code>word</code>
	 */
	public String getSuperlative(String word) {

		if (hasItem(Category.ADJECTIVE, word)) {
			return adjectives.get(word).getSuperlative();

		} else {
			Adjective adj = new Adjective(word);
			adjectives.put(word, adj);
			return adj.getSuperlative();
		}

	}

	/**
	 * Gets the past tense form of a verb. Whenever called,
	 * <code>DefaultLexicon</code> checks whether it already contains a
	 * <code>Verb</code> whose baseform is <code>word</code>. If that is
	 * the case, it returns the past tense form by calling
	 * {@link simplenlg.lexicon.lexicalitems.Verb#getPast()} from the <code>Verb</code>.
	 * If not, it creates a new instance of <code>Verb</code>, which gets
	 * stored in this instance of <code>DefaultLexicon</code>, and returns
	 * the past tense form form.
	 * 
	 * @param word
	 *            The baseform of some verb
	 * @return A <code>String</code> which is the past tense form of
	 *         <code>word</code>
	 */
	public String getPast(String word) {

		if (hasItem(Category.VERB, word)) {
			return this.verbs.get(word).getPast().toString();

		} else {
			Verb verb = new Verb(word);
			verbs.put(word, verb);
			return verb.getPast().toString();
		}
	}

	/**
	 * Gets the past participle form of a verb. Whenever called,
	 * <code>DefaultLexicon</code> checks whether it already contains a
	 * <code>Verb</code> whose baseform is <code>word</code>. If that is
	 * the case, it returns the past participle by calling
	 * {@link simplenlg.lexicon.lexicalitems.Verb#getPastParticiple()} from the
	 * <code>Verb</code>. If not, it creates a new instance of
	 * <code>Verb</code>, which gets stored in this instance of
	 * <code>DefaultLexicon</code>, and returns the past participle form.
	 * 
	 * @param word
	 *            The baseform of some verb
	 * @return A <code>String</code> which is the past participle form of
	 *         <code>word</code>
	 */
	public String getPastParticiple(String word) {

		if (hasItem(Category.VERB, word)) {
			return this.verbs.get(word).getPastParticiple().toString();

		} else {
			Verb verb = new Verb(word);
			verbs.put(word, verb);
			return verb.getPastParticiple().toString();
		}
	}

	/**
	 * Gets the 3rd person present tense form of a verb. Whenever called,
	 * <code>DefaultLexicon</code> checks whether it already contains a
	 * <code>Verb</code> whose baseform is <code>word</code>. If that is
	 * the case, it returns the 3rd person present by calling
	 * {@link simplenlg.lexicon.lexicalitems.Verb#getPresent3SG()} from the
	 * <code>Verb</code>. If not, it creates a new instance of
	 * <code>Verb</code>, which gets stored in this instance of
	 * <code>DefaultLexicon</code>, and returns the present tense form.
	 * 
	 * @param word
	 *            The baseform of some verb
	 * @return A <code>String</code> which is the 3rd person singular present
	 *         tense form of <code>word</code>
	 */
	public String getPresent3SG(String word) {

		if (hasItem(Category.VERB, word)) {
			return this.verbs.get(word).getPresent3SG().toString();

		} else {
			Verb verb = new Verb(word);
			verbs.put(word, verb);
			return verb.getPresent3SG().toString();
		}
	}

	/**
	 * Gets the "ing" (present participle or continuous) form of a verb.
	 * Whenever called, <code>DefaultLexicon</code> checks whether it already
	 * contains a <code>Verb</code> whose baseform is <code>word</code>. If
	 * that is the case, it returns the "ing" form by calling
	 * {@link simplenlg.lexicon.lexicalitems.Verb#getPresentParticiple()} from the
	 * <code>Verb</code>. If not, it creates a new instance of
	 * <code>Verb</code>, which gets stored in this instance of
	 * <code>DefaultLexicon</code>, and returns the "ing" form.
	 * 
	 * @param word
	 *            The baseform of some verb
	 * @return A <code>String</code> which is the "ing" form of
	 *         <code>word</code>
	 */
	public String getPresentParticiple(String word) {

		if (hasItem(Category.VERB, word)) {
			return this.verbs.get(word).getPresentParticiple().toString();

		} else {
			Verb verb = new Verb(word);
			verbs.put(word, verb);
			return verb.getPresentParticiple().toString();
		}
	}

	/**
	 * An alias for {@link simplenlg.lexicon.Lexicon#getPresentParticiple(String)}
	 * 
	 * @param word
	 *            The baseform of some verb
	 * @return The "ing" form.
	 */
	public String getContinuous(String word) {
		return getPresentParticiple(word).toString();
	}

	/**
	 * An alias for {@link simplenlg.lexicon.Lexicon#getPresentParticiple(String)}
	 * 
	 * @param word
	 *            The baseform of some verb
	 * @return The "ing" form.
	 */
	public String getIngForm(String word) {
		return getPresentParticiple(word).toString();
	}

	/**
	 * Add a morphological rule to this simplenlg.lexicon. The rule must implement the
	 * <code>MorphologicalRule</code> interface. This is the easiest way to
	 * add functionality to the <code>DefaultLexicon</code>. New
	 * <code>MorphologicalRule</code>s added in this way are indexed by their
	 * name, the return value of the <code>MorphologicalRule.getName()</code>
	 * method.
	 * 
	 * @see simplenlg.lexicon.lexicalrules.MorphologicalRule
	 * @param rule
	 *            The <code>MorphologicalRule</code> to be added.
	 */
	public void addRule(MorphologicalRule rule) {

		if (rules == null) {
			rules = new TreeMap<String, MorphologicalRule>();
		}

		rules.put(rule.getName(), rule);
	}

	/**
	 * Apply a <code>MorphologicalRule</code> with name <code>ruleName</code>
	 * to word <code>word</code>. The rule must already have been added to
	 * the <code>DefaultLexicon</code>. If no such rule exists, the return
	 * value is <code>null</code>.
	 * 
	 * @param ruleName
	 *            The name of the <code>MorphologicalRule</code>
	 * @param word
	 *            The <code>String</code> to which the rule should be applied.
	 * @return The result of applying the rule.
	 */
	public String applyRule(String ruleName, String word) {

		try {
			return rules.get(ruleName).apply(word);

		} catch (NullPointerException npe) {
			return null;
		}
	}

	/**
	 * Apply a <code>MorphologicalRule</code> with name <code>ruleName</code>
	 * to <code>LexicalItem</code> <code>lex</code>. The rule must already
	 * have been added to the <code>DefaultLexicon</code>. If no such rule
	 * exists, the return value is <code>null</code>.
	 * 
	 * @param ruleName
	 *            The name of the <code>MorphologicalRule</code>
	 * @param lex
	 *            The <code>LexicalItem</code> to which the rule should be
	 *            applied.
	 * @return The result of applying the rule.
	 */
	public String applyRule(String ruleName, LexicalItem lex) {

		try {
			return rules.get(ruleName).apply(lex);

		} catch (NullPointerException npe) {
			return null;
		}
	}

	/**
	 * Utility method: get a form of a verb in a specific <code>Tense</code> with a 
	 * specific <code>Person</code> and <code>Number</code> configuration.
	 * @param v The base form of the verb
	 * @param t The <code>Tense</code>
	 * @param p The <code>Person</code>
	 * @param n The <code>Number</code> combination 
	 * @return A <code>java.lang.String</code>, the inflected form.
	 */
	public String getVerbForm(String v, Tense t, Person p, Number n) {
		Verb verb;		
		if (hasItem(Category.VERB, v)) {
			verb = (Verb) getItem(Category.VERB, v);
			
		} else {
			verb = new Verb(v);
			addItem(Category.VERB, v);
		}		
		
		String result = null;
		switch (t) {
		case PRESENT:
			result = verb.getPresent(p, n).get(0).toString(); break;
		case PAST:
			result = verb.getPast(p, n).get(0).toString(); break;
		default:
			result = verb.getBaseForm().toString(); break;
		}
		return result;
	}

	/**
	 * 
	 * @return Utility method: The form of the verb "to be" used in subjunctive
	 *         mood (conditionals), i.e. "were"
	 */
	public String getBeSubjunctive() {
		return "were";
	}

	private void addVerbExceptions() {
		// the verb "to be"
		Verb be = new Verb("be");		
		verbs.put("be", be);

		// the verb "to have"
		Verb have = new Verb("have");
		verbs.put("have", have);

		// the verb "to do"
		Verb doing = new Verb("do");
		verbs.put("do", doing);

		// load list of consonant doubling verbs
		for (String word : VerbLists.CONS_DOUBLING) {
			Verb newVerb = new Verb(word);
			newVerb.setConsonantDoubling(true);
			verbs.put(word, newVerb);
		}

		// load list of verbs which are never inflected
		for (String word : VerbLists.NULL_AFFIX_VERB_LIST) {
			Verb verb = new Verb(word);
			verb.setIsNullAffixVerb(true);
			verbs.put(word, verb);
		}
	}
	
	/*	Quick way to ensure that 'meeting notes' and 'statistical data' don't get pluralised.
	 *	Obviously the 'notes' bit is rather a cheap hack!
	 */
	private void addNounExceptions()
	{	
		nouns.put("data", new Noun("data", "data"));
		nouns.put("notes", new Noun("notes", "notes"));
	}

	// load symbol list
	private void addSymbols() {

		for (String symb : SymbolLists.SYMBOLS) {
			symbols.put(symb, new Symbol(symb));
		}

	}

}
