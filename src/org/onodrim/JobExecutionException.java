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
 * Raised when some error was found by the {@link JobsExecutor} when executing a {@link JobsSet}. A possible reason is that
 * the framework could not create the folder to contain the directories where {@link Job}s will store their results.
 * @author lrodero
 *
 */
public class JobExecutionException extends Exception {

    private static final long serialVersionUID = 1L;

    public JobExecutionException(String msg) {
        super(msg);
    }

    public JobExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
