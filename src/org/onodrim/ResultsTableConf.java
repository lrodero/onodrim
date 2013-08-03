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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Each instance of this class holds the configuration information needed to build a
 * {@link ResultsTable} instance.
 * An instance of this class will be passed to the
 * {@link ResultsTable#buildResultsTable(ResultsTableConf, java.util.Map, java.util.Map)} method
 * to actually print the table.
 * 
 * The configuration information is comprised of the following items:
 * <ul>
 *  <li> Parameter(s) that will build the column values, by groups.
 *  <li> Parameter(s) that will build the row values, by groups.
 *  <li> The result from the experiments that will be printed in the table.
 *  <li> If there are several experiments that where run for each combination of row and column
 *       parameters, how the results of these experiments should be computed/shown in the table.
 *       Possible values are defined in the {@link RESULTS_COMPUTATION} enum.
 *  <li> Name of the file where the table will be printed. 
 * </ul>
 * 
 * So, let's say that experiments are configured by four parameters {@code R1}, {@code R2}, {@code R3} and {@code C1}, each one
 * with these possible values:
 * <ul style="list-style-type: none;">
 *   <li>{@code R1 -> R1V1, R1V2}
 *   <li>{@code R2 -> R2V1, R2V2}
 *   <li>{@code R3 -> R3V1, R3V2}
 *   <li>{@code C1 -> C1V1, C1V2}
 * </ul>
 * 
 * Also, let's say all experiments are run twice, and each experiment gives the following values to the result data labeled
 * {@code ResJob}:
 * <ul style="list-style-type: none;">
 *  <li>{@code R1V1, R2V1, R3V1, C1V1 -> 1}
 *  <li>{@code R1V1, R2V1, R3V1, C1V1 -> 2}
 *  <li>{@code R1V1, R2V1, R3V1, C1V2 -> 3}
 *  <li>{@code R1V1, R2V1, R3V1, C1V2 -> 4}
 *  <li>{@code R1V1, R2V1, R3V2, C1V1 -> 5}
 *  <li>{@code R1V1, R2V1, R3V2, C1V1 -> 6}
 *  <li>{@code R1V1, R2V1, R3V2, C1V2 -> 7}
 *  <li>{@code R1V1, R2V1, R3V2, C1V2 -> 8}
 *  <li>{@code R1V1, R2V2, R3V1, C1V1 -> 9}
 *  <li>{@code R1V1, R2V2, R3V1, C1V1 -> 10}
 *  <li>{@code R1V1, R2V2, R3V1, C1V2 -> 11}
 *  <li>{@code R1V1, R2V2, R3V1, C1V2 -> 12}
 *  <li>{@code R1V1, R2V2, R3V2, C1V1 -> 13}
 *  <li>{@code R1V1, R2V2, R3V2, C1V1 -> 14}
 *  <li>{@code R1V1, R2V2, R3V2, C1V2 -> 15}
 *  <li>{@code R1V1, R2V2, R3V2, C1V2 -> 16}
 *  <li>{@code R1V2, R2V1, R3V1, C1V1 -> 17}
 *  <li>{@code R1V2, R2V1, R3V1, C1V1 -> 18}
 *  <li>{@code R1V2, R2V1, R3V1, C1V2 -> 19}
 *  <li>{@code R1V2, R2V1, R3V1, C1V2 -> 20}
 *  <li>{@code R1V2, R2V1, R3V2, C1V1 -> 21}
 *  <li>{@code R1V2, R2V1, R3V2, C1V1 -> 22}
 *  <li>{@code R1V2, R2V1, R3V2, C1V2 -> 23}
 *  <li>{@code R1V2, R2V1, R3V2, C1V2 -> 24}
 *  <li>{@code R1V2, R2V2, R3V1, C1V1 -> 25}
 *  <li>{@code R1V2, R2V2, R3V1, C1V1 -> 26}
 *  <li>{@code R1V2, R2V2, R3V1, C1V2 -> 27}
 *  <li>{@code R1V2, R2V2, R3V1, C1V2 -> 28}
 *  <li>{@code R1V2, R2V2, R3V2, C1V1 -> 29}
 *  <li>{@code R1V2, R2V2, R3V2, C1V1 -> 30}
 *  <li>{@code R1V2, R2V2, R3V2, C1V2 -> 31}
 *  <li>{@code R1V2, R2V2, R3V2, C1V2 -> 32}
 * </ul><br>
 *    
 * Now, for example, a table built using as row parameter {@code R1} and column parameter {@code C1} and
 * where {@code ResJob} values should be concatenated would be defined as {@code {R1|C1|ResJob|CONCAT}})
 * and printed as follows:
 * <table border="0">
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1</th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V1</th><td align="right" border="0">6|2|14|10</td><td align="right" border="0">30|18|22|26</td></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V2</th><td align="right" border="0">12|8|4|16</td><td align="right" border="0">28|32|20|24</td></tr>
 * </table>
 * <br>
 * 
 * If the table was built using as row parameters {@code R1} and {@code R2} and column {@code C1} with results
 * again concatenated, it would be defined as {@code {R1;R2|C1|ResJob|CONCAT}} and printed as follows:
 * <table border="0">
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R2</th><th align="right" border="0" bgcolor="#AAAAAA">R2V1</th><th align="right" border="0" bgcolor="#AAAAAA">R2V2</th><th align="right" border="0" bgcolor="#AAAAAA">R2V1</th><th align="right" border="0" bgcolor="#AAAAAA">R2V2</th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1</th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V1</th><td align="right" border="0">6|2</td><td align="right" border="0">14|10</td><td align="right" border="0">18|22</td><td align="right" border="0">30|26</td></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V2</th><td align="right" border="0">8|4</td><td align="right" border="0">12|16</td><td align="right" border="0">20|24</td><td align="right" border="0">28|32</td></tr>
 * </table>
 * <br>
 * 
 * The same table, but built computing the maximum value of results instead of concatenating them would be defined as
 * {@code {R1;R2|C1|ResJob|MAX}} and printed as follows:
 * <table border="0">
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R2</th><th align="right" border="0" bgcolor="#AAAAAA">R2V1</th><th align="right" border="0" bgcolor="#AAAAAA">R2V2</th><th align="right" border="0" bgcolor="#AAAAAA">R2V1</th><th align="right" border="0" bgcolor="#AAAAAA">R2V2</th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1</th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V1</th><td align="right" border="0">6.0</td><td align="right" border="0">14.0</td><td align="right" border="0">22.0</td><td align="right" border="0">30.0</td></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V2</th><td align="right" border="0">8.0</td><td align="right" border="0">16.0</td><td align="right" border="0">24.0</td><td align="right" border="0">32.0</td></tr>
 * </table>
 * <br>
 * 
 * In the examples above there was one set of parameters that define the rows in the table ({@code R1} and {@code R2}), and 
 * another set that defined the columns (with a single element {@code C1}). But it is possible to define tables with several
 * sets of parameters both for rows and columns. For example, the following definition {@code {R1;R2:R3|C1|ResJob|CONCAT}}
 * configures the table to generate two sets of rows, the first one with {@code R1} and {@code R2} as before and the second
 * one with a single element {@code R3}. The resulting table would look as follows:
 * <table border="0">
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V1</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th><th align="right" border="0" bgcolor="#AAAAAA">R1V2</th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">R2</th><th align="right" border="0" bgcolor="#AAAAAA">R2V1</th><th align="right" border="0" bgcolor="#AAAAAA">R2V2</th><th align="right" border="0" bgcolor="#AAAAAA">R2V1</th><th align="right" border="0" bgcolor="#AAAAAA">R2V2</th><th align="right" border="0" bgcolor="#AAAAAA">R3</th><th align="right" border="0" bgcolor="#AAAAAA">R3V1</th><th align="right" border="0" bgcolor="#AAAAAA">R3V2</th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1</th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th><th align="right" border="0" bgcolor="#AAAAAA"></th></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V1</th><td align="right" border="0">6|2</td><td align="right" border="0">14|10</td><td align="right" border="0">18|22</td><td align="right" border="0">30|26</td><th align="right" border="0" bgcolor="#AAAAAA"></th><td align="right" border="0">2|10</td><td align="right" border="0">6|14</td></tr>
 * <tr border="0"><th align="right" border="0" bgcolor="#AAAAAA">C1V2</th><td align="right" border="0">8|4</td><td align="right" border="0">12|16</td><td align="right" border="0">20|24</td><td align="right" border="0">28|32</td><th align="right" border="0" bgcolor="#AAAAAA"></th><td align="right" border="0">12|4</td><td align="right" border="0">8|16</td></tr>
 * </table>
 * 
 * In this example some results are repeated for both groups of rows. This is the correct outcome given the input, however this feature
 * is in fact intented to easily compare results from different sets of experiments with no overlapping. These would be configured using
 * Onodrim 'packets'.
 * 
 * @author Luis Rodero-Merino
 * @since 1.0
 */
public class ResultsTableConf {

    public static final String CONCATENATION_SEPARATOR = new String("|"); // 0xA6 = "¦"
    
    public static enum RESULTS_COMPUTATION {
        /** Choose the maximum value of all results */
        MAX,
        /** Choose the minimum value of all results */
        MIN,
        /** Compute the average of all results values */
        MEAN,
        /** Get the first value of all results */
        FIRST,
        /** Get the last value of all results */
        LAST,
        /** Get a random value among all results */
        RANDOM,
        /** Build a string concatenating all values */
        CONCAT
    };

    private static ResultsTableConf.RESULTS_COMPUTATION DEFAULT_RESULTS_COMPUTATION = RESULTS_COMPUTATION.FIRST;
    private static final String DEFINITION_ELEMENTS_SEPARATOR = "\\|";
    private static final String PARAMETER_SETS_SEPARATOR = ":";
    private static final String PARAMETERS_IN_SET_SEPARATOR = ";";

    private String[][] rowParamsSets = null;
    private String[][] columnParamsSets = null;
    private String resultToStore = null;
    private ResultsTableConf.RESULTS_COMPUTATION resultsComputation = null;
    private String resultFileName = null;

    /**
     * @param rowParamsSets Sets of parameters that will be placed in the table top header 
     * @param columnParamsSets Sets of parameters that will be placed in the table left header
     * @param resultToStore Name of the result whose values will be used to build the table
     * @param resultsComputation The results from several experiments can correspond to the same cell in the table,
     * here it is defined how those values should be combined (choose the maximum, concatenate them, etc.)
     * @param resultFileName Name of the file where results should be stored
     */
    public ResultsTableConf(String[][] rowParamsSets, String[][] columnParamsSets,
            String resultToStore,
            ResultsTableConf.RESULTS_COMPUTATION resultsComputation,
            String resultFileName) {
        this.rowParamsSets = rowParamsSets;
        this.columnParamsSets = columnParamsSets;
        this.resultToStore = resultToStore;
        this.resultsComputation = resultsComputation;
        this.resultFileName = resultFileName;
    }

    // Parse
    // {rowsSet1_Param1;rowsSet1_Param2:rowsSet2_Param1|columnParam1:columnParam2|resultName|operation|resultFileName,
    // ...} and
    // convert it into a list of ResultsTablesConf instances
    public static List<ResultsTableConf> parseResultsTablesConf(String[] resultsTablesConfs) throws ConfigurationException {
        List<ResultsTableConf> resultsTablesConfList = new ArrayList<ResultsTableConf>();
        if(resultsTablesConfs == null)
            return resultsTablesConfList;
        for (String resultTableConf : resultsTablesConfs)
            resultsTablesConfList.add(parseResultsTableConf(resultTableConf.trim()));
        return resultsTablesConfList;
    }

    public String[][] getRowParamsSets() {
        return rowParamsSets;
    }

    public String[][] getColumnParamsSets() {
        return columnParamsSets;
    }

    public String getResultToStore() {
        return resultToStore;
    }

    public ResultsTableConf.RESULTS_COMPUTATION getResultsComputationMethod() {
        return resultsComputation;
    }

    public String getResultFileName() {
        return resultFileName;
    }
    
    public static void main(String[] args) throws ConfigurationException {
        print(parseResultsTableConf("rowsSet1.Param1;rowsSet1.Param2:rowsSet2.Param1|columnParam1;columnParam2|resultName|CONCAT"));
    }
    
    private static void print(ResultsTableConf resultsTableConf) {
        System.out.println("\n\tROWS PARAMS SETS:");
        String[][] rowParamsSets = resultsTableConf.getRowParamsSets();
        for(String[] rowParamsSet: rowParamsSets) {
            System.out.println("\t\tROW PARAM SET");
            for(String rowParam: rowParamsSet)
                System.out.println("\t\t\t" + rowParam);
        }
        System.out.println("\n\tCOLUMNS PARAMS SETS:");
        String[][] columnParamsSets = resultsTableConf.getColumnParamsSets();
        for(String[] columnParamsSet: columnParamsSets) {
            System.out.println("\t\tCOLUMN PARAM SET");
            for(String columnParam: columnParamsSet)
                System.out.println("\t\t\t" + columnParam);
        }
        System.out.println();
        System.out.println("\tRESULT: " + resultsTableConf.getResultToStore());
        System.out.println("\tMETHOD: " + resultsTableConf.getResultsComputationMethod());
        System.out.println("\tFILE NAME: " + resultsTableConf.getResultFileName());
    }

    // Split
    // rowsSet1_Param1;rowsSet1_Param2:rowsSet2_Param1|columnParam1:columnParam2|resultName|operation|resultFileName
    // and convert it into a ResultsTableConf instance
    private static ResultsTableConf parseResultsTableConf(String resultsTableConf) throws ConfigurationException {
        String[] tokens = resultsTableConf.split(DEFINITION_ELEMENTS_SEPARATOR);
        if ((tokens.length < 4) || (tokens.length > 5))
            throw new ConfigurationException("Cannot parse table configuration '" + resultsTableConf
                            + "', it should have 4 or 5 strings split by '|' and it has " + tokens.length);
        String[] rowParamsSetsRaw = tokens[0].trim().split(PARAMETER_SETS_SEPARATOR);
        String[][] rowParamsSets = new String[rowParamsSetsRaw.length][];
        for(int index = 0; index < rowParamsSets.length; index++) {
            if(rowParamsSetsRaw[index].isEmpty()) 
                throw new ConfigurationException("Cannot parse table configuration '" + resultsTableConf + "', it contains an empty row set param");
            rowParamsSets[index] = rowParamsSetsRaw[index].split(PARAMETERS_IN_SET_SEPARATOR);
            for(String rowParam: rowParamsSets[index])
                if(rowParam.isEmpty())
                    throw new ConfigurationException("Cannot parse table configuration '" + resultsTableConf + "', it contains an empty row param");
        }

        String[] columnParamsSetsRaw = tokens[1].trim().split(PARAMETER_SETS_SEPARATOR);
        String[][] columnParamsSets = new String[columnParamsSetsRaw.length][];
        for(int index = 0; index < columnParamsSets.length; index++) {
            if(columnParamsSetsRaw[index].isEmpty()) 
                throw new ConfigurationException("Cannot parse table configuration '" + resultsTableConf + "', it contains an empty column set param");
            columnParamsSets[index] = columnParamsSetsRaw[index].split(PARAMETERS_IN_SET_SEPARATOR);
            for(String columnParam: columnParamsSets[index])
                if(columnParam.isEmpty())
                    throw new ConfigurationException("Cannot parse table configuration '" + resultsTableConf + "', it contains an empty column param");
        }
        
        String resultToStore = tokens[2].trim();
        if (resultToStore.isEmpty())
            throw new ConfigurationException("Cannot parse table configuration '" + resultsTableConf + "', the result to store cannot be empty");

        String computationToApply = tokens[3].trim();
        ResultsTableConf.RESULTS_COMPUTATION resultsComputation = DEFAULT_RESULTS_COMPUTATION;
        if (!computationToApply.isEmpty())
            try {
                resultsComputation = RESULTS_COMPUTATION
                        .valueOf(computationToApply);
            } catch (IllegalArgumentException exception) {
                throw new ConfigurationException("Cannot parse '" + computationToApply + "' to a valid computation in table configuration '" + resultsTableConf + "'");
            }

        String resultFileName = null;
        if ((tokens.length == 5) && (!tokens[4].trim().isEmpty()))
            resultFileName = tokens[4];
        else {
            resultFileName = "RESULTS_TABLE_" + resultToStore + "_USING_";
            for(int index = 0; index < rowParamsSets.length; index++){
                String[] rowParamsSet = rowParamsSets[index];
                if(index > 0)
                    resultFileName += "!";
                for(int index2 = 0; index2 < rowParamsSet.length; index2++) {
                    if(index2 > 0)
                        resultFileName += "-";
                    resultFileName += rowParamsSet[index2];
                }
            }
            resultFileName += "_AGAINST_";
            for(int index = 0; index < columnParamsSets.length; index++) {
                String[] columnParamsSet = columnParamsSets[index];
                if(index > 0)
                    resultFileName += "!";
                for(int index2 = 0; index2 < columnParamsSet.length; index2++) {
                    if(index2 > 0)
                        resultFileName += "-";
                    resultFileName += columnParamsSet[index2];
                }
            }
        }
        return new ResultsTableConf(rowParamsSets, columnParamsSets, resultToStore, resultsComputation, resultFileName);
    }

    public static String computeResults(List<Object> values, ResultsTableConf.RESULTS_COMPUTATION computation) {

        if (values.isEmpty())
            return "NO_RESULT";

        switch (computation) {
            case MAX:
                return max(values);
            case MIN:
                return min(values);
            case MEAN:
                return mean(values);
            case FIRST:
                return values.get(0).toString();
            case LAST:
                return values.get(values.size() - 1).toString();
            case RANDOM:
                return values.get(new Random().nextInt(values.size())).toString();
            case CONCAT:
                return concat(values);
            default:
                throw new Error("Should never get here! Computation " + computation + " not understood");
        }
    }

    private static String max(List<Object> values) {
        double maxValue = Double.MIN_VALUE;
        double[] numericValues = parseNums(values);
        for (double value : numericValues)
            if (value > maxValue)
                maxValue = value;
        return Double.toString(maxValue);
    }

    private static String min(List<Object> values) {
        double minValue = Double.MAX_VALUE;
        double[] numericValues = parseNums(values);
        for (double value : numericValues)
            if (value < minValue)
                minValue = value;
        return Double.toString(minValue);
    }

    private static String mean(List<Object> values) {
        double sum = 0;
        double[] numericValues = parseNums(values);
        for (double value : numericValues)
            sum += value;
        return Double.toString(sum / values.size());
    }

    private static String concat(List<Object> values) {
        String concat = "";
        if (values.size() > 0)
            concat = values.get(0).toString();
        for (int remainingIndex = 1; remainingIndex < values.size(); remainingIndex++)
            concat += (CONCATENATION_SEPARATOR + values.get(remainingIndex));
        return concat;
    }

    private static double[] parseNums(List<Object> values) {
        double[] parsedValues = new double[values.size()];
        for (int valueIndex = 0; valueIndex < parsedValues.length; valueIndex++)
            parsedValues[valueIndex] = Double.parseDouble(values.get(valueIndex).toString());
        return parsedValues;
    }

}