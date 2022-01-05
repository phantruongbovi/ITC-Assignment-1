package com.client;

import com.model.Car;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DeleteService {
    private static final String DELETE_QUEUE_NAME = "my.delete";

    public void deleteCar(Channel channel, RabbitMQClient client) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);
        String idxCar;

        System.out.print("Chọn id car muốn xóa: ");
        idxCar = sc.nextLine();

        String replyQueueName = channel.queueDeclare().getQueue();
        BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        client.basicPublish("", DELETE_QUEUE_NAME, properties, Buffer.buffer(idxCar, "UTF-8"), message -> {
            if (message.succeeded()) {
                System.out.println("Sending request delete one car succeed with id " + corrId);
            } else {
                System.out.println("Sending request delete one car failed!");
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
        System.out.println("Xóa thành công xe");
        System.out.println("---------------------------");

    }
}
