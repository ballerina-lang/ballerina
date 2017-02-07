import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.util;

connector Twitter (string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {

    http:HTTPConnector tweeterEP = create http:HTTPConnector("https://api.twitter.com");

    action tweet(Twitter t, string msg) (message) {

        message request = {};

        string oauthHeader = constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, msg);
        string tweetPath = "/1.1/statuses/update.json?status="+uri:encode(msg);

        message:setHeader(request, "User-Agent", "Ballerina/0.8.0");
        message:setHeader(request, "Authorization", oauthHeader);

        message response = http:HTTPConnector.post(tweeterEP, tweetPath, request);

        return response;
    }

}


function constructOAuthHeader(string consumerKey, string consumerSecret,
                string accessToken, string accessTokenSecret, string tweetMessage) (string) {

    string timeStamp = string:valueOf(system:epochTime());
    string nonceString =  util:getRandomString();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+timeStamp+"&oauth_token="+accessToken+"&oauth_version=1.0&status="+uri:encode(tweetMessage);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret)+"&"+uri:encode(accessTokenSecret);
    string signature = util:getHmac(baseString, keyStr, "SHA1");
    string oauthHeader = "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp +
                                      "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";

    return string:unescape(oauthHeader);
}

function main (string[] args) {

    Twitter twitterConnector = create Twitter(args[0], args[1], args[2], args[3]);

    message tweetResponse = sample:Twitter.tweet(twitterConnector, args[4]);

    json tweetJSONResponse = message:getJsonPayload(tweetResponse);
    system:println(json:toString(tweetJSONResponse));
}
