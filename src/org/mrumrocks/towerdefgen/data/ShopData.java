package org.mrumrocks.towerdefgen.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mrumrocks.towerdefgen.core.Aspect;
import org.mrumrocks.towerdefgen.core.Data;
import org.mrumrocks.towerdefgen.core.GameData;
import org.mrumrocks.towerdefgen.core.Invalid;
import org.mrumrocks.towerdefgen.core.NonNullableFileReference;

public class ShopData implements Data {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NonNullableFileReference("shop background image")
	public File shopBackground;

	@NonNullableFileReference("shop button image (up state)")
	public File shopButtonImage;

	@NonNullableFileReference("shop button image (hover state)")
	public File shopButtonImageHover;

	@NonNullableFileReference("shop button image (pressed state)")
	public File shopButtonImagePressed;

	@NonEmptyStringReference("upgrade text")
	public String doUpgrade;

	@NonEmptyStringReference("not enough money text")
	public String notEnoughMoney;

	@NonNullableFileReference("purchase sound")
	public File purchaseSound;

	@NonNullableFileReference("invalid purchase sound")
	public File invalidPurchaseSound;

	@Override
	public List<Invalid> validate(GameData data) {
		List<Invalid> errors = new ArrayList<>();
		Invalid.forBadFiles(errors, Aspect.SHOP, this);
		Invalid.forEmptyStrings(errors, Aspect.SHOP, this);
		return errors;
	}

}
