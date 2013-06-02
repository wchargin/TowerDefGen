package org.mrumrocks.towerdefgen.editors;

import java.awt.BorderLayout;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mrumrocks.towerdefgen.data.GeneralData;

import tools.customizable.AbstractProperty;
import tools.customizable.FileProperty;
import tools.customizable.PropertyPanel;
import tools.customizable.PropertySet;
import tools.customizable.TextProperty;

public class GeneralConfigurationPanel extends EditorPanel<GeneralData> {
	/**
	 * The runnable invoked to update the menu after a change.
	 */
	private final Runnable rUpdate;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GeneralConfigurationPanel() {
		super();
		setLayout(new BorderLayout());

		contents = new GeneralData();

		PropertySet ps = new PropertySet();

		final TextProperty tpName = new TextProperty("Game name", null);
		final FileProperty fpMenuBackgroundImage = new FileProperty(
				"Menu background image (800 \u00d7 600)", null);
		final FileProperty fpBackgroundMusic = new FileProperty(
				"Background music", null);
		final FileProperty fpTitleImage = new FileProperty(
				"Title image (800 \u00d7 100)", null);
		final FileProperty fpHelpImage = new FileProperty(
				"Help image (800 \u00d7 600)", null);
		final FileProperty fpBaseImage = new FileProperty(
				"Home base (96 \u00d7 96)", null);
		final FileProperty fpButtonImage = new FileProperty(
				"Button up image (48 \u00d7 48, divided in thirds)", null);
		final FileProperty fpButtonHover = new FileProperty(
				"Button hover image", null);
		final FileProperty fpButtonPressed = new FileProperty(
				"Button pressed image", null);
		final FileProperty fpButtonClick = new FileProperty(
				"Button click sound", null);
		final FileProperty fpButtonTweenInSound = new FileProperty(
				"Button fly-in sound", null);

		ps.add(tpName);
		ps.add(fpTitleImage);
		ps.add(fpBackgroundMusic);
		ps.add(null);
		ps.add(fpMenuBackgroundImage);
		ps.add(fpHelpImage);
		ps.add(null);
		ps.add(fpBaseImage);
		ps.add(null);
		ps.add(fpButtonImage);
		ps.add(fpButtonHover);
		ps.add(fpButtonPressed);
		ps.add(null);
		ps.add(fpButtonClick);
		ps.add(fpButtonTweenInSound);

		FileProperty[] images = { fpButtonImage, fpButtonHover, fpBaseImage,
				fpButtonPressed, fpTitleImage, fpHelpImage,
				fpMenuBackgroundImage };
		for (FileProperty imageProperty : images) {
			imageProperty.setFilter(ffImage);
		}
		fpBackgroundMusic.setFilter(ffSoundOgg);
		fpButtonClick.setFilter(ffSoundWav);
		fpButtonTweenInSound.setFilter(ffSoundWav);

		tpName.setDescription("Appears in the window title.");
		fpMenuBackgroundImage
				.setDescription("The background image for the main menu.");
		fpBackgroundMusic
				.setDescription("Will start when the title first enters and loop until quit.");
		fpButtonImage
				.setDescription("This will be the image for all buttons. It should be optimized for nine-slice scaling.");
		fpButtonHover
				.setDescription("This image will be displayed when the user's mouse pointer is over a button.");
		fpBaseImage
				.setDescription("This is the base that the enemies are approaching and the player must defend.");
		fpButtonPressed
				.setDescription("This image will be displayed when the user is pressing a button.");
		fpButtonClick
				.setDescription("The sound made when a button is clicked.");
		fpTitleImage
				.setDescription("The image for the title text only (not background, etc.).");
		fpButtonTweenInSound
				.setDescription("The sound that plays when each main menu button enters the screen.");
		fpHelpImage
				.setDescription("The image for the help pane. This should contain the background and all text.");

		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				if (!isUpdating()) {
					contents.name = tpName.getValue();
					contents.menuBackgroundImage = fpMenuBackgroundImage
							.getValue();
					contents.backgroundMusic = fpBackgroundMusic.getValue();
					contents.base = fpBaseImage.getValue();
					contents.buttonImage = fpButtonImage.getValue();
					contents.buttonImageHover = fpButtonHover.getValue();
					contents.buttonImagePressed = fpButtonPressed.getValue();
					contents.buttonClickSound = fpButtonClick.getValue();
					contents.titleImage = fpTitleImage.getValue();
					contents.buttonTweenInSound = fpButtonTweenInSound
							.getValue();
					contents.helpImage = fpHelpImage.getValue();
				}
			}
		};

		for (AbstractProperty<?> p : ps) {
			if (p != null) {
				p.addChangeListener(cl);
			}
		}

		add(new PropertyPanel(ps, true, false), BorderLayout.CENTER);

		rUpdate = new Runnable() {
			@Override
			public void run() {
				tpName.setValue(contents.name);
				fpBaseImage.setValue(contents.base);
				fpMenuBackgroundImage.setValue(contents.menuBackgroundImage);
				fpBackgroundMusic.setValue(contents.backgroundMusic);
				fpButtonImage.setValue(contents.buttonImage);
				fpButtonHover.setValue(contents.buttonImageHover);
				fpButtonPressed.setValue(contents.buttonImagePressed);
				fpButtonClick.setValue(contents.buttonClickSound);
				fpTitleImage.setValue(contents.titleImage);
				fpButtonTweenInSound.setValue(contents.buttonTweenInSound);
				fpHelpImage.setValue(contents.helpImage);
			}
		};
	}

	@Override
	protected void updatePanel() {
		rUpdate.run();
	}

}
