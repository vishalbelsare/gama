/*********************************************************************************************
 * 
 *
 * 'MoleSimulationLoader.java', in plugin 'gama.core.headless', is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package gama.core.headless.openmole;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gama.core.headless.core.GamaHeadlessException;
import gama.core.headless.core.GamlProperties;
import gama.core.headless.core.HeadlessSimulationLoader;
import gama.common.interfaces.IModel;
import gaml.compilation.GamlCompilationError;

public abstract class MoleSimulationLoader {

	public static void loadGAMA() {
		// Now done in the loader
	}

	/**
	 * 
	 * @param modelPath
	 * @return a compiled model
	 * @throws IOException
	 * @throws GamaHeadlessException
	 * @deprecated use loadModel(File, List<GamlCompilationError>) instead
	 */
	@Deprecated
	public static IModel loadModel(final File modelPath) throws IOException, GamaHeadlessException {
		return loadModel(modelPath, null);
	}

	public static IModel loadModel(final File modelPath, final List<GamlCompilationError> errors)
			throws IOException, GamaHeadlessException {
		return loadModel(modelPath, errors, null);
	}

	public static IModel loadModel(final File modelPath, final List<GamlCompilationError> errors,
			final GamlProperties metadata) throws IOException, GamaHeadlessException {
		HeadlessSimulationLoader.preloadGAMA();
		return HeadlessSimulationLoader.loadModel(modelPath, errors, metadata);
	}

	public static IMoleExperiment newExperiment(final IModel model) {
		return new MoleExperiment(model);
	}

}