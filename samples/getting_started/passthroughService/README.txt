Description
===========
This sample will passthrough the message to the backend and response to the client


How to run this sample
======================
bin$ ./ballerinaserver.sh ../samples/passthroughService/passthroughService.bal

the above command will start the ballerina server in the current terminal and deploy the passthroughService.bal file and publish the 'passthroughService' and 'nyseStockQuoteService' services
Here the 'nyseStockQuoteService' will act as a backend.

Invoking the service
====================
curl -v http://localhost:9090/passthrough
