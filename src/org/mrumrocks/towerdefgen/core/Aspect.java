package org.mrumrocks.towerdefgen.core;

public enum Aspect {
	GENERAL("General"), SHOP("Shop"), LEVELS("Levels"), TOWERS("Towers"), ENEMIES(
			"Enemies"), PROJECTILES("Projectiles");
	public final String name;

	private Aspect(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}