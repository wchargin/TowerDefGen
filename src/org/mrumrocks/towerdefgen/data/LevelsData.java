package org.mrumrocks.towerdefgen.data;

import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.MultiData;
import org.mrumrocks.towerdefgen.core.Named;
import org.mrumrocks.towerdefgen.core.NonNullableFileReference;
import org.mrumrocks.towerdefgen.core.Invalid;

public class LevelsData extends MultiData<LevelsData.SingleLevelData> {

	public static class SingleLevelData implements Data, Named {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public String name;

		public String getName() {
			return name;
		}

		@NonNullableFileReference("level background image")
		public File backgroundImage;

		@NonNullableFileReference("level icon image")
		public File iconImage;
		public Polygon waypoints;
		public int pathWidth;

		@Override
		public List<Invalid> validate(GameData data) {
			List<Invalid> errors = new ArrayList<>();
			Invalid.forInvalidName(errors, Aspect.LEVELS, name, "level");
			if (waypoints == null) {
				errors.add(new Invalid(Aspect.LEVELS,
						"The enemy path on level \""
								+ (name == null ? "" : name)
								+ "\" has not been defined."));
			} else if (waypoints.npoints < 2) {
				errors.add(new Invalid(Aspect.LEVELS,
						"The enemy path on level \"" + name
								+ "\" has fewer than two points."));
			}
			Invalid.forBadFiles(errors, Aspect.LEVELS, this, name);
			return errors;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final List<LevelsData.SingleLevelData> levels = new ArrayList<>();

	@Override
	public List<LevelsData.SingleLevelData> getSubs() {
		return levels;
	}

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = super.validate(data);
		Invalid.forEmptyAspect(errors, Aspect.LEVELS, levels, "levels");
		Invalid.forDuplicates(errors, Aspect.LEVELS, levels, "levels");
		return errors;
	}

}