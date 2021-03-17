/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bindgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Java resources for the unit testing of different method bindings generated.
 *
 * @since 2.0.0
 */
public class MethodsTestResource {

    // Different instance method combinations
    private void privateMethod() {
    }

    protected void protectedMethod() {
    }

    void defaultMethod() {
    }

    public void returnVoid() {
    }

    public void returnOptionalError() throws Exception {
    }

    public IOException returnError() {
        return new IOException();
    }

    public Map returnInterface() {
        return new HashMap();
    }

    public Set<String> returnGenericObject() {
        return new HashSet<>();
    }

    public AbstractSet returnAbstractObject() {
        return new HashSet<>();
    }

    public System.Logger.Level returnEnum() {
        return System.Logger.Level.INFO;
    }

    public void errorParam(IOException x, String[] y) {
    }

    public void interfaceParam(Map x, Object[] y, Object[] z) {
    }

    public Set<String> genericObjectParam(Set<String> x, int y, String[] z) {
        return x;
    }

    public void abstractObjectParam(AbstractSet x, Object y, String z) {
    }

    public System.Logger.Level enumParam(System.Logger.Level x) {
        return x;
    }

    public File returnObject() {
        return new File("test.txt");
    }

    public File returnObjectThrowable() throws FileNotFoundException {
        return new File("test.txt");
    }

    public File[] returnObjectThrowableError1() throws FileNotFoundException {
        return new File[2];
    }

    public File returnObjectThrowableError2(int[] x) throws FileNotFoundException {
        return new File("test.txt");
    }

    public File[] returnObjectError1() {
        return new File[2];
    }

    public File returnObjectError2(int[] x) {
        return new File("test.txt");
    }

    public byte returnByte(byte x) throws ArithmeticException {
        return x;
    }

    public char returnChar(char x, int[] y) {
        return x;
    }

    public short returnShort(short x, int y) throws ArithmeticException {
        return x;
    }

    public int returnInt(long x) {
        return 5;
    }

    public long returnLong(long x) {
        return x;
    }

    public float returnFloat(float x) {
        return x;
    }

    public float returnFloat(float x, String y) {
        return x;
    }

    public double returnDouble(double x) {
        return x;
    }

    public double returnDouble(double x, Object y) {
        return x;
    }

    public boolean returnBoolean(boolean x) {
        return x;
    }

    public boolean returnBoolean(boolean x, Object[] y) throws ArithmeticException {
        return x;
    }

    public String returnString(String x) {
        return x;
    }

    public byte[] returnByteArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public char[] returnCharArray(char[] x, int[] y, short[] z) {
        return x;
    }

    public short[] returnShortArray(short x, int y) throws ArithmeticException {
        return new short[2];
    }

    public int[] returnIntArray(int[] x, boolean y, boolean[] z) {
        return x;
    }

    public long[] returnLongArray(long[] x, Object y) throws ArithmeticException {
        return x;
    }

    public float[] returnFloatArray(float[] x, String y) {
        return x;
    }

    public double[] returnDoubleArray(double[] x, double[] y) {
        return x;
    }

    public boolean[] returnBooleanArray() {
        return new boolean[2];
    }

    public String[] returnStringArray1(String[] x, StringBuffer y, int z) {
        return x;
    }

    public String[] returnStringArray2(String[] x, StringBuffer y, int z) throws InterruptedException {
        return x;
    }

    public byte[] returnByteMultiArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public char[] returnCharMultiArray(char[] x, int[] y, Object[] z) {
        return x;
    }

    public short[] returnShortMultiArray(short x, int y, short[] z) {
        return new short[2];
    }

    public int[] returnIntMultiArray(int[] x, boolean y) throws ArithmeticException {
        return x;
    }

//    public long[][] returnLongMultiArray(long[][] x, Object y, boolean[][] z) {
//        return x;
//    }

    public float[][] returnFloatMultiArray(float[][] x, String y) throws ArithmeticException {
        return x;
    }

    public double[] returnDoubleMultiArray(double[] x, double[] y) {
        return x;
    }

    public boolean[] returnBooleanMultiArray() {
        return new boolean[2];
    }

//    public String[][] returnStringMultiArray(String[][] x, StringBuffer y, int z)
//            throws ArrayIndexOutOfBoundsException {
//        return x;
//    }

    // Different static method combinations

    private static void privateStaticMethod() {
    }

    protected static void protectedStaticMethod() {
    }

    static void defaultStaticMethod() {
    }

    public static void returnStaticVoid() {
    }

    public static void returnStaticOptionalError() throws Exception {
    }

    public static IOException returnStaticError() {
        return new IOException();
    }

    public static Map returnStaticInterface() {
        return new HashMap();
    }

    public static Set<String> returnStaticGenericObject() {
        return new HashSet<>();
    }

    public static AbstractSet returnStaticAbstractObject() {
        return new HashSet<>();
    }

    public static System.Logger.Level returnStaticEnum() {
        return System.Logger.Level.INFO;
    }

//    public static void errorStaticParam(IOException x, String[][] z) {
//    }

    public static void interfaceStaticParam(Map x, boolean[] y) {
    }

    public static Set<String> genericObjectStaticParam(Set<String> x) {
        return x;
    }

    public static void abstractObjectStaticParam(AbstractSet x, int y, Object z) {
    }

    public static System.Logger.Level enumStaticParam(System.Logger.Level x, Object[] y) {
        return x;
    }

    public static File returnStaticObject() {
        return new File("test.txt");
    }

    public static File returnStaticObjectThrowable() throws FileNotFoundException {
        return new File("test.txt");
    }

    public static File[] returnStaticObjectThrowableError1() throws FileNotFoundException {
        return new File[2];
    }

    public static File returnStaticObjectThrowableError2(int[] x) throws FileNotFoundException {
        return new File("test.txt");
    }

    public static File[] returnStaticObjectError1() {
        return new File[2];
    }

    public static File returnStaticObjectError2(int[] x) {
        return new File("test.txt");
    }

    public static byte returnStaticByte(byte x) throws ArithmeticException {
        return x;
    }

    public static char returnStaticChar(char x, int[] y) {
        return x;
    }

    public static short returnStaticShort(short x, int y) throws ArithmeticException {
        return x;
    }

    public static int returnStaticInt(long x) {
        return 5;
    }

    public static long returnStaticLong(long x) {
        return x;
    }

    public static float returnStaticFloat(float x, String y) {
        return x;
    }

    public static float returnStaticFloat(float x) {
        return x;
    }

    public static double returnStaticDouble(double x) {
        return x;
    }

    public static boolean returnStaticBoolean(boolean x) {
        return x;
    }

    public static double returnStaticDouble(double x, Object y, String[] z) {
        return x;
    }

    public static boolean returnStaticBoolean(boolean x, Object[] y) throws ArithmeticException {
        return x;
    }

    public static String returnStaticString(String x) {
        return x;
    }

    public static String returnStaticString2(String x) throws IOException {
        return x;
    }

    public static byte[] returnStaticByteArray(byte[] x) throws ArithmeticException {
        return x;
    }

    public static char[] returnStaticCharArray(char[] x, int[] y) {
        return x;
    }

    public static short[] returnStaticShortArray(short x, int y) throws ArithmeticException {
        return new short[2];
    }

    public static int[] returnStaticIntArray(int[] x, boolean y, boolean[] z) {
        return x;
    }

    public static long[] returnStaticLongArray(long[] x, Object y) throws ArithmeticException {
        return x;
    }

//    public static float[] returnStaticFloatArray(float[] x, String y, Object[][] z) {
//        return x;
//    }

    public static double[] returnStaticDoubleArray(double[] x, double[] y) {
        return x;
    }

    public static boolean[] returnStaticBooleanArray() {
        return new boolean[2];
    }

    public static String[] returnStaticStringArray(String[] x, StringBuffer y, int z)
            throws ArrayIndexOutOfBoundsException {
        return x;
    }

//    public static byte[][] returnStaticByteMultiArray(byte[][] x) throws ArithmeticException {
//        return x;
//    }
//
//    public static char[][] returnStaticCharMultiArray(char[][] x, int[] y, String[][] z) {
//        return x;
//    }
//
//    public static short[][] returnStaticShortMultiArray(short x, int y, short[][] z) {
//        return new short[2][1];
//    }
//
//    public static int[][] returnStaticIntMultiArray(int[][] x, boolean y) throws ArithmeticException {
//        return x;
//    }
//
//    public static long[][] returnStaticLongMultiArray(long[][] x, Object y, boolean[][] z) {
//        return x;
//    }
//
//    public static float[][] returnStaticFloatMultiArray(float[][] x, String y) throws ArithmeticException {
//        return x;
//    }
//
//    public static double[][] returnStaticDoubleMultiArray(double[][] x, double[] y) {
//        return x;
//    }

//    public static boolean[][] returnStaticBooleanMultiArray() {
//        return new boolean[2][1];
//    }

//    public static String[][] returnStaticStringMultiArray(String[][] x, StringBuffer y, int z)
//            throws ArrayIndexOutOfBoundsException {
//        return x;
//    }
}
