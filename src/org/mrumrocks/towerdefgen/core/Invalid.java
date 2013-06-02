package org.mrumrocks.towerdefgen.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mrumrocks.towerdefgen.data.NonEmptyStringReference;

public class Invalid {

	public final Aspect aspect;

	public final String message;

	public Invalid(Aspect aspect, String message) {
		super();
		this.aspect = aspect;
		this.message = message;
	}

	/**
	 * @return <code>new ValidationError(aspect, "\"" + name + "\" is not a valid "
				+ type + " name.");</code>
	 */
	public static Invalid createInvalidNameError(Aspect aspect, String name,
			String type) {
		return new Invalid(aspect, "\"" + name + "\" is not a valid " + type
				+ " name.");
	}

	public static String emptyIfNull(String test) {
		return test == null ? "" : test;
	}

	public static void forBadFiles(List<Invalid> errors, Aspect aspect,
			Object o, String suffix) {
		for (Field f : o.getClass().getFields()) {
			NonNullableFileReference name = f
					.getAnnotation(NonNullableFileReference.class);
			try {
				if (name != null) {
					Object value = f.get(o);
					if (value == null) {
						errors.add(new Invalid(aspect,
								"You have not selected a file for the "
										+ name.value()
										+ (suffix == null ? "." : (" for \""
												+ suffix + ".\""))));
					} else if (value instanceof File) {
						File file = (File) value;
						if (!file.exists()) {
							errors.add(new Invalid(aspect, "The file "
									+ file.getAbsolutePath()
									+ " no longer exists."));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void forBadFiles(List<Invalid> errors, Aspect aspect, Object o) {
		forBadFiles(errors, aspect, o, null);
	}

	public static void forInvalidName(List<Invalid> errors, Aspect aspect,
			String name, String type) {
		if (GameData.convertToJavaIdentifier(name).isEmpty()) {
			errors.add(createInvalidNameError(aspect, name == null ? "" : name,
					type));
		}
	}

	public static void forNoSubs(Collection<Invalid> errors, Aspect aspect,
			Collection<?> subs, String name, String type, String subtype) {
		if (subs.isEmpty()) {
			errors.add(new Invalid(aspect, String.format(
					"%s \"%s\" has no %s; it must have at least one.", type,
					Invalid.emptyIfNull(name), subtype)));
		}
	}

	public static void forEmptyAspect(Collection<Invalid> errors,
			Aspect aspect, Collection<?> items, String name) {
		if (items.isEmpty()) {
			errors.add(new Invalid(aspect, String.format(
					"There are no %s defined; there must be at least one.",
					name)));
		}
	}

	public static void forDuplicates(Collection<Invalid> errors, Aspect aspect,
			Collection<? extends Named> items, String name) {
		List<String> names = new ArrayList<String>(items.size());
		boolean hasDuplicates = false;
		for (Named n : items) {
			String thisName = GameData.convertToJavaIdentifier(n.getName());
			if (names.contains(thisName)) {
				hasDuplicates = true;
				break;
			} else {
				names.add(thisName);
			}
		}
		if (hasDuplicates) {
			errors.add(new Invalid(aspect, "There exist identically named "
					+ name + "."));
		}
	}

	public static void forEmptyStrings(List<Invalid> errors, Aspect aspect,
			Object o, String suffix) {
		for (Field f : o.getClass().getFields()) {
			NonEmptyStringReference name = f
					.getAnnotation(NonEmptyStringReference.class);
			try {
				if (name != null) {
					Object value = f.get(o);
					if (value == null
							|| (value instanceof String && ((String) value)
									.trim().isEmpty())) {
						errors.add(new Invalid(aspect,
								"You have not entered text for the "
										+ name.value()
										+ (suffix == null ? "." : (" for \""
												+ suffix + ".\""))));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void forEmptyStrings(List<Invalid> errors, Aspect aspect,
			Object o) {
		forEmptyStrings(errors, aspect, o, null);
	}
}
