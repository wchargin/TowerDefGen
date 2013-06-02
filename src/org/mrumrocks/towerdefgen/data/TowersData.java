package org.mrumrocks.towerdefgen.data;

import java.io.File;
import static org.mrumrocks.towerdefgen.core.Invalid.emptyIfNull;
import java.util.ArrayList;
import java.util.List;

import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.MultiData;
import org.mrumrocks.towerdefgen.core.Invalid;
import org.mrumrocks.towerdefgen.core.Named;
import org.mrumrocks.towerdefgen.core.NonNullableFileReference;

public class TowersData extends MultiData<TowersData.SingleTowerData> {

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = super.validate(data);
		Invalid.forEmptyAspect(errors, Aspect.TOWERS, towers, "towers");
		Invalid.forDuplicates(errors, Aspect.TOWERS, towers, "towers");
		return errors;
	}

	public static class TowerLevelData implements Data {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@NonNullableFileReference("tower image")
		public File image;
		public int cost; // to upgrade or purchase
		public int fireDelay;
		public int launchSpeed;
		public String projectileType;
		public int range;

		@Override
		public List<Invalid> validate(GameData data) {
			List<Invalid> errors = new ArrayList<>();

			String convertedProjectileType = GameData
					.convertToJavaIdentifier(projectileType);

			boolean hasProjectileType = false;
			for (ProjectilesData.SingleProjectileData spd : data.projectiles.projectiles) {
				if (GameData.convertToJavaIdentifier(spd.name).equals(
						convertedProjectileType)) {
					hasProjectileType = true;
					break;
				}
			}

			if (!hasProjectileType) {
				errors.add(new Invalid(Aspect.TOWERS, "Projectile \""
						+ emptyIfNull(projectileType) + "\" does not exist."));
			}

			Invalid.forBadFiles(errors, Aspect.TOWERS, this);

			return errors;
		}
	}

	public static class SingleTowerData extends MultiData<TowerLevelData>
			implements Named {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public String name;

		public String getName() {
			return name;
		}

		public final List<TowerLevelData> levels = new ArrayList<>();

		@Override
		public List<TowerLevelData> getSubs() {
			return levels;
		}

		@Override
		public List<Invalid> validate(GameData data) {
			List<Invalid> errors = super.validate(data);
			Invalid.forInvalidName(errors, Aspect.TOWERS, name, "tower");
			Invalid.forNoSubs(errors, Aspect.TOWERS, levels, name, "Tower",
					"levels");

			return errors;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final List<TowersData.SingleTowerData> towers = new ArrayList<>();

	@Override
	public List<TowersData.SingleTowerData> getSubs() {
		return towers;
	}

}