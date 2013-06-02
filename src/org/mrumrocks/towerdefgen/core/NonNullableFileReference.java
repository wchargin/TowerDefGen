package org.mrumrocks.towerdefgen.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Denotes a field representing a file reference that is invalid as null and has
 * the given name. Can be used with
 * {@link GameData#forBadFiles(List,Aspect, Object)} .
 * 
 * @author William Chargin
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNullableFileReference {

	public String value();

}
