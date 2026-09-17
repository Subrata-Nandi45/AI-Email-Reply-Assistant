package com.email.writer.service;

import com.email.writer.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailGeneratorService
{
    private final WebClient webClient;
    private final String apiKey;
    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey
                                 )
    {

        this.apiKey = geminiApiKey;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }



   public String generateEmailReply(EmailRequest emailRequest)
   {

       //build prompt
       String prompt=buildPrompt(emailRequest);
       //prepare raw json body
       String requestBody=String.format("""
               {
                   "model": "gemini-3.8-flash",
                   "input": "%s"
                 }""",prompt);
       //send request



       //extract response

   }

    private String buildPrompt(EmailRequest emailRequest)
    {
        //prompt can be modified further that's why stringbuilder
        StringBuilder prompt=new StringBuilder();
        prompt.append("Generate a professional email reply for the following email:");
        if(emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty())
        {
            prompt.append("Use a").append(emailRequest.getTone()).append(" tone.");
            //Use a professional/casula/frienldy tone
        }
        prompt.append("Original Email: \n").append(emailRequest.getEmailContent());

        return prompt.toString();


    }
}
