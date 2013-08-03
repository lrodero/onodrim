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

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Class that groups several utility methods with heterogeneous functionality. Collections-related methods
 * are encapsulated in the inner class {@link Util.Collections}.
 * 
 * @author Luis Rodero-Merino
 * @since 1.0
 */
public class Util {

    private static Logger logger = Logger.getLogger(Util.class.getCanonicalName());
    
    /**
     * Deletes folder and its contents recursively.
     * @param dir Dir to delete
     * @return {@code true} if the folder was properly deleted, {@code false} otherwise (for example because there were problems
     * related with the folder permissions).
     */
    public static boolean removeDirRecursively(File dir) {
        if(!dir.exists())
            return true;
        if (dir.isDirectory())
            for (String childFile : dir.list())
                if (!removeDirRecursively(new File(dir, childFile)))
                    return false;
        boolean deleted = false;
        try {
            deleted = dir.delete();
        } catch(SecurityException exception) {
            logger.log(Level.WARNING, "Could not delete dir '" + dir.getAbsolutePath() +"', check permissions", exception);
        }   
        return deleted;
    }
    
    /**
     * Creates a ready handler to pass to a {@link java.util.logging.Logger}. It prints logging messages
     * to the {@link java.io.OutputStream} passed as parameter, performing an automatic flush after each log.
     * The layout of the messages is: {@code %1$5s: [%2$s|%3$s] %4$s\n}
     * @param out Messages will be printed to this stream
     * @param formatter To format log messages, if it is {@code null} a default formatter will be used. 
     * @return A custom handler for messages with automatic flushing enabled.
     */
    public static StreamHandler createStreamHandlerWithAutomaticFlushing(OutputStream out, Formatter formatter) {
        return new StreamHandlerWithAutomaticFlushing(out, formatter != null ? formatter : new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%1$5s: [%2$s|%3$s] %4$s\n", record.getLevel(),
                                    Thread.currentThread().getName(),
                                    record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1),
                                    record.getMessage());
            }
        });
    }
    
    /**
     * Instances of this class are created and returned by the {@link Util#createStreamHandlerWithAutomaticFlushing(OutputStream, Formatter)}
     * method.
     * @author Luis Rodero-Merino
     * @since 1.0
     */
    private static class StreamHandlerWithAutomaticFlushing extends StreamHandler {
        private StreamHandlerWithAutomaticFlushing(OutputStream out, Formatter formatter) {
            super(out, formatter);
        }
        @Override
        public synchronized void publish(LogRecord record) {
            super.publish(record);
            super.flush();
        }
    }
    
    /**
     * Class that groups static methods for arrays handling.
     * 
     * @author Luis Rodero-Merino
     * @since 1.0
     */
    public static class Collections {

        /**
         * Creates a new array that contains all the elements carried by the arrays
         * passed as parameter, null(s) included. E.g.:
         * 
         * {@code [[a,b,c], [null, b, d]] -> [a,b,c,null,b,d]}
         * 
         * @param arrays
         *            Arrays that will be combined.
         * @param elementsClass
         *            Class of arrays' members.
         * @param <T>
         *            Type of elements in arrays.
         * @return A new array that is the concatenation of the contents of all the
         *         arrays passed as parameter.
         */
        @SuppressWarnings("unchecked")
        public static <T> T[] union(T[][] arrays, Class<T> elementsClass) {

            if (arrays == null)
                throw new IllegalArgumentException(
                        "The array of arrays to unite cannot be null");

            if (elementsClass == null)
                throw new IllegalArgumentException(
                        "The type of the elements in the arrays to unite cannot null type");

            int unionLength = 0;
            for (T[] array : arrays)
                if (array != null)
                    unionLength += array.length;
            T[] union = (T[]) Array.newInstance(elementsClass, unionLength);
            int nextArrayPos = 0;
            for (T[] array : arrays)
                if (array != null) {
                    System.arraycopy(array, 0, union, nextArrayPos, array.length);
                    nextArrayPos += array.length;
                }
            return union;
        }

        /**
         * Transposes the matrix passed as parameter (as an array of arrays) and
         * returns a copy of it transposed. E.g.: {@code [[a1,a2,a3],   -> [[a1,b1],
         *         [b1,b2,b3]]       [a2,b2],
         *                           [a3,b3]]}
         * 
         * @param arrays
         *            Array of arrays ('matrix') to transpose.
         * @param elementsClass
         *            Class of arrays' members.
         * @param <T>
         *            Type of elements in arrays.
         * @return Matrix transposed, again as an array of arrays. Elements are
         *         copied.
         */
        @SuppressWarnings("unchecked")
        public static <T> T[][] transpose(T[][] arrays, Class<T> elementsClass) {

            if (arrays == null)
                throw new IllegalArgumentException(
                        "The array of arrays to tranpose cannot be null");

            if (elementsClass == null)
                throw new IllegalArgumentException(
                        "The type of the elements in the arrays to transpose cannot null type");

            // First, must get rid of empty and null arrays. Also, we check all
            // arrays have the same length
            int arraysSize = -1;
            List<T[]> arraysList = new ArrayList<T[]>();
            for (T[] array : arrays) {
                if ((array != null) && (array.length > 0))
                    arraysList.add(array);
                if (arraysSize == -1)
                    arraysSize = array.length;
                else if (arraysSize != array.length) {
                    throw new IllegalArgumentException(
                            "Lists have different sizes, cannot reverse them");
                }
            }

            // If there are no more arrays remaining, just return empty array
            if (arraysList.size() == 0)
                return (T[][]) Array.newInstance(elementsClass, 0, 0);

            T[][] traversedMatrix = (T[][]) Array.newInstance(elementsClass,
                    arraysList.get(0).length, arrays.length);
            for (int arrayIndex = 0; arrayIndex < arrays.length; arrayIndex++) {
                T[] array = arrays[arrayIndex];
                for (int elementIndex = 0; elementIndex < array.length; elementIndex++)
                    traversedMatrix[elementIndex][arrayIndex] = arrays[arrayIndex][elementIndex];
            }

            return traversedMatrix;
        }

        /**
         * Computes a (kind of) cartesian product of the arrays passed as parameter.
         * 
         * {@code [[a1,a2],[b1,b2],[c1,c2]] ->
         * [[a1,b1,c1],[a1,b1,c2],[a1,b2,c1],[a1,b2,c2],
         *  [a2,b1,c1],[a2,b1,c2],[a2,b2,c1],[a2,b2,c2]]
         * }
         * 
         * @param arrays
         *            Arrays that will be 'multiplied'.
         * @param elementsClass
         *            Class of arrays' members.
         * @param <T>
         *            Type of elements in arrays.
         * @return New array of arrays that contains the 'cartesian product' of the
         *         arrays passed as parameter.
         */
        @SuppressWarnings("unchecked")
        public static <T> T[][] product(T[][] arrays, Class<T> elementsClass) {

            if (arrays == null)
                throw new IllegalArgumentException(
                        "The array of arrays cannot be null");

            if (elementsClass == null)
                throw new IllegalArgumentException(
                        "The type of the elements in the arrays cannot null type");

            // First, must get rid of null arrays
            List<T[]> arraysList = new ArrayList<T[]>();
            for (T[] array : arrays)
                if ((array != null) && (array.length > 0))
                    arraysList.add(array);

            // If there are no more arrays remaining for the cartesian product, just
            // return empty array
            if (arraysList.size() == 0)
                return (T[][]) Array.newInstance(elementsClass, 0, 0);

            // Total number of combinations
            int totalCombinations = 1;
            for (T[] array : arraysList)
                totalCombinations *= array.length;

            T[][] cartesianProduct = (T[][]) Array.newInstance(elementsClass,
                    totalCombinations, arrays.length);
            for (int index = 0; index < cartesianProduct.length; index++)
                cartesianProduct[index] = (T[]) Array.newInstance(elementsClass,
                        arraysList.size());

            int combinationsSoFar = 1;
            for (int index = 0; index < arraysList.size(); index++) {
                T[] array = arraysList.get(index);
                combinationsSoFar *= array.length;
                int repetitionsOfEachItem = cartesianProduct.length
                        / combinationsSoFar;
                int iters = cartesianProduct.length
                        / (repetitionsOfEachItem * array.length);
                int rowIndex = 0;
                for (int iter = 0; iter < iters; iter++)
                    for (T item : array)
                        for (int repetition = 0; repetition < repetitionsOfEachItem; repetition++)
                            cartesianProduct[rowIndex++][index] = item;
            }

            return cartesianProduct;

        }

    }
}
