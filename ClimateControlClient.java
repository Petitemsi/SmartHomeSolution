package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class ClimateControlClient {
    public static void main(String[] args) {
        try {
            // Step 1: Discover the service using JmDNS
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
            ServiceInfo serviceInfo = jmdns.getServiceInfo("_climatecontrol._tcp.local.", "ClimateControlService", 5000);
            
            if (serviceInfo == null) {
                System.err.println("❌ Could not find ClimateControlService via JmDNS.");
                return;
            }

            String host = serviceInfo.getHostAddresses()[0];
            int port = serviceInfo.getPort();

            System.out.println("🔍 Found ClimateControlService at " + host + ":" + port);

            // Step 2: Create gRPC channel
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .build();

            // Step 3: Attach API key
            Metadata headers = new Metadata();
            Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(API_KEY_HEADER, "my-secret-key");

            ClimateControlServiceGrpc.ClimateControlServiceBlockingStub stub =
                    MetadataUtils.attachHeaders(ClimateControlServiceGrpc.newBlockingStub(channel), headers);

            // Step 4: Send request
            TemperatureRequest request = TemperatureRequest.newBuilder()
                    .setRoom("Living Room")
                    .setTemperature(22.5)
                    .build();

            TemperatureResponse response = stub.setTemperature(request);
            System.out.println("🌡️ Climate Response: " + response.getMessage());

            channel.shutdown();
            jmdns.close();

        } catch (IOException e) {
            System.err.println("❌ JmDNS error: " + e.getMessage());
        }
    }
}
