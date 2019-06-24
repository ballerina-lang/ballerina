package org.ballerinalang.net.http.compiler;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_RESOURCE_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.ANN_RESOURCE_ATTR_BODY;
import static org.ballerinalang.net.http.HttpConstants.ANN_RESOURCE_ATTR_PATH;
import static org.ballerinalang.net.http.HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH;
import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpConstants.HTTP_LISTENER_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;

/**
 * A utility class for validating an HTTP resource signature at compile time.
 *
 * @since 0.965.0
 */
public class ResourceSignatureValidator {

    public static final int COMPULSORY_PARAM_COUNT = 2;

    private static final String ENDPOINT_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + HTTP_LISTENER_ENDPOINT;
    private static final String CALLER_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + CALLER;
    private static final String HTTP_REQUEST_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + REQUEST;

    public static void validate(List<BLangSimpleVariable> signatureParams, DiagnosticLog dlog, DiagnosticPos pos) {
        final int nParams = signatureParams.size();

        if (nParams < COMPULSORY_PARAM_COUNT) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "resource signature parameter count should be >= 2");
            return;
        }

        if (!isValidResourceParam(signatureParams.get(0), CALLER_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "first parameter should be of type " + CALLER_TYPE);
            return;
        }

        if (!isValidResourceParam(signatureParams.get(1), HTTP_REQUEST_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "second parameter should be of type " + HTTP_REQUEST_TYPE);
        }
    }

    private static boolean isValidResourceParam(BLangSimpleVariable param, String expectedType) {
        return expectedType.equals(param.type.toString());
    }

    @SuppressWarnings("unchecked")
    static void validateAnnotation(FunctionNode resourceNode, DiagnosticLog dlog) {
        List<AnnotationAttachmentNode> annotations =
                (List<AnnotationAttachmentNode>) resourceNode.getAnnotationAttachments();
        List<BLangRecordLiteral.BLangRecordKeyValue> annVals = new ArrayList<>();
        List<String> paramSegments = new ArrayList<>();
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ANN_NAME_RESOURCE_CONFIG) &&
                    annotation.getExpression() != null) {
                annVals = ((BLangRecordLiteral) annotation.getExpression()).keyValuePairs;
                count++;
            }
        }
        if (count > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               "There cannot be more than one resource annotations");
        } else if (count == 0) {
            return;
        }
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : annVals) {
            if (checkMatchingConfigKey(keyValue, ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE)) {
                if (annVals.size() > 1) {
                    dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                                       "Invalid configurations for WebSocket upgrade resource");
                } else {
                    List<BLangRecordLiteral.BLangRecordKeyValue> upgradeFields =
                            ((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs;
                    if (upgradeFields.size() == 1) {
                        if (!((BLangSimpleVarRef) upgradeFields.get(
                                0).key.expr).variableName.getValue().equals("upgradeService")) {
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                                               "An upgradeService need to be specified for the WebSocket upgrade " +
                                                       "resource");
                        }
                    } else if (upgradeFields.isEmpty()) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                                           "An upgradeService need to be specified for the WebSocket upgrade " +
                                                   "resource");
                    } else {
                        // Websocket upgrade path validation
                        for (BLangRecordLiteral.BLangRecordKeyValue upgradeField : upgradeFields) {
                            if (checkMatchingConfigKey(upgradeField, ANN_WEBSOCKET_ATTR_UPGRADE_PATH)) {
                                DiagnosticPos position = upgradeField.getValue().getPosition();
                                String[] segments = upgradeField.getValue().toString().split("/");
                                for (String segment : segments) {
                                    validatePathSegment(segment, position, dlog, paramSegments);
                                }
                            }
                        }
                    }
                }
            }

            // Resource config path validation
            if (checkMatchingConfigKey(keyValue, ANN_RESOURCE_ATTR_PATH)) {
                DiagnosticPos position = keyValue.getValue().getPosition();
                String[] segments = keyValue.getValue().toString().split("/");
                for (String segment : segments) {
                    validatePathSegment(segment, position, dlog, paramSegments);
                }
            }

            // Resource config data binding param validation
            if (checkMatchingConfigKey(keyValue, ANN_RESOURCE_ATTR_BODY)) {
                List<? extends SimpleVariableNode> parameters = resourceNode.getParameters();
                String bodyFieldValue = keyValue.getValue().toString();
                // Data binding param should be placed as the last signature param
                String signatureBodyParam = parameters.get(parameters.size() - 1).getName().getValue();
                if (bodyFieldValue.isEmpty()) {
                    dlog.logDiagnostic(Diagnostic.Kind.ERROR, keyValue.getValue().getPosition(),
                                       "Empty data binding param value");

                } else if (!signatureBodyParam.equals(bodyFieldValue)) {
                    dlog.logDiagnostic(Diagnostic.Kind.ERROR, keyValue.getValue().getPosition(),
                                       "Invalid data binding param in the signature : expected '" +
                                               bodyFieldValue + "' as param name, but found '" +
                                               signatureBodyParam + "' in the resource signature");
                }
                paramSegments.add(bodyFieldValue);
            }

        }

        // Validate path param names and signature - signature params should be a subset of path and body params
        List<? extends SimpleVariableNode> signatureParams = resourceNode.getParameters().subList(
                COMPULSORY_PARAM_COUNT, resourceNode.getParameters().size());
        if (!signatureParams.stream().allMatch(signatureParam -> paramSegments.stream()
                .anyMatch(parameter -> signatureParam.getName().getValue().equals(parameter)))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               "Mismatching path param(s) in the resource signature");
        }
    }

    private static void validatePathSegment(String segment, DiagnosticPos pos, DiagnosticLog dlog,
                                            List<String> pathParamSegments) {
        boolean expression = false;
        int startIndex = 0;
        int maxIndex = segment.length() - 1;

        for (int pointerIndex = 0; pointerIndex < segment.length(); pointerIndex++) {
            char ch = segment.charAt(pointerIndex);
            switch (ch) {
                case '{':
                    if (expression) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Illegal open brace character in resource path config");
                        break;
                    }
                    if (pointerIndex + 1 >= maxIndex) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Invalid param expression in resource path config");
                    }
                    if (pointerIndex != startIndex) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "Illegal expression in resource path config");
                    }
                    expression = true;
                    startIndex++;
                    break;
                case '}':
                    if (!expression) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Illegal closing brace detected in resource path config");
                        break;
                    }
                    if (pointerIndex <= startIndex) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Illegal empty expression in resource path config");
                    }
                    pathParamSegments.add(segment.substring(startIndex, pointerIndex));
                    expression = false;
                    startIndex = pointerIndex + 1;
                    break;
                default:
                    if (pointerIndex == maxIndex) {
                        if (expression) {
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "Incomplete path param expression");
                            break;
                        }
                        if (startIndex != 0 && pointerIndex == startIndex) {
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                               "Illegal expression in resource path config");
                        }
                    }
            }
        }
    }

    private ResourceSignatureValidator() {
    }

    static void validateResourceReturnType(boolean resourceReturnsErrorOrNil, DiagnosticLog dlog, DiagnosticPos pos) {
        if (!resourceReturnsErrorOrNil) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "invalid return type: expected error?");
        }
    }

    private static boolean checkMatchingConfigKey(BLangRecordLiteral.BLangRecordKeyValue keyValue, String key) {
        return ((BLangSimpleVarRef) (keyValue.key).expr).variableName.getValue().equals(key);
    }
}

