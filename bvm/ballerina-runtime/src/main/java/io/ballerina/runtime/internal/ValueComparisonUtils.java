/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrorType;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.util.PrimitiveIterator;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;

/**
 * Class @{@link ValueComparisonUtils} provides utils to compare Ballerina Values.
 *
 * @since 2.0.0
 */
public class ValueComparisonUtils {

    /**
     * Check if left hand side ordered type value is less than right hand side ordered type value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side ordered type value is less than right hand side ordered type value, else false.
     */
    public static boolean compareValueLessThan(Object lhsValue, Object rhsValue) {
        try {
            return compareValues(lhsValue, rhsValue) < 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side ordered type value is less than or equal to the right hand side ordered type value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side ordered type value is less than or equal to the right hand side ordered type
     * value, else false.
     */
    public static boolean compareValueLessThanOrEqual(Object lhsValue, Object rhsValue) {
        try {
            return compareValues(lhsValue, rhsValue) <= 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side ordered type value is greater than the right hand side ordered type value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side ordered type value is greater than the right hand side ordered type value, else
     * false.
     */
    public static boolean compareValueGreaterThan(Object lhsValue, Object rhsValue) {
        try {
            return compareValues(lhsValue, rhsValue) > 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side ordered type value is greater than or equal to the right hand side ordered type value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side ordered type value is greater than or equal to the right hand side ordered type
     * value, else
     * false.
     */
    public static boolean compareValueGreaterThanOrEqual(Object lhsValue, Object rhsValue) {
        try {
            return compareValues(lhsValue, rhsValue) >= 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side float value is less than right hand side float value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side float value is less than right hand side float value, else false.
     */
    public static boolean compareValueLessThan(double lhsValue, double rhsValue) {
        try {
            return compareFloatValues(lhsValue, rhsValue) < 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side float value is less than or equal to the right hand side float value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side float value is less than or equal to the right hand side float value, else false.
     */
    public static boolean compareValueLessThanOrEqual(double lhsValue, double rhsValue) {
        try {
            return compareFloatValues(lhsValue, rhsValue) <= 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side float value is greater than the right hand side float value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side float value is greater than the right hand side float value, else false.
     */
    public static boolean compareValueGreaterThan(double lhsValue, double rhsValue) {
        try {
            return compareFloatValues(lhsValue, rhsValue) > 0;
        } catch (BError error) {
            return false;
        }
    }

    /**
     * Check if left hand side float value is greater than or equal to the right hand side float value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if left hand side float value is greater than or equal to the right hand side float value, else
     * false.
     */
    public static boolean compareValueGreaterThanOrEqual(double lhsValue, double rhsValue) {
        try {
            return compareFloatValues(lhsValue, rhsValue) >= 0;
        } catch (BError error) {
            return false;
        }
    }

    private static int compareValues(Object lhsValue, Object rhsValue) {
        return compareValues(lhsValue, rhsValue, "");
    }

    private static int compareFloatValues(double lhsValue, double rhsValue) {
        return compareFloatValues(lhsValue, rhsValue, true, true);
    }

    /**
     * Check if left hand side value is greater than, less than or equal to the right hand side value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @param direction Sort direction ascending/descending/""
     * @return 1 if the left hand side value > right hand side value,
     * -1 if left hand side value < right hand side value,
     * 0 left hand side value = right hand side value
     */
    public static int compareValues(Object lhsValue, Object rhsValue, String direction) {
        int lhsTypeTag = TypeChecker.getType(lhsValue).getTag();
        int rhsTypeTag = TypeChecker.getType(rhsValue).getTag();
        boolean inRelationalExpr = false;
        if (direction.isEmpty()) {
            inRelationalExpr = true;
        }
        boolean isAscending = direction.equals("ascending");
        // Compare(x,()) is UN if x is not ().
        // () should come last for CompareA(x, y) and CompareD(x, y).
        if (lhsValue == null) {
            if (rhsValue == null) {
                return 0;
            }
            if (inRelationalExpr) {
                throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_RUNTIME_PKG_ID,
                        RuntimeErrorType.UNORDERED_TYPES_IN_COMPARISON, TYPE_NULL, rhsValue);
            }
            if (isAscending) {
                return 1;
            }
            return -1;
        }
        if (rhsValue == null) {
            if (inRelationalExpr) {
                throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_RUNTIME_PKG_ID,
                        RuntimeErrorType.UNORDERED_TYPES_IN_COMPARISON, lhsValue, TYPE_NULL);
            }
            if (isAscending) {
                return -1;
            }
            return 1;
        }
        if (TypeTags.isStringTypeTag(lhsTypeTag) && TypeTags.isStringTypeTag(rhsTypeTag))  {
            return codePointCompare(lhsValue.toString(), rhsValue.toString());
        }

        if (TypeTags.isIntegerTypeTag(lhsTypeTag) && TypeTags.isIntegerTypeTag(rhsTypeTag)) {
            return Long.compare((long) lhsValue, (long) rhsValue);
        }

        if (lhsTypeTag == rhsTypeTag) {
            switch (lhsTypeTag) {
                case TypeTags.BOOLEAN_TAG:
                    return Boolean.compare((boolean) lhsValue, (boolean) rhsValue);
                case TypeTags.BYTE_TAG:
                    return Integer.compare((int) lhsValue, (int) rhsValue);
                case TypeTags.FLOAT_TAG:
                    return compareFloatValues((double) lhsValue, (double) rhsValue, inRelationalExpr, isAscending);
                case TypeTags.DECIMAL_TAG:
                    if (TypeChecker.checkDecimalEqual((DecimalValue) lhsValue, (DecimalValue) rhsValue)) {
                        return 0;
                    }
                    if (checkDecimalGreaterThan((DecimalValue) lhsValue, (DecimalValue) rhsValue)) {
                        return 1;
                    }
                    return -1;
                case TypeTags.ARRAY_TAG:
                case TypeTags.TUPLE_TAG:
                    return compareArrayValues(lhsValue, rhsValue, lhsTypeTag, direction);
            }
        }

        throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_RUNTIME_PKG_ID,
                RuntimeErrorType.UNSUPPORTED_COMPARISON_OPERATION, TypeChecker.getType(lhsValue),
                TypeChecker.getType(rhsValue));
    }

    private static int codePointCompare(String str1, String str2) {
        PrimitiveIterator.OfInt iterator1 = str1.codePoints().iterator();
        PrimitiveIterator.OfInt iterator2 = str2.codePoints().iterator();
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                return 1;
            }
            Integer codePoint1 = iterator1.next();
            Integer codePoint2 = iterator2.next();

            int cmp = codePoint1.compareTo(codePoint2);
            if (cmp != 0) {
                return cmp;
            }
        }
        if (iterator2.hasNext()) {
            return -1;
        }
        return 0;
    }

    private static int compareFloatValues(double lhsValue, double rhsValue, boolean inRelationalExpr,
                                          boolean isAscending) {
        // Compare(x, y) is UN if x or y or both is NaN
        // NaN should be placed last or one before the last when () is present for
        // CompareA(x, y) and CompareD(x, y).
        if (Double.isNaN(lhsValue)) {
            if (inRelationalExpr) {
                throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_RUNTIME_PKG_ID,
                        RuntimeErrorType.UNORDERED_TYPES_IN_COMPARISON, lhsValue, rhsValue);
            }
            if (Double.isNaN(rhsValue)) {
                return 0;
            }
            if (isAscending) {
                return 1;
            }
            return -1;
        }
        if (Double.isNaN(rhsValue)) {
            if (inRelationalExpr) {
                throw ErrorUtils.createRuntimeError(RuntimeConstants.BALLERINA_LANG_RUNTIME_PKG_ID,
                        RuntimeErrorType.UNORDERED_TYPES_IN_COMPARISON, lhsValue, rhsValue);
            }
            if (isAscending) {
                return -1;
            }
            return 1;
        }
        // -0.0 = +0.0
        if (lhsValue == 0 && rhsValue == 0) {
            return 0;
        }
        return Double.compare(lhsValue, rhsValue);
    }

    private static boolean checkDecimalGreaterThan(DecimalValue lhsValue, DecimalValue rhsValue) {
        switch (lhsValue.valueKind) {
            case POSITIVE_INFINITY:
                return isDecimalRealNumber(rhsValue) || rhsValue.valueKind == DecimalValueKind.NEGATIVE_INFINITY;
            case ZERO:
            case OTHER:
                return rhsValue.valueKind == DecimalValueKind.NEGATIVE_INFINITY || (isDecimalRealNumber(rhsValue) &&
                        lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) > 0);
            default:
                return false;
        }
    }

    private static boolean isDecimalRealNumber(DecimalValue decimalValue) {
        return decimalValue.valueKind == DecimalValueKind.ZERO || decimalValue.valueKind == DecimalValueKind.OTHER;
    }

    private static int compareArrayValues(Object lhsValue, Object rhsValue, int lhsTypeTag, String direction) {
        int lengthVal1;
        int lengthVal2;
        if (lhsTypeTag == TypeTags.ARRAY_TAG) {
            lengthVal1 = ((BArray) lhsValue).size();
            lengthVal2 = ((BArray) rhsValue).size();
        } else {
            lengthVal1 = ((TupleValueImpl) lhsValue).size();
            lengthVal2 = ((TupleValueImpl) rhsValue).size();
        }

        if (lengthVal1 == 0) {
            if (lengthVal2 == 0) {
                return 0;
            }
            return -1;
        }
        if (lengthVal2 == 0) {
            return 1;
        }
        int len = Math.min(lengthVal1, lengthVal2);
        int c = 0;
        for (int i = 0; i < len; i++) {
            if (lhsTypeTag == TypeTags.ARRAY_TAG) {
                c = compareValues(((BArray) lhsValue).get(i), ((BArray) rhsValue).get(i), direction);
            } else {
                c = compareValues(((TupleValueImpl) lhsValue).get(i), ((TupleValueImpl) rhsValue).get(i),
                        direction);
            }
            if (c != 0) {
                break;
            } else {
                if (i == len - 1) {
                    return Long.compare(lengthVal1, lengthVal2);
                }
            }
        }
        return c;
    }
}
