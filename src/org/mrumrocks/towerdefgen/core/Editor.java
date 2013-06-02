package org.mrumrocks.towerdefgen.core;


public interface Editor<T extends Data> {

	/**
	 * Gets the object being edited.
	 * 
	 * @return the object
	 */
	public T getContents();

	/**
	 * Sets the contents of this editor to the given contents.
	 * 
	 * @param contents
	 *            the new contents
	 */
	public void setContents(T contents);

}
