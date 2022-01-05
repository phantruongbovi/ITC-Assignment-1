package com.server;

import com.server.UpdateService;
import com.server.AddService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.model.Car;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final String GETALL_QUEUE_NAME = "my.getall";
    private static final String GETONE_QUEUE_NAME = "my.getone";
    private static final String DELETE_QUEUE_NAME = "my.delete";
    private static final String UPDATE_QUEUE_NAME = "my.update";
    private static final String ADD_QUEUE_NAME = "my.add";

    private RabbitMQClient server;
    private Gson gsonCar = new Gson();
    private final Map<String, Car> cars = new HashMap<String, Car>();
    private final GetService getService = new GetService();
    private final DeleteService deleteService = new DeleteService();
    private final AddService addService = new AddService();
    private final UpdateService updateService = new UpdateService();

    public Server(){
        RabbitMQOptions config = new RabbitMQOptions();
        config.setHost("localhost");
        server = RabbitMQClient.create(Vertx.vertx(), config);
        server.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("Server now running!");
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
    }

    private void createExampleData(){
        cars.put("0", new Car("0", 21, "Truong"));
        cars.put("1", new Car("1", 21, "Trung"));
        cars.put("2", new Car("2", 22, "Trong"));
        String json = gsonCar.toJson(cars);
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.createExampleData();
        Thread.sleep(1000);
        server.run();
    }

    private void run(){
        // GET ALL : my.getall
        server.basicConsumer(GETALL_QUEUE_NAME, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer get all car created!");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    try {
                        System.out.println("---------------------------");
                        getService.getAllElement(cars, message, server);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });

        // GET ONE ELEMENT : my.getone
        server.basicConsumer(GETONE_QUEUE_NAME, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer get one car created!");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    System.out.println("---------------------------");
                    getService.getOneElement(cars, message, server);
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });

        // DELETE ELEMENT : my.delete
        server.basicConsumer(DELETE_QUEUE_NAME, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer delete car created!");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    System.out.println("---------------------------");
                    deleteService.deleteCar(cars, message, server);
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });

        // INSERT ELEMENT: my.insert
        server.basicConsumer(ADD_QUEUE_NAME, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer add car created!");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    System.out.println("---------------------------");
                    addService.addCar(cars, message, server);
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });


        // UPDATE ELEMENT : my.update
        server.basicConsumer(UPDATE_QUEUE_NAME, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer update car created!");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    System.out.println("---------------------------");
                    updateService.updateCar(cars, message, server);
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });
    }
}
