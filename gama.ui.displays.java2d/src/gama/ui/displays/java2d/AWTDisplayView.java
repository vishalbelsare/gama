/*********************************************************************************************
 *
 * 'AWTDisplayView.java, in plugin gama.ui.displays.java2d, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package gama.ui.displays.java2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gama.ui.base.views.WorkaroundForIssue1353;
import gama.ui.displays.java2d.swing.SwingControl;
import gama.ui.experiment.views.displays.LayeredDisplayView;

public class AWTDisplayView extends LayeredDisplayView {

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {

		if (getOutput() == null) { return null; }

		surfaceComposite = new SwingControl(parent, SWT.NO_FOCUS) {

			@Override
			protected Java2DDisplaySurface createSwingComponent() {
				return (Java2DDisplaySurface) getDisplaySurface();
			}

		};
		surfaceComposite.setEnabled(false);
		WorkaroundForIssue1594.installOn(this, parent, surfaceComposite, (Java2DDisplaySurface) getDisplaySurface());
		WorkaroundForIssue2745.installOn(this);
		WorkaroundForIssue1353.install();
		return surfaceComposite;
	}

}