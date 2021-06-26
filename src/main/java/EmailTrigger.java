/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author abhishek
 */
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import java.util.UUID;
import java.time.Instant;

public class EmailTrigger implements RequestHandler<SNSEvent, Object> {

    static DynamoDB dynamoDB;

    @Override
    public Object handleRequest(SNSEvent request, Context context) {
        String domain = System.getenv("Domain");
        final String FROM = "no-reply@" + domain;
        final String TO = request.getRecords().get(0).getSNS().getMessage();

        try {
            init();
            Table table = dynamoDB.getTable("csye6225");
            long tokenExpiry = Instant.now().getEpochSecond() + 15 * 60;
            long invocationTime = Instant.now().getEpochSecond();
            if (table == null) {
                context.getLogger().log("Dynamo Table Not Found!!!");
            } else {
                Item item = table.getItem("id", request.getRecords().get(0).getSNS().getMessage());
                if (item == null || (item != null && Long.parseLong(item.get("expiryTime").toString()) < invocationTime)) {
                    String token = UUID.randomUUID().toString();
                    Item itemPut = new Item()
                            .withPrimaryKey("id", request.getRecords().get(0).getSNS().getMessage())
                            .withString("UniqueToken", token)
                            .withNumber("expiryTime", tokenExpiry);
                    table.putItem(itemPut);
                    context.getLogger().log("Reset Email ID:" + request.getRecords().get(0).getSNS().getMessageId());
                    AmazonSimpleEmailService client
                            = AmazonSimpleEmailServiceClientBuilder.standard()
                                    .withRegion(Regions.US_EAST_1).build();
                    SendEmailRequest req = new SendEmailRequest().withDestination(new Destination().withToAddresses(TO)).withMessage(new Message()
                            .withBody(new Body().withHtml(new Content().withCharset("UTF-8")
                                    .withData("Hi,<br/><br/>" + "Please click on the below link to reset your password<br/>"
                                            + "<p><a href='#'>http://" + domain + "/reset?email=" + TO + "&token=" + token + "</a></p><br/><br/>" + "Regards,<br/>Online Book Store")))
                            .withSubject(new Content().withCharset("UTF-8")
                                    .withData("Password Reset Link - Online Book Store")))
                            .withSource(FROM);
                    SendEmailResult response = client.sendEmail(req);
                    context.getLogger().log("Email Successfully sent!!!");
                } else {
                    context.getLogger().log(item.toJSON() + "Email Already sent!!!");
                }
            }
        } catch (Exception ex) {
            context.getLogger().log("Error message: " + ex.getMessage());
        }

        return null;
    }

    private static void init() throws Exception {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        dynamoDB = new DynamoDB(client);
    }

}
