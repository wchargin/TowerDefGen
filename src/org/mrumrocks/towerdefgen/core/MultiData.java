package org.mrumrocks.towerdefgen.core;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiData<T extends Data> implements Data {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = new ArrayList<>();
		for (T t : getSubs()) {
			errors.addAll(t.validate(data));
		}
		return errors;
	}

	/**
	 * Gets the sub-data of this datum.
	 * 
	 * @return the sub-data
	 */
	public abstract List<T> getSubs();

}
