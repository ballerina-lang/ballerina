package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartBDataSource;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.ballerinalang.mime.util.HeaderUtil.isMultipart;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeUtil.getContentTypeWithParameters;
import static org.ballerinalang.mime.util.MimeUtil.getNewMultipartDelimiter;

/**
 * 'getBodyPartsAsChannel' extern function converts a set of body parts into a byte channel.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getBodyPartsAsChannel",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetBodyPartsAsChannel extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(GetBodyPartsAsChannel.class);

    @Override
    public void execute(Context context) {
        try {
            BMap<String, BValue> entityStruct = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            String contentType = getContentTypeWithParameters(entityStruct);
            if (isMultipart(contentType)) {
                BString boundaryValue = HeaderUtil.extractBoundaryBParameter(contentType);
                String multipartDataBoundary = boundaryValue != null ? boundaryValue.toString() :
                        getNewMultipartDelimiter();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MultipartBDataSource multipartDataSource = new MultipartBDataSource(entityStruct,
                                                                                    multipartDataBoundary);
                multipartDataSource.serialize(outputStream);
                EntityBodyChannel entityBodyChannel = new EntityBodyChannel(new ByteArrayInputStream(
                        outputStream.toByteArray()));
                BMap<String, BValue> byteChannelStruct = BLangConnectorSPIUtil.createBStruct(context,
                                                                 IOConstants.IO_PACKAGE, READABLE_BYTE_CHANNEL_STRUCT);
                byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new EntityWrapper(entityBodyChannel));
                context.setReturnValues(byteChannelStruct);
            } else {
                context.setReturnValues(MimeUtil.createError(context, "Entity doesn't contain body parts"));
            }
        } catch (Throwable err) {
            log.error("Error occurred while constructing a byte channel out of body parts", err);
            context.setReturnValues(MimeUtil.createError(context, "Error occurred while constructing a byte " +
                    "channel out of body parts : " + err.getMessage()));
        }
    }

    public static Object getBodyPartsAsChannel(Strand strand, ObjectValue entityObj) {
        try {
            String contentType = getContentTypeWithParameters(entityObj);
            if (isMultipart(contentType)) {
                String boundaryValue = HeaderUtil.extractBoundaryParameter(contentType);
                String multipartDataBoundary = boundaryValue != null ? boundaryValue : getNewMultipartDelimiter();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MultipartDataSource multipartDataSource = new MultipartDataSource(entityObj, multipartDataBoundary);
                multipartDataSource.serialize(outputStream);
                EntityBodyChannel entityBodyChannel = new EntityBodyChannel(new ByteArrayInputStream(
                        outputStream.toByteArray()));
                ObjectValue byteChannelObj = BallerinaValues.createObjectValue(IOConstants.IO_PACKAGE,
                                                                               READABLE_BYTE_CHANNEL_STRUCT);
                byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new EntityWrapper(entityBodyChannel));
                return byteChannelObj;
            } else {
                return MimeUtil.createError("Entity doesn't contain body parts");
            }
        } catch (Throwable err) {
            log.error("Error occurred while constructing a byte channel out of body parts", err);
            return MimeUtil.createError("Error occurred while constructing a byte " +
                    "channel out of body parts : " + err.getMessage());
        }
    }
}
