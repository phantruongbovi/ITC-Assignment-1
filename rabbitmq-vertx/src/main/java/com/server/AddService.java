package com.server;

import com.google.gson.Gson;
import com.model.Car;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;

import java.util.Map;

public class AddService {
    private Gson gson = new Gson();

    public void addCar(Map<String, Car> cars, RabbitMQMessage message, RabbitMQClient server) {
        try{
            System.out.println("Client " + message.properties().getCorrelationId() + " call add one car");
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        finally {
            Car infoCar = gson.fromJson(message.body().toString(), Car.class);
            String carIdx = infoCar.getId();
            Buffer response = null;
            BasicProperties properties = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(message.properties().getCorrelationId())
                    .build();
            if (cars.containsKey(carIdx)){
                response = Buffer.buffer("Tồn tại id " + carIdx + ", thêm thất bại !");
            }
            else {
                cars.put(carIdx, new Car(carIdx, infoCar.getAge(), infoCar.getName()));
                response = Buffer.buffer("Thêm thành công!");
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
