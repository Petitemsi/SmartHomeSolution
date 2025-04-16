package com.mycompany.smarthome;
import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlantSensorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50053)
                .addService(new PlantSensorServiceImpl())
                .build();

        System.out.println("🌿 PlantSensorServer running on port 50053");
        server.start();
        server.awaitTermination();
    }

    static class PlantSensorServiceImpl extends PlantSensorServiceGrpc.PlantSensorServiceImplBase {
        @Override
        public StreamObserver<PlantSensorReading> sendSensorReadings(StreamObserver<PlantSensorSummary> responseObserver) {
            return new StreamObserver<PlantSensorReading>() {
                List<PlantSensorReading> readings = new ArrayList<>();

                @Override
                public void onNext(PlantSensorReading value) {
                    System.out.printf("📥 Received Reading: Moisture=%.2f%%, Light=%.2f lumens%n",
                            value.getMoisture(), value.getLight());
                    readings.add(value);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("❌ Error receiving sensor readings");
                }

                @Override
                public void onCompleted() {
                    double totalMoisture = 0;
                    double totalLight = 0;

                    for (PlantSensorReading r : readings) {
                        totalMoisture += r.getMoisture();
                        totalLight += r.getLight();
                    }

                    int count = readings.size();

                    PlantSensorSummary summary = PlantSensorSummary.newBuilder()
                            .setTotalReadings(count)
                            .setAverageMoisture(count == 0 ? 0 : totalMoisture / count)
                            .setAverageLight(count == 0 ? 0 : totalLight / count)
                            .build();

                    System.out.println("✅ Sending summary to client...");
                    responseObserver.onNext(summary);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
