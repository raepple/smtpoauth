Simple Java test client for SMTP-based access to M365 Exchange Online with SASL XOAUTH2 authentication

## Prerequisites
- Follow this [documentation](https://learn.microsoft.com/en-us/exchange/client-developer/legacy-protocols/how-to-authenticate-an-imap-pop-smtp-application-by-using-oauth) to setup Entra ID and Exchange Online
- Ensure that your user's mail settings have [SMTP AUTH enabled](https://learn.microsoft.com/en-us/exchange/clients-and-mobile-in-exchange-online/authenticated-client-smtp-submission#enable-smtp-auth-for-specific-mailboxes)

## Build
`mvn clean package`

## Usage
`java -jar .\target\smtpoauth2test-1.0-SNAPSHOT.jar -c <clientId> -s <clientSecret> -t <AAD tenant ID> -e <email account>`