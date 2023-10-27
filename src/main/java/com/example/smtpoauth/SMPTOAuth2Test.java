package com.example.smtpoauth;

import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SMPTOAuth2Test {

    public static void main(String[] args) {

        Options options = new Options();
        options.addRequiredOption("c", "clientid", true, "client id");
        options.addRequiredOption("s", "clientSecret", true, "client secret");
        options.addRequiredOption("t", "tenant", true, "tenant id");
        options.addRequiredOption("e", "email", true, "email");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String mailboxUserName = cmd.getOptionValue("e");
            System.out.println("Trying to access mailbox for " + mailboxUserName);
            
            String protocol = "smtp";
            Properties props = new Properties();
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.host", "outlook.office365.com");
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.auth", "true");

            props.put("mail.debug", "true");
            props.put("mail.debug.auth", "true");

            Session session = Session.getInstance(props);
            session.setDebug(true);
            System.out.println("Trying to access SMTP mailbox with properties ");
            props.forEach((k, v) -> System.out.println(k + "=" + v));

            Transport transport = session.getTransport(protocol);
            transport.connect("outlook.office365.com", mailboxUserName, getAuthToken(cmd.getOptionValue("c"), cmd.getOptionValue("s"), cmd.getOptionValue("t")));
            transport.close();
            System.out.println("success");

        } catch (ParseException e) {
            System.out.println(
                    "Wrong arguments. Usage: SMPTOAuth2Test -c <clientId> -s <clientSecret> -t <AAD tenant ID> -e <email account>");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static String getAuthToken(String clientid, String clientSecret, String tenantId) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost loginPost = new HttpPost(
                    "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token");
            String scopes = "https://outlook.office365.com/.default";
            String encodedBody = "client_id=" + clientid
                    + "&scope=" + scopes
                    + "&client_secret=" + clientSecret
                    + "&grant_type=client_credentials";

            loginPost.setEntity(new StringEntity(encodedBody, ContentType.APPLICATION_FORM_URLENCODED));

            loginPost.addHeader(new BasicHeader("cache-control", "no-cache"));
            CloseableHttpResponse loginResponse = client.execute(loginPost);
            byte[] response = loginResponse.getEntity().getContent().readAllBytes();
            ObjectMapper objectMapper = new ObjectMapper();
            JavaType type = objectMapper.constructType(objectMapper.getTypeFactory()
                    .constructParametricType(Map.class, String.class, String.class));
            Map<String, String> parsed = new ObjectMapper().readValue(response, type);
            return parsed.get("access_token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}