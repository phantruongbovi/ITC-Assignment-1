package com.server;

import com.google.gson.Gson;
import com.model.Car;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;

import java.util.Map;

public class DeleteService {
    private Gson gsonCar = new Gson();

    public static void deleteCar(Map<String, Car> cars, RabbitMQMessage message, RabbitMQClient server) {
        try{
            System.out.println("Client " + message.properties().getCorrelationId() + " call delete one car");
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        finally {
            String carIdx = message.body().toString("UTF-8");
            Buffer response = null;
            BasicProperties properties = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(message.properties().getCorrelationId())
                    .build();
            if (cars.containsKey(carIdx)){
                cars.remove(carIdx);
                response = Buffer.buffer("Đã xóa thành công id " + carIdx + " !");
            }
            else {
                response = Buffer.buffer("Không tồn tại car id!");
            }
            server.basicPublish("", message.properties().getReplyTo(), properties, response, pubResult -> {
                if(pubResult.succeeded()){
                    System.out.println("Message published to queue " + message.properties().getReplyTo() + " !");
                }
                else{
                    pubResult.cause().printStackTrace();
                }
            });
        }
    }
}
