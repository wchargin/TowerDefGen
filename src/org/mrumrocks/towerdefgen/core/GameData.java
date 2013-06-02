package org.mrumrocks.towerdefgen.core;

import java.io.Serializable;

import org.mrumrocks.towerdefgen.data.EnemiesData;
import org.mrumrocks.towerdefgen.data.GeneralData;
import org.mrumrocks.towerdefgen.data.LevelsData;
import org.mrumrocks.towerdefgen.data.ProjectilesData;
import org.mrumrocks.towerdefgen.data.ShopData;
import org.mrumrocks.towerdefgen.data.TowersData;

public class GameData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GeneralData general;
	public ShopData shop;
	public LevelsData levels;
	public TowersData towers;
	public EnemiesData enemies;
	public ProjectilesData projectiles;

	public static String convertToJavaIdentifier(String string) {
		if (string == null || string.isEmpty()) {
			return new String();
		}
		char[] ca = string.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char c : ca) {
			if (sb.length() == 0) {
				if (Character.isJavaIdentifierStart(c)) {
					sb.append(c);
				}
			} else {
				if (Character.isJavaIdentifierPart(c)) {
					sb.append(c);
				}
			}
		}
		return (sb.length() == 0 ? "" : ("$" + sb.toString()));
	}
}
