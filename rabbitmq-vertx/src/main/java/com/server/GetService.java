package com.server;

import com.google.gson.Gson;
import com.model.Car;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class GetService {
    private Gson gsonCar = new Gson();

    public void getAllElement(Map<String, Car> cars, RabbitMQMessage message, RabbitMQClient server) throws UnsupportedEncodingException {
        try{
            System.out.println("Client " + message.properties().getCorrelationId() + " call get all");
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        finally {

            String json = gsonCar.toJson(cars);
            Buffer response = Buffer.buffer(json);
            BasicProperties properties = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(message.properties().getCorrelationId())
                    .build();
            server.basicPublish("", message.properties().getReplyTo(), properties,response, pubResult -> {
                if(pubResult.succeeded()){
                    System.out.println("Message published to queue " + message.properties().getReplyTo() + " !");
                }
                else{
                    pubResult.cause().printStackTrace();
                }
            });
        }
    }

    public void getOneElement(Map<String, Car> cars, RabbitMQMessage message, RabbitMQClient server) {
        try{
            System.out.println("Client " + message.properties().getCorrelationId() + " call get one car");
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
                String json = gsonCar.toJson(cars.get(carIdx));
                response = Buffer.buffer(json);
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
