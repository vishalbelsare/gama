/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC 
 * 
 * Developers :
 * 
 * - Alexis Drogoul, IRD (Kernel, Metamodel, XML-based GAML), 2007-2011
 * - Vo Duc An, IRD & AUF (SWT integration, multi-level architecture), 2008-2011
 * - Patrick Taillandier, AUF & CNRS (batch framework, GeoTools & JTS integration), 2009-2011
 * - Pierrick Koch, IRD (XText-based GAML environment), 2010-2011
 * - Romain Lavaud, IRD (project-based environment), 2010
 * - Francois Sempe, IRD & AUF (EMF behavioral model, batch framework), 2007-2009
 * - Edouard Amouroux, IRD (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, IRD (OpenMap integration), 2007-2008
 */
package msi.gaml.extensions.fipa;

/**
 * Implementation of the FIPA Request When interaction protocol.
 * Reference : http://www.fipa.org/specs/fipa00028/SC00028H.html
 */

import static msi.gaml.extensions.fipa.FIPAConstants.*;
import static msi.gaml.extensions.fipa.FIPAConstants.Performatives.*;

/**
 * The Class FIPARequestWhen.
 */
public class FIPARequestWhen extends FIPAProtocol {

	/** Definition of protocol model. */
	private static Object[] __after_cancel = { FAILURE,
			Integer.valueOf(CONVERSATION_END), PARTICIPANT, null, INFORM,
			Integer.valueOf(CONVERSATION_END), PARTICIPANT, null };

	/** The __after_agree. */
	private static Object[] __after_agree = { FAILURE,
			Integer.valueOf(CONVERSATION_END), PARTICIPANT, null, INFORM,
			Integer.valueOf(CONVERSATION_END), PARTICIPANT, null };

	/** The __after_request. */
	private static Object[] __after_request = { CANCEL,
			Integer.valueOf(AGENT_ACTION_REQ), INITIATOR, __after_cancel, REFUSE,
			Integer.valueOf(CONVERSATION_END), PARTICIPANT, null, AGREE,
			Integer.valueOf(NO_AGENT_ACTION_REQ), PARTICIPANT, __after_agree };

	/** The roots. */
	public static Object[] roots = { REQUEST_WHEN,
			Integer.valueOf(AGENT_ACTION_REQ), INITIATOR, __after_request };

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.misc.current_development.FIPAProtocol#getName()
	 */
	@Override
	public int getIndex() {
		return FIPAConstants.Protocols.FIPA_REQUEST_WHEN;
	}

	@Override
	public String getName() {
		return FIPAConstants.Protocols.FIPA_REQUEST_WHEN_STR;
	}
}
