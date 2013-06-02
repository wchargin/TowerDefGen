package org.mrumrocks.towerdefgen.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.Invalid;
import org.mrumrocks.towerdefgen.core.MultiData;
import org.mrumrocks.towerdefgen.core.Named;
import org.mrumrocks.towerdefgen.core.NonNullableFileReference;

public class EnemiesData extends MultiData<EnemiesData.SingleEnemyData> {

	public static class SingleEnemyData implements Data, Named {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public String name;

		public String getName() {
			return name;
		}

		@NonNullableFileReference("image")
		public File image;

		public int slowness;
		public int health;
		public int money;

		@Override
		public List<Invalid> validate(GameData data) {
			List<Invalid> errors = new ArrayList<>();
			Invalid.forInvalidName(errors, Aspect.ENEMIES, name, "enemy");
			Invalid.forBadFiles(errors, Aspect.ENEMIES, this, name);
			return errors;
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final List<EnemiesData.SingleEnemyData> enemies = new ArrayList<>();

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = super.validate(data);
		Invalid.forEmptyAspect(errors, Aspect.ENEMIES, enemies, "enemies");
		Invalid.forDuplicates(errors, Aspect.ENEMIES, enemies, "enemies");
		return errors;
	}

	@Override
	public List<EnemiesData.SingleEnemyData> getSubs() {
		return enemies;
	}

}