package net.sourceforge.mpango.terrains;

import net.sourceforge.mpango.entity.Construction;

public class MountainTerrain implements Terrain {

	public boolean canBuildConstruction(Construction construction) {
		return false;
	}

	public boolean canEvolveTo(Terrain terrain) {
		boolean canEvolve = false;
		if (terrain instanceof FieldTerrain) {
			canEvolve = true;
		}
		return canEvolve;
	}

	public boolean canUpgradeTo(Upgrade upgrade) {
		// TODO Auto-generated method stub
		return false;
	}

}
