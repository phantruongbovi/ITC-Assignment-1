package com.server;

import com.google.gson.Gson;
import com.model.Car;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;

import java.util.Map;

public class UpdateService {
    private Gson gson = new Gson();

    public void updateCar(Map<String, Car> cars, RabbitMQMessage message, RabbitMQClient server) {
        try{
            System.out.println("Client " + message.properties().getCorrelationId() + " call update one car");
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
                cars.put(carIdx, new Car(carIdx, infoCar.getAge(), infoCar.getName()));
                response = Buffer.buffer("Đã update thành công id car " + carIdx);
            }
            else {
                response = Buffer.buffer("Không tồn tại id car cần update!");
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
