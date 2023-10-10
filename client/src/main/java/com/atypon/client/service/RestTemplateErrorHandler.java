package com.atypon.client.service;

import com.atypon.client.exception.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ExtractingResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestTemplateErrorHandler implements ResponseErrorHandler {
    //    @Autowired
    private final DefaultResponseErrorHandler defaultResponseErrorHandler = new ExtractingResponseErrorHandler();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return !response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode() != HttpStatus.BAD_REQUEST ) {
            defaultResponseErrorHandler.handleError(response);
            return;
        }
        JSONObject object;
        try {
            object = (JSONObject) new JSONParser()
                    .parse(
                            new String(response.getBody().readAllBytes(),
                                    StandardCharsets.UTF_8)
                    );
        } catch (ParseException e) {
            System.out.println();
            throw new NodeServerException(e.getMessage());
        }
        String type = (String) object.get("type");
        String message = (String) object.get("message");
        if (type.contains("Database"))
            throw new DatabaseException(message);
        else if (type.contains("Document"))
            throw new DocumentException(message);
        else if (type.contains("Collection"))
            throw new CollectionException(message);
        else if (response.getStatusCode().is4xxClientError()) {
            System.out.println("response");
        } else
            defaultResponseErrorHandler.handleError(response);

    }

}