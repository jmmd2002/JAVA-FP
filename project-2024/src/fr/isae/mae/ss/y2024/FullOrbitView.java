package fr.isae.mae.ss.y2024;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;


public class FullOrbitView extends BasicOrbitView {
	@Override
	protected double computeFarDistance(Position eyePosition) {
		double defaultFarDistance = super.computeFarDistance(eyePosition);
		return defaultFarDistance*2;
	}
}