/*******************************************************************************************************
 *
 * msi.gama.kernel.batch.StoppingCriterionMaxIt.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.kernel.batch;

import java.util.Map;

public class StoppingCriterionMaxIt implements StoppingCriterion {

	private final int maxIt;

	public StoppingCriterionMaxIt(final int maxIt) {
		super();
		this.maxIt = maxIt;
	}

	@Override
	@SuppressWarnings("boxing")
	public boolean stopSearchProcess(final Map<String, Object> parameters) {
		return (Integer) parameters.get("Iteration") > maxIt;
	}

}
