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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.onodrim.Configuration;
import org.onodrim.ConfigurationException;

public class AnnotationProcessor {
    
    private final static Logger logger = Logger.getLogger(AnnotationProcessor.class.getCanonicalName());

    /**
     * This method reads the configuration passed as parameter and sets the corresponding variables in
     * the object. To set a certain variable the value of a parameter (e.g. {@code ParamName}),
     * that variable must be annotated with {@code @Configure(parameter = "ParamName")}. Types are inferfed
     * by Onodrim. Example:<br>
     * <pre>
     * &#64;Configure(parameter = "ParamName1")
     * public String param1;
     * &#64;Configure(parameter = "ParamName2")
     * public int param2;
     * </pre>
     * For lists/arrays, however, the framework cannot set the value straight ahead, it needs the class to implement
     * a set method named after the parameter name. For example, to set the value of a parameter {@code ListProp}, which
     * contains a list of integers:<br>
     * <pre>
     * &#64;Configure(parameter = "ListProp")
     * private int[] listPropVal = null;
     * public void setListProp(int[] listPropVal) &#123;
     *     this.listPropVal = listPropVal;
     * &#125;
     * </pre>
     * @param object Instance that contains the annotated variables/methods that will be configured. 
     * @param configuration Configuration to read the parameter values from.
     * @throws ConfigurationException If the {@code Configuration} annotation has no set the parameter
     * name, no parameter with that name is found, or there is some error when setting the variable/calling
     * the method.
     */
    public static void setConfiguration(Object object, Configuration configuration) throws ConfigurationException {

        // For each local variable, look if it has the '@Configure' annotation
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            Configure confAnn = field.getAnnotation(Configure.class);
            if (confAnn == null)
                continue;
            String parameterName = confAnn.parameter();
            logger.log(Level.FINE, "Found parameter " + parameterName);
            // It is the parameter name defined?
            if ((parameterName == null) || (parameterName.isEmpty()))
                throw new ConfigurationException("Field " + field.getName()
                        + " in instance of Class "
                        + object.getClass().getName()
                        + " has no value for annotation "
                        + Configure.class.getName());
            // Field type, necessary for the conversion
            Class<?> typeClass = field.getType();
            Object value = getParameter(parameterName, typeClass, configuration);
            try {
                field.set(object, value);
            } catch (IllegalArgumentException exception) {
                throw new ConfigurationException(
                        "Could not set value of field " + field.getName()
                                + " in instance of class "
                                + object.getClass().getName()
                                + ", could not convert value to field type "
                                + typeClass.getName(), exception);
            } catch (IllegalAccessException exception) {
                throw new ConfigurationException(
                        "Could not set value of field " + field.getName()
                                + " in instance of class "
                                + object.getClass().getName()
                                + ", IllegalAccessException caught (WTF??)",
                        exception);
            }

        }

        // For each method, look if it has the '@Configure' annotation
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            Configure confAnn = method.getAnnotation(Configure.class);
            if (confAnn == null)
                continue;

            String parameterName = confAnn.parameter();
            // It is the parameter name defined?
            if ((parameterName == null) || (parameterName.isEmpty()))
                throw new ConfigurationException("Method " + method.getName()
                        + " in instance of Class "
                        + object.getClass().getName()
                        + " has no value for annotation "
                        + Configure.class.getName());
            // It must be a method with one single parameter
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1)
                throw new ConfigurationException("Method " + method.getName()
                        + " in instance of Class "
                        + object.getClass().getName()
                        + " must have one and only one parameter");
            Object value = getParameter(parameterName, parameterTypes[0], configuration);
            try {
                method.invoke(object, value);
            } catch (IllegalArgumentException exception) {
                throw new ConfigurationException("Could not call to method "
                        + method.getName() + " in instance of class "
                        + object.getClass().getName()
                        + ", the parameter is not valid", exception);
            } catch (IllegalAccessException exception) {
                throw new ConfigurationException("Could not call to method "
                        + method.getName() + " in instance of class "
                        + object.getClass().getName()
                        + ", IllegalAccessException caught (WTF??)", exception);
            } catch (InvocationTargetException exception) {
                throw new ConfigurationException("Could not call to method "
                        + method.getName() + " in instance of class "
                        + object.getClass().getName()
                        + ", an exception was thrown by the method", exception);
            }
        }
    }

    private static Object getParameter(String parameterName, Class<?> typeClass, Configuration configuration)
            throws ConfigurationException {

        if (!typeClass.isArray())
            return configuration.getParameter(parameterName, typeClass);

        Object[] array = configuration.getArrayParameter(parameterName,
                typeClass.getComponentType(), null);

        if (array == null)
            return array;

        if (!typeClass.getComponentType().isPrimitive()) // Ok, the array was of
                                                         // references, can
                                                         // assign it
            return array;

        // If the type class was of primitive type (e.g. int[]) we must
        // transform it because the method getArrayParameter
        // always returns arrays of references (e.g. Integer[]).
        if (typeClass.getComponentType().equals(Character.TYPE)) {
            char[] arrayPrim = new char[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Character) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Boolean.TYPE)) {
            boolean[] arrayPrim = new boolean[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Boolean) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Byte.TYPE)) {
            byte[] arrayPrim = new byte[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Byte) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Short.TYPE)) {
            short[] arrayPrim = new short[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Short) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Integer.TYPE)) {
            int[] arrayPrim = new int[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Integer) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Long.TYPE)) {
            long[] arrayPrim = new long[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Long) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Float.TYPE)) {
            float[] arrayPrim = new float[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Float) array[index];
            return arrayPrim;
        }

        if (typeClass.getComponentType().equals(Double.TYPE)) {
            double[] arrayPrim = new double[array.length];
            for (int index = 0; index < arrayPrim.length; index++)
                arrayPrim[index] = (Double) array[index];
            return arrayPrim;
        }

        throw new Error("This should never happen, parameter '" + parameterName
                + "' should be translated to an array of "
                + typeClass.getComponentType() + " which is"
                + "primitive, but cannot parse it to any known primitive type");

    }

}
