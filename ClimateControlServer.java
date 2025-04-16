package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class ClimateControlServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new ClimateControlServiceImpl())
                .build();

        System.out.println("🌡️ ClimateControlServer running on port 50051");
        server.start();
        server.awaitTermination();
    }

    static class ClimateControlServiceImpl extends ClimateControlServiceGrpc.ClimateControlServiceImplBase {

        @Override
        public void setTemperature(TemperatureRequest request, StreamObserver<TemperatureResponse> responseObserver) {
            System.out.println("🌍 Request to set temperature in " + request.getRoom() + " to " + request.getTemperature() + "°C");

            TemperatureResponse response = TemperatureResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("✅ Temperature set to " + request.getTemperature() + "°C in " + request.getRoom())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
