package com.mycompany.smarthome;

import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public class JwtServerInterceptor implements ServerInterceptor {
    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String token = headers.get(AUTH_HEADER);

        if (token == null || !token.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid token"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        try {
            String jwt = token.substring(7); // remove "Bearer "
            Jws<Claims> claims = JwtUtil.validateToken(jwt);
            return next.startCall(call, headers);
        } catch (Exception e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT token"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
    }
}
