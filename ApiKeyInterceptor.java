package com.mycompany.smarthome;

import io.grpc.*;

public class ApiKeyInterceptor implements ServerInterceptor {

    // 1. Define the metadata key for the API key
    private static final Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);

    // 2. Set your valid API key (in a real app, load from config or env)
    private static final String VALID_API_KEY = "my-secret-key";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // 3. Extract the API key from headers
        String apiKey = headers.get(API_KEY_HEADER);

        // 4. Check if the key is missing
        if (apiKey == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("API key is missing"), new Metadata());
            return new ServerCall.Listener<ReqT>() {}; // Do nothing
        }

        // 5. Check if the key is invalid
        if (!apiKey.equals(VALID_API_KEY)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        // 6. Valid key — continue processing the request
        return next.startCall(call, headers);
    }
}
