package org.mrumrocks.towerdefgen.core;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.mrumrocks.towerdefgen.data.EnemiesData;
import org.mrumrocks.towerdefgen.data.EnemiesData.SingleEnemyData;
import org.mrumrocks.towerdefgen.data.GeneralData;
import org.mrumrocks.towerdefgen.data.LevelsData;
import org.mrumrocks.towerdefgen.data.LevelsData.SingleLevelData;
import org.mrumrocks.towerdefgen.data.ProjectilesData;
import org.mrumrocks.towerdefgen.data.ProjectilesData.SingleProjectileData;
import org.mrumrocks.towerdefgen.data.ShopData;
import org.mrumrocks.towerdefgen.data.TowersData;
import org.mrumrocks.towerdefgen.data.TowersData.SingleTowerData;
import org.mrumrocks.towerdefgen.data.TowersData.TowerLevelData;

public class CodeGenerator {

	private static class Switch extends HashMap<Integer, String> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public final String switchOn;
		public final boolean shouldBreak;
		public String tabs = "\t\t";

		public Switch(String switchOn, boolean shouldBreak) {
			super();
			this.switchOn = switchOn;
			this.shouldBreak = shouldBreak;
		}

		public String generate(String def) {
			StringBuilder sb = new StringBuilder();
			sb.append(tabs);
			sb.append("switch (");
			sb.append(switchOn);
			sb.append(") {\n");
			for (Map.Entry<Integer, String> entry : entrySet()) {
				sb.append(tabs);
				sb.append("case ");
				sb.append(entry.getKey());
				sb.append(":\n");
				sb.append(tabs);
				sb.append('\t');
				sb.append(entry.getValue());
				sb.append('\n');
				if (shouldBreak) {
					sb.append(tabs);
					sb.append('\t');
					sb.append("break;");
					sb.append('\n');
				}
			}
			sb.append(tabs);
			sb.append("default:");
			sb.append('\n');
			sb.append(tabs);
			sb.append('\t');
			sb.append(def);
			sb.append('\n');
			sb.append(tabs);
			sb.append("}");
			return sb.toString();
		}
	}

	private static class Token {
		public static String apply(CharSequence file, Token... tokens) {
			String text = file.toString();
			for (Token t : tokens) {
				text = text.replace(t.key, t.replacement);
			}
			return text;
		}

		public static Token esc(String key, String replacement) {
			return new Token(key, escape(replacement));
		}

		public static Token id(String key, String replacement) {
			return new Token(key, GameData.convertToJavaIdentifier(replacement));
		}

		public static Token raw(String key, Object replacement) {
			return new Token(key, replacement.toString());
		}

		public final String key;

		public final String replacement;

		private Token(String key, String replacement) {
			super();
			this.key = "$$$" + key + "$$$";
			this.replacement = replacement;
		}
	}

	private static void copyRsc(File base, String subpackage, File... src) {
		for (File f : src) {
			String fullPath = base.getAbsolutePath()
					+ (".rsc.org.mrumrocks.td.rsc."
							+ (subpackage == null ? "" : subpackage) + ".")
							.replace('.', File.separatorChar)
					+ (escape(f.getName()));
			File file = new File(fullPath);
			file.getParentFile().mkdirs();
			try {
				Files.copy(f.toPath(), new FileOutputStream(file));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String escape(String input) {
		// escape quotes
		input = input.replace("\"", "\\\"");

		return input;
	}

	private static void generateEnemies(EnemiesData data, File tempDir) {
		StringBuilder sb = getTemplate("enemies/EnemyTemplate.txt");
		for (EnemiesData.SingleEnemyData enemy : data.enemies) {
			Token name = Token.id("NAME", enemy.name);
			Token image = Token.esc("IMAGE", enemy.image.getName());
			Token health = Token.raw("HEALTH", enemy.health);
			Token money = Token.raw("MONEY", enemy.money);
			Token slow = Token.raw("SLOWNESS", enemy.slowness);

			writeSource(Token.apply(sb, name, image, health, slow, money),
					tempDir, "enemies",
					GameData.convertToJavaIdentifier(enemy.name));

			copyRsc(tempDir, "images.enemies", enemy.image);
		}

	}

	private static void generateGeneralAndShop(GameData data, File tempDir) {
		final GeneralData general = data.general;
		final ShopData shop = data.shop;

		{ // Base
			StringBuilder sb = getTemplate("Base.txt");
			Token image = Token.esc("BASE_IMAGE", general.base.getName());
			writeSource(Token.apply(sb, image), tempDir, null, "Base");
			copyRsc(tempDir, "images", general.base);
		}

		{ // Button
			StringBuilder sb = getTemplate("Button.txt");
			Token image = Token
					.esc("BUTTON_OUT", general.buttonImage.getName());
			Token hover = Token.esc("BUTTON_HOVER",
					general.buttonImageHover.getName());
			Token press = Token.esc("BUTTON_PRESSED",
					general.buttonImagePressed.getName());
			Token click = Token.esc("BUTTON_CLICK",
					general.buttonClickSound.getName());

			writeSource(Token.apply(sb, image, hover, press, click), tempDir,
					null, "Button");
			copyRsc(tempDir, "images", general.buttonImage,
					general.buttonImageHover, general.buttonImagePressed);
			copyRsc(tempDir, "sounds", general.buttonClickSound);
		}

		{ // GameView
			StringBuilder sb = getTemplate("GameView.txt");

			Token purchase = Token.esc("PURCHASE_SOUND",
					shop.purchaseSound.getName());
			Token invalid = Token.esc("INVALID_PURCHASE_SOUND",
					shop.invalidPurchaseSound.getName());
			Token count = Token.raw("ENEMY_COUNT", data.enemies.enemies.size());

			StringBuilder shopButtons = new StringBuilder();
			for (SingleTowerData tower : data.towers.towers) {
				TowerLevelData base = tower.levels.get(0);
				shopButtons.append("\t\tsp.addButton(new ShopButton(\n");
				shopButtons
						.append("\t\t\tImageCache.forClass(TowerDef.class).get(\"towers/\" + \"");
				shopButtons.append(base.image.getName());
				shopButtons.append("\"),\n");
				shopButtons.append("\t\t\tnew TowerGenerator() {\n");
				shopButtons.append("\t\t\t\t@Override\n");
				shopButtons.append("\t\t\t\tpublic Tower generateTower() {\n");
				shopButtons
						.append("\t\t\t\t\treturn new org.mrumrocks.td.towers.");
				shopButtons
						.append(GameData.convertToJavaIdentifier(tower.name));
				shopButtons.append("();\n");
				shopButtons.append("\t\t\t\t}\n");
				shopButtons.append("\t\t\t}, ");
				shopButtons.append(base.cost);
				shopButtons.append("));\n\n");
			}
			Token buttons = Token.raw("SHOP_BUTTONS", shopButtons);

			Switch enemySwitch = new Switch("i", true);
			enemySwitch.tabs = "\t\t\t\t";
			for (int i = 0; i < data.enemies.enemies.size(); i++) {
				SingleEnemyData enemy = data.enemies.enemies.get(i);
				enemySwitch.put(i, String.format(
						"e = new org.mrumrocks.td.enemies.%s();",
						GameData.convertToJavaIdentifier(enemy.name)));
			}
			Token enemy = Token.raw("ENEMY_SWITCH",
					enemySwitch.generate("break;"));

			writeSource(
					Token.apply(sb, purchase, invalid, count, buttons, enemy),
					tempDir, null, "GameView");
			copyRsc(tempDir, "sounds", shop.purchaseSound,
					shop.invalidPurchaseSound);
		}

		{ // HelpView
			StringBuilder sb = getTemplate("HelpView.txt");
			Token image = Token.esc("HELP_IMAGE", general.helpImage.getName());
			writeSource(Token.apply(sb, image), tempDir, null, "HelpView");
			copyRsc(tempDir, "images", general.helpImage);
		}

		{ // MainMenuView
			StringBuilder sb = getTemplate("MainMenuView.txt");
			Token bg = Token.esc("BACKGROUND",
					general.menuBackgroundImage.getName());
			Token title = Token.esc("TITLE", general.titleImage.getName());
			Token music = Token.esc("BACKGROUND_MUSIC",
					general.backgroundMusic.getName());

			StringBuilder mapLevels = new StringBuilder();
			final int max = data.levels.levels.size();
			for (int i = 0; i < max; i++) {
				SingleLevelData level = data.levels.levels.get(i);
				mapLevels.append("\t\t\t\t\t\t\t\tnew org.mrumrocks.td.maps.");
				mapLevels.append(GameData.convertToJavaIdentifier(level.name));
				mapLevels.append("()");
				if (i + 1 < max) {
					mapLevels.append(",\n");
				}
			}
			Token levels = Token.raw("NEW_MAP_LEVELS", mapLevels);

			writeSource(Token.apply(sb, bg, title, music, levels), tempDir,
					null, "MainMenuView");
			copyRsc(tempDir, "images", general.titleImage,
					general.menuBackgroundImage);
			copyRsc(tempDir, "sounds", general.backgroundMusic);
		}

		{ // TowerDef
			StringBuilder sb = getTemplate("TowerDef.txt");
			Token windowTitle = Token.esc("TITLE", general.name);
			writeSource(Token.apply(sb, windowTitle), tempDir, null, "TowerDef");
		}

		{ // ShopButton
			StringBuilder sb = getTemplate("shop/ShopButton.txt");
			Token out = Token.esc("SHOP_BUTTON_OUT",
					shop.shopButtonImage.getName());
			Token hover = Token.esc("SHOP_BUTTON_HOVER",
					shop.shopButtonImageHover.getName());
			Token pressed = Token.esc("SHOP_BUTTON_PRESSED",
					shop.shopButtonImagePressed.getName());
			writeSource(Token.apply(sb, out, hover, pressed), tempDir, "shop",
					"ShopButton");
			copyRsc(tempDir, "images.shop", shop.shopButtonImage,
					shop.shopButtonImageHover, shop.shopButtonImagePressed);
		}

		{ // ShopPanel
			StringBuilder sb = getTemplate("shop/ShopPanel.txt");
			Token bg = Token.esc("SHOP_BACKGROUND",
					shop.shopBackground.getName());
			writeSource(Token.apply(sb, bg), tempDir, "shop", "ShopPanel");
			copyRsc(tempDir, "images.shop", shop.shopBackground);
		}

		{ // Miscellaneous resources
			copyRsc(tempDir, "sounds", general.buttonTweenInSound);
		}

	}

	public synchronized static void generateJar(GameData data, File output) {
		try {
			File tempDir = Files.createTempDirectory(null).toFile();
			String tempPath = tempDir.getAbsolutePath();
			System.out.println(tempPath);

			generateGeneralAndShop(data, tempDir);
			generateLevels(data.levels, tempDir);
			generateTowers(data.towers, tempDir);
			generateEnemies(data.enemies, tempDir);
			generateProjectiles(data.projectiles, tempDir);

			// Copy the rawcopy
			copyFromJar("/rawcopy.zip", tempDir);
			copyFromJar("/jgame-1.2.zip", tempDir);
			copyFromJar("/build.xml", tempDir);

			File buildFile = new File(tempPath + "/build.xml");
			Project p = new Project();
			p.setUserProperty("ant.file", buildFile.getAbsolutePath());
			p.setUserProperty("base", tempPath);
			p.setUserProperty("jar_output", output.getAbsolutePath());
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, buildFile);
			p.executeTarget(p.getDefaultTarget());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void copyFromJar(String string, File tempDir) {
		try {
			InputStream stream = CodeGenerator.class
					.getResourceAsStream(string);
			OutputStream resStreamOut;
			int readBytes;
			byte[] buffer = new byte[4096];
			resStreamOut = new FileOutputStream(new File(
					tempDir.getAbsoluteFile() + string));
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
			resStreamOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateProjectiles(ProjectilesData projectiles,
			File tempDir) {
		StringBuilder sb = getTemplate("projectiles/ProjectileTemplate.txt");

		for (SingleProjectileData projectile : projectiles.projectiles) {
			Token name = Token.id("NAME", projectile.name);
			Token image = Token.esc("IMAGE", projectile.image.getName());
			Token health = Token.raw("HEALTH", projectile.health);

			writeSource(Token.apply(sb, name, image, health), tempDir,
					"projectiles",
					GameData.convertToJavaIdentifier(projectile.name));
			copyRsc(tempDir, "images/projectiles", projectile.image);
		}
	}

	private static void generateLevels(LevelsData data, File tempDir) {
		StringBuilder sb = getTemplate("maps/MapTemplate.txt");

		for (SingleLevelData level : data.levels) {
			Token name = Token.id("NAME", level.name);
			Token image = Token.esc("IMAGE", level.backgroundImage.getName());
			Token icon = Token.esc("ICON", level.iconImage.getName());
			Token width = Token.raw("PATH_WIDTH", level.pathWidth);
			Token waypoints = Token.raw("POLYGON",
					waypointCodeFor(level.waypoints));

			writeSource(Token.apply(sb, name, image, icon, width, waypoints),
					tempDir, "maps",
					GameData.convertToJavaIdentifier(level.name));

			copyRsc(tempDir, "images.maps", level.backgroundImage,
					level.iconImage);
		}

	}

	private static void generateTowers(TowersData data, File tempDir) {
		StringBuilder sb = getTemplate("towers/TowerTemplate.txt");

		for (SingleTowerData tower : data.towers) {
			String code = sb.toString();

			{
				Token name = Token.id("NAME", tower.name);
				Token image = Token.esc("IMAGE",
						tower.levels.get(0).image.getName());
				Token level = Token.raw("MAX_LEVEL", tower.levels.size() - 1);
				code = Token.apply(code, name, image, level);
			}

			Switch upgradeCost = new Switch("level", false);
			Switch fireDelay = new Switch("getLevel()", false);
			Switch projectile = new Switch("getLevel()", true);
			Switch speed = new Switch("getLevel()", false);
			Switch range = new Switch("getLevel()", false);
			Switch imageSetter = new Switch("level", true);

			for (int i = 0; i < tower.levels.size(); i++) {
				TowerLevelData level = tower.levels.get(i);
				upgradeCost.put(i, String.format("return %s;", level.cost));
				fireDelay.put(i, String.format("return %s;", level.fireDelay));
				projectile
						.put(i,
								String.format(
										"projectile = new org.mrumrocks.td.projectiles.%s();",
										GameData.convertToJavaIdentifier(level.projectileType)));
				speed.put(i, String.format("return %s;", level.launchSpeed));
				range.put(i, String.format("return %s;", level.range));
				imageSetter
						.put(i,
								String.format(
										"setImage(ImageCache.forClass(TowerDef.class).get(\"towers/%s\"));",
										escape(level.image.getName())));

				copyRsc(tempDir, "images.towers", level.image);
			}

			String sUpgradeCost = upgradeCost.generate("return 0;");
			String sFireDelay = fireDelay.generate("return 24;");
			String sProjectile = projectile.generate("return null;");
			String sSpeed = speed.generate("return 10;");
			String sRange = range.generate("return Integer.MAX_VALUE;");
			String sImageSetter = imageSetter.generate("return;");

			Token tUpgradeCost = Token.raw("UPGRADE_COST_SWITCH", sUpgradeCost);
			Token tFireDelay = Token.raw("FIRING_DELAY_SWITCH", sFireDelay);
			Token tProjectile = Token.raw("PROJECTILE_SWITCH", sProjectile);
			Token tSpeed = Token.raw("PROJECTILE_SPEED_SWITCH", sSpeed);
			Token tRange = Token.raw("RANGE_SWITCH", sRange);
			Token tImageSetter = Token.raw("IMAGE_SETTER_SWITCH", sImageSetter);

			code = Token.apply(code, tUpgradeCost, tFireDelay, tProjectile,
					tSpeed, tRange, tImageSetter);

			writeSource(code, tempDir, "towers",
					GameData.convertToJavaIdentifier(tower.name));
		}
	}

	private static StringBuilder getTemplate(String path) {
		BufferedReader templateReader = new BufferedReader(
				new InputStreamReader(CodeGenerator.class
						.getResourceAsStream("/template/org/mrumrocks/td/"
								+ path)));

		StringBuilder sb = new StringBuilder();
		try {
			for (String line; (line = templateReader.readLine()) != null;) {
				sb.append(line);
				sb.append('\n');
			}
			return sb;
		} catch (IOException ie) {
			ie.printStackTrace();
			return null;
		}
	}

	private static StringBuilder waypointCodeFor(Polygon waypoints) {
		StringBuilder sb = new StringBuilder();

		final String TWO_TABS = "\t\t";

		sb.append(TWO_TABS);
		sb.append("int[] x = new int[] { ");
		for (int i = 0; i < waypoints.npoints; i++) {
			sb.append(waypoints.xpoints[i]);
			if (i + 1 < waypoints.npoints) {
				sb.append(", ");
			}
		}
		sb.append(" };");
		sb.append("\n");

		sb.append(TWO_TABS);
		sb.append("int[] y = new int[] { ");
		for (int i = 0; i < waypoints.npoints; i++) {
			sb.append(waypoints.ypoints[i]);
			if (i + 1 < waypoints.npoints) {
				sb.append(", ");
			}
		}
		sb.append(" };");
		sb.append("\n");

		sb.append(TWO_TABS);
		sb.append("Polygon p = new Polygon(x, y, " + waypoints.npoints + ");\n");

		sb.append(TWO_TABS);
		sb.append("return p;");
		return sb;
	}

	private static void writeSource(String text, File base, String subpackage,
			String name) {
		String fullPath = base.getAbsolutePath()
				+ (".src.org.mrumrocks.td."
						+ (subpackage == null ? "" : subpackage) + ".")
						.replace('.', File.separatorChar) + name + ".java";
		File file = new File(fullPath);
		file.getParentFile().mkdirs();
		try (PrintWriter pw = new PrintWriter(file)) {
			for (String line : text.split("\n")) {
				pw.println(line);
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
}
