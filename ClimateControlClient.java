
package com.mycompany.smarthome;
import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClimateControlClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        ClimateControlServiceGrpc.ClimateControlServiceBlockingStub stub =
                ClimateControlServiceGrpc.newBlockingStub(channel);

        TemperatureRequest request = TemperatureRequest.newBuilder()
                .setRoom("Bedroom")
                .setTemperature(21)
                .build();

        TemperatureResponse response = stub.setTemperature(request);
        System.out.println("🛰️ Server response: " + response.getMessage());

        channel.shutdown();
    }
}
