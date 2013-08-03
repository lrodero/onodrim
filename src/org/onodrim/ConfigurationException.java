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

package org.onodrim;

/**
 * This exception is used to mark errors in the parameters passed to be
 * processed by Onodrim.
 * 
 * @author Luis Rodero-Merino
 * @since 1.0
 */
public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
