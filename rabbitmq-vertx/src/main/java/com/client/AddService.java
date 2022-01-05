package com.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.model.Car;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AddService {
    private static final String ADD_QUEUE_NAME = "my.add";
    Gson gson = new Gson();

    public void addCar(Channel channel, RabbitMQClient client) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);
        String idCar, nameCar;
        int ageCar;
        String replyQueueName = channel.queueDeclare().getQueue();
        BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        System.out.print("Nhập id car: ");
        idCar = sc.nextLine();
        System.out.print("Nhập name car: ");
        nameCar = sc.nextLine();
        System.out.print("Nhập age car: ");
        ageCar = sc.nextInt();
        Car newCar = new Car(idCar, ageCar, nameCar);
        String json = gson.toJson(newCar);
        Buffer request = Buffer.buffer(json);

        client.basicPublish("", ADD_QUEUE_NAME, properties, request, message -> {
            if (message.succeeded()) {
                System.out.println("Sending request add car succeed with id " + corrId);
            } else {
                System.out.println("Sending request add cars failed!");
            }
        });

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        channel.basicCancel(ctag);
        System.out.println(result);
        System.out.println("---------------------------");
    }
}
