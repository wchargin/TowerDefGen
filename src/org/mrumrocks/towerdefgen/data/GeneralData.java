package org.mrumrocks.towerdefgen.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.NonNullableFileReference;
import org.mrumrocks.towerdefgen.core.Invalid;

public class GeneralData implements Data {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NonEmptyStringReference("game name")
	public String name;

	@NonNullableFileReference("title image")
	public File titleImage;

	@NonNullableFileReference("background music")
	public File backgroundMusic;

	@NonNullableFileReference("background image")
	public File menuBackgroundImage;

	@NonNullableFileReference("help image")
	public File helpImage;

	@NonNullableFileReference("button click sound")
	public File buttonClickSound;

	@NonNullableFileReference("button fly-in sound")
	public File buttonTweenInSound;

	@NonNullableFileReference("base image")
	public File base;

	@NonNullableFileReference("button image (up state)")
	public File buttonImage;

	@NonNullableFileReference("button image (hover state)")
	public File buttonImageHover;

	@NonNullableFileReference("button image (pressed state)")
	public File buttonImagePressed;

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = new ArrayList<>();
		Invalid.forEmptyStrings(errors, Aspect.GENERAL, this);
		Invalid.forBadFiles(errors, Aspect.GENERAL, this);
		return errors;
	}

}