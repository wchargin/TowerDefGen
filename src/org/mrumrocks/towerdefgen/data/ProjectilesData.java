package org.mrumrocks.towerdefgen.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.MultiData;
import org.mrumrocks.towerdefgen.core.Invalid;
import org.mrumrocks.towerdefgen.core.Named;
import org.mrumrocks.towerdefgen.core.NonNullableFileReference;

public class ProjectilesData extends
		MultiData<ProjectilesData.SingleProjectileData> {

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = super.validate(data);
		Invalid.forEmptyAspect(errors, Aspect.PROJECTILES, projectiles,
				"projectiles");
		Invalid.forDuplicates(errors, Aspect.PROJECTILES, projectiles,
				"projectiles");
		return errors;
	}

	public static class SingleProjectileData implements Data, Named {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public String name;

		public String getName() {
			return name;
		}

		@NonNullableFileReference("projectile image")
		public File image;
		
		public int health;

		@Override
		public List<Invalid> validate(GameData data) {
			List<Invalid> errors = new ArrayList<>();
			Invalid.forInvalidName(errors, Aspect.PROJECTILES, name,
					"projectile");
			Invalid.forBadFiles(errors, Aspect.PROJECTILES, this, name);
			return errors;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List<ProjectilesData.SingleProjectileData> projectiles = new ArrayList<>();

	@Override
	public List<ProjectilesData.SingleProjectileData> getSubs() {
		return projectiles;
	}

}