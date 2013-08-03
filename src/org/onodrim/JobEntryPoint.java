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
 * This interface must be implemented by the class that contains the functionality to be run when a {@link Job} is executed,
 * in fact when creating a new {@link Job} instance is mandatory to pass it as parameter of the constructor an instance
 * that implements this interface.
 * @author lrodero
 *
 */
public interface JobEntryPoint {
    /**
     * Method that will be called by the {@link Job#run()} method, which in turn is called right after
     * {@link Job#preExecutionProcess()} has been executed. When this method returns, then
     * {@link Job#run()} will return and method {@link Job#postExecutionProcess(Throwable)} will be
     * called.
     */
    public void startJob();
}
