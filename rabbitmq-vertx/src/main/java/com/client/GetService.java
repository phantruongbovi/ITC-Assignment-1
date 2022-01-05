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
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GetService {
    private static final String GETALL_QUEUE_NAME = "my.getall";
    private static final String GETONE_QUEUE_NAME = "my.getone";
    private Gson gson = new Gson();

    public void getAllCar(Channel channel, RabbitMQClient client) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();
        BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        client.basicPublish("", GETALL_QUEUE_NAME, properties, Buffer.buffer(), message -> {
            if (message.succeeded()) {
                System.out.println("Sending request get all cars succeed with id " + corrId);
            } else {
                System.out.println("Sending request get all cars failed!");
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
        Type mapType = new TypeToken<Map<Integer, Car>>(){}.getType();
        Map<Integer, Car> cars = gson.fromJson(result, mapType);
        System.out.println("Cars: " + cars);
        System.out.println("Total cars: " + cars.size());
        System.out.println("---------------------------");

    }

    public void getOneCar(Channel channel, RabbitMQClient client) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);
        String idxCar;

        System.out.print("Chọn id car muốn lấy: ");
        idxCar = sc.nextLine();

        String replyQueueName = channel.queueDeclare().getQueue();
        BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        client.basicPublish("", GETONE_QUEUE_NAME, properties, Buffer.buffer(idxCar, "UTF-8"), message -> {
            if (message.succeeded()) {
                System.out.println("Sending request get one car succeed with id " + corrId);
            } else {
                System.out.println("Sending request get one car failed!");
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
        Car car = null;
        //Type mapType = new TypeToken<Map<Car>>(){}.getType();
        try {
            car = gson.fromJson(result, Car.class);
        } catch (Exception e){

        }
        finally {
            if(car == null){
                System.out.println("Không tồn tại car!");
            }
            else{
                System.out.println("ID Car: " + car.getId());
                System.out.println("Name: " + car.getName());
                System.out.println("Age: " + car.getAge());
            }
            System.out.println("---------------------------");
        }


    }
}
