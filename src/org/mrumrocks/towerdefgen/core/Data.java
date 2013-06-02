package org.mrumrocks.towerdefgen.core;

import java.io.Serializable;
import java.util.List;

public interface Data extends Serializable {

	/**
	 * Validates this editor with the given context, and returns a list of any
	 * errors that may occur.
	 * 
	 * @param data
	 *            the data to use for validation, in conjunction with this
	 *            object
	 * @return the list of validation errors
	 */
	public List<Invalid> validate(GameData data);

}
