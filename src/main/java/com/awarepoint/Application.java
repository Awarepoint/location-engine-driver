package com.awarepoint;

import com.awarepoint.ble.server.api.ServerDevicePacket;
import com.awarepoint.locationengine.configuration.domain.full.FullConfiguration;
import com.awarepoint.locationengine.configuration.domain.full.FullConfigurationRequest;
import com.awarepoint.locationengine.outputlocation.api.application.TagLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Collections;

@SpringBootApplication
public class Application  implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws InterruptedException, SQLException, IOException {

        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws IOException, InterruptedException {
        waitForFullConfigurationRequest();
        sendConfigs();
        sendWifiBlink();
        waitForLocationOutput();
    }

    private void waitForFullConfigurationRequest() {
        try(
                ServerSocket serverSocket = new ServerSocket(1134);
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(in);
                ) {
            FullConfigurationRequest configurationRequest = (FullConfigurationRequest) ois.readObject();
            logger.trace("got full config request");
        } catch (IOException | ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    private void sendConfigs() {
        try(
                Socket socket = new Socket("localhost", 1135);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            //TODO add real configurations
            FullConfiguration fullConfiguration = new FullConfiguration(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
            out.writeObject(fullConfiguration);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void sendWifiBlink() {
        try(
                Socket socket = new Socket("localhost", 1132);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            // TODO sample server device packet, add non-empty beacon rssi readings
            ServerDevicePacket serverDevicePacket= new ServerDevicePacket(System.currentTimeMillis(), 1, Collections.emptyList(), false, false, false, false, 1, 0.0, 0.0, 0.0);
            out.writeObject(serverDevicePacket);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void waitForLocationOutput() {
        try(
                ServerSocket serverSocket = new ServerSocket(1133);
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(in);
        ) {
            TagLocation tagLocation = (TagLocation) ois.readObject();
            logger.trace("got tag location: {}", tagLocation);
        } catch (IOException | ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }
}
