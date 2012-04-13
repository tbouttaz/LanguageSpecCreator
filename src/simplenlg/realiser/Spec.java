/* Copyright (c) 2007, the University of Aberdeen
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
 */

package simplenlg.realiser;

import java.util.List;

import WYSIWYM.model.Anchor;

/**
 * Spec - parent class for all TextSpec and PhraseSpec
 *
 * @author ereiter
 * adapted by fhielkema May 2007
 *
 */
public abstract class Spec 
{
	private Anchor anchor;
	private boolean flash = false, elided = false;

	// Spec classes must have a realise method
	abstract public List<AnchorString> realise(Realiser r);
	
	// Promote to a specified text spec (or higher)
	// This is overriden by TextSpec, just deal with other cases here
	public TextSpec promote(DocStructure level) 
	{
		return new TextSpec(level, this);
	}
	
	public void setFlash(boolean f)
	{
		flash = f;
	}
	
	public boolean flash()
	{
		return flash;
	}
	
	public void setElided(boolean e)
	{
		elided = e;
	}
	
	public boolean isElided()
	{
		return elided;
	}
	
	public List<AnchorString> flash(List<AnchorString> list)
	{
		if (flash)
		{
			list.add(new AnchorString("</font>", null));
			list.add(0, new AnchorString("<font style=\"BACKGROUND-COLOR: #C3D9FF\">", null));
		}
		return list;
	}
	
	public void setAnchor(Anchor a)
	{
		anchor = a;
	}
	
	public Anchor getAnchor()
	{
		return anchor;
	}
}
