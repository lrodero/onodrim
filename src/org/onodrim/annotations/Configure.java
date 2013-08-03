/*
 * Copyright 2012 Luis Rodero-Merino.
 * 
 * This file is part of Onodrim.
 * 
 * Onodrim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Onodrim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Onodrim.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.onodrim.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This annotation is used to set fields that will be automatically configured with
 * the {@link AnnotationProcessor#setConfiguration(Object, org.onodrim.Configuration)} method
 * (or {@link org.onodrim.Onodrim#setConfByAnnotations(Object)},
 * {@link org.onodrim.Onodrim#setConfByAnnotations(Object, org.onodrim.Configuration)} methods, that
 * call to the fisrt one). It can be associated to fields directly, or to methods that assign the
 * value (see documentation of {@link AnnotationProcessor#setConfiguration(Object, org.onodrim.Configuration)}.
 * @author Luis Rodero-Merino
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Configure {
    String parameter();
}
