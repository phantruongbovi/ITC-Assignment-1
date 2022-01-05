package com.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.model.Car;
import com.rabbitmq.client.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.rabbitmq.RabbitMQPublisher;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class Client {




    private Channel channel;
    private Connection connection;
    private RabbitMQClient client;
    private Gson gson = new Gson();

    private final GetService getService = new GetService();
    private final DeleteService deleteService = new DeleteService();
    private final AddService addService = new AddService();
    private final UpdateService updateService = new UpdateService();

    public Client() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        RabbitMQOptions config = new RabbitMQOptions();
        config.setHost("localhost");
        client = RabbitMQClient.create(Vertx.vertx(), config);
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("RabbitMQ successfully connected!");
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        Thread.sleep(500);
        client.run();

    }

    private void run() throws InterruptedException, IOException {
        Scanner sc = new Scanner(System.in);
        int chooseOption;
        System.out.println("1. Get all cars" +
                "\n2. Get one car" +
                "\n3. Delete one car" +
                "\n4. Add car" +
                "\n5. Update car" +
                "\n6. Out");
        while(true){
            System.out.print("Choose option: ");
            chooseOption = sc.nextInt();
            System.out.println("---------------------------");
            if(chooseOption == 1){
                getService.getAllCar(channel, client);
            }
            else if(chooseOption == 2){
                getService.getOneCar(channel, client);
            }
            else if(chooseOption == 3){
                deleteService.deleteCar(channel, client);
            }
            else if(chooseOption == 4){
                addService.addCar(channel, client);
            }
            else if(chooseOption == 5){
                updateService.updateCar(channel, client);
            }
            else if(chooseOption == 6){
                System.exit(1);
            }
        }
    }


}
