package org.ballerinalang.net.mime.nativeimpl.mimebase64;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Mime decoder to decode string values.
 */
@BallerinaFunction(packageName = "ballerina.net.mime",
                   functionName = "decodeString",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "MimeBase64Decoder",
                                        structPackage = "ballerina.net.mime"),
                   args = {
                           @Argument(name = "content",
                                     type = TypeKind.STRING), @Argument(name = "charset",
                                                                        type = TypeKind.STRING)
                   },
                   returnType = { @ReturnType(type = TypeKind.STRING) },
                   isPublic = true)
public class DecodeString extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        String encodedContent = this.getStringArgument(context, 0);
        String charset = this.getStringArgument(context, 1);
        byte[] decodedBytes;
        String decodedContent = "";
        try {
            if (charset != null) {
                decodedBytes = Base64.getMimeDecoder().decode(encodedContent.getBytes(charset));
                decodedContent = new String(decodedBytes, charset);
            } else {
                decodedBytes = Base64.getMimeDecoder().decode(encodedContent.getBytes(StandardCharsets.UTF_8));
                decodedContent = new String(decodedBytes, StandardCharsets.UTF_8);
            }
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Error occured while decoding mime string: " + e.getMessage());
        }
        return getBValues(new BString(decodedContent));
    }
}
