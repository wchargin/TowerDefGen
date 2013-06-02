package org.mrumrocks.towerdefgen.editors;

import java.awt.BorderLayout;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mrumrocks.towerdefgen.data.ShopData;

import tools.customizable.AbstractProperty;
import tools.customizable.FileProperty;
import tools.customizable.PropertyPanel;
import tools.customizable.PropertySet;
import tools.customizable.TextProperty;

public class ShopConfigurationPanel extends EditorPanel<ShopData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Runnable rUpdate;

	public ShopConfigurationPanel() {
		super();
		setLayout(new BorderLayout());
		contents = new ShopData();

		PropertySet ps = new PropertySet();

		final FileProperty fpShopBackground = new FileProperty(
				"Shop background image (160 \u00d7 120)", null);
		final FileProperty fpShopButtonImage = new FileProperty(
				"Shop button up image (64 \u00d7 64)", null);
		final FileProperty fpShopButtonHover = new FileProperty(
				"Shop button hover image", null);
		final FileProperty fpShopButtonPressed = new FileProperty(
				"Button pressed image", null);

		final TextProperty tpUpgrade = new TextProperty("Upgrade text", null);
		final TextProperty tpNotEnoughMoney = new TextProperty(
				"Not enough money text", null);

		final FileProperty fpPurchaseSound = new FileProperty("Purchase sound",
				null);
		final FileProperty fpInvalidPurchaseSound = new FileProperty(
				"Invalid purchase sound", null);

		ps.add(fpShopBackground);
		ps.add(null);
		ps.add(fpShopButtonImage);
		ps.add(fpShopButtonHover);
		ps.add(fpShopButtonPressed);
		ps.add(null);
		ps.add(tpUpgrade);
		ps.add(tpNotEnoughMoney);
		ps.add(null);
		ps.add(fpPurchaseSound);
		ps.add(fpInvalidPurchaseSound);

		FileProperty[] images = { fpShopBackground, fpShopButtonImage,
				fpShopButtonHover, fpShopButtonPressed };
		for (FileProperty imageProperty : images) {
			imageProperty.setFilter(ffImage);
		}
		fpPurchaseSound.setFilter(ffSoundWav);
		fpInvalidPurchaseSound.setFilter(ffSoundWav);

		fpShopBackground.setDescription("The background image for the shop.");

		fpShopButtonImage
				.setDescription("This is the button used for the background of shop items. It need not be optimized for nine-slice scaling.");
		fpShopButtonHover
				.setDescription("This image will be displayed when the user's mouse pointer is over a shop button.");
		fpShopButtonPressed
				.setDescription("This image will be displayed when the user is pressing a shop button.");
		tpUpgrade
				.setDescription("The text to display on the \"purchase upgrade\" button.");
		tpNotEnoughMoney
				.setDescription("The text to display on the \"purchase upgrade\" button when the user cannot afford the upgrade.");
		fpPurchaseSound
				.setDescription("The sound played on a purchase or upgrade.");
		fpInvalidPurchaseSound
				.setDescription("The sound played when the user cannot afford a purchase.");

		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				if (!isUpdating()) {
					contents.shopBackground = fpShopBackground.getValue();
					contents.shopButtonImage = fpShopButtonImage.getValue();
					contents.shopButtonImageHover = fpShopButtonHover
							.getValue();
					contents.shopButtonImagePressed = fpShopButtonPressed
							.getValue();
					contents.doUpgrade = tpUpgrade.getValue();
					contents.notEnoughMoney = tpNotEnoughMoney.getValue();
					contents.purchaseSound = fpPurchaseSound.getValue();
					contents.invalidPurchaseSound = fpInvalidPurchaseSound
							.getValue();
				}
			}
		};
		for (AbstractProperty<?> p : ps) {
			if (p != null) {
				p.addChangeListener(cl);
			}
		}

		rUpdate = new Runnable() {
			@Override
			public void run() {
				fpShopBackground.setValue(contents.shopBackground);
				fpShopButtonImage.setValue(contents.shopButtonImage);
				fpShopButtonHover.setValue(contents.shopButtonImageHover);
				fpShopButtonPressed.setValue(contents.shopButtonImagePressed);
				tpUpgrade.setValue(contents.doUpgrade);
				tpNotEnoughMoney.setValue(contents.notEnoughMoney);
				fpPurchaseSound.setValue(contents.purchaseSound);
				fpInvalidPurchaseSound.setValue(contents.invalidPurchaseSound);
			}
		};

		add(new PropertyPanel(ps, true, false), BorderLayout.CENTER);
	}

	@Override
	protected void updatePanel() {
		rUpdate.run();
	}

}
