package com.mycompany.smarthome;
import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class EnergyRoutineServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50052)
                .addService(new EnergyRoutineServiceImpl())
                .build();

        System.out.println("⚡ EnergyRoutineServer running on port 50052");
        server.start();
        server.awaitTermination();
    }

    static class EnergyRoutineServiceImpl extends EnergyRoutineServiceGrpc.EnergyRoutineServiceImplBase {

        @Override
        public void streamHourlyEnergyUsage(EnergyUsageRequest request, StreamObserver<EnergyUsageData> responseObserver) {
            System.out.println("📩 Received energy usage request for date: " + request.getDate());

            // Simulate hourly energy data streaming
            for (int hour = 0; hour < 24; hour++) {
                EnergyUsageData usage = EnergyUsageData.newBuilder()
                        .setHour(hour)
                        .setUsageKwh(0.5 + Math.random()) // simulate 0.5–1.5 kWh usage
                        .build();

                System.out.println("⏱️ Sending data for hour " + hour + ": " + usage.getUsageKwh() + " kWh");
                responseObserver.onNext(usage);

                try {
                    Thread.sleep(100); // small delay to simulate streaming
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            responseObserver.onCompleted();
            System.out.println("✅ Finished streaming data for " + request.getDate());
        }
    }
}
