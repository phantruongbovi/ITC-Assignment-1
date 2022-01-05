package truong.com.server.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import truong.com.server.model.Car;

import java.util.HashMap;

public class CarVerticle extends AbstractVerticle {
    private HashMap cars = new HashMap();
    public void createExampleData(){
        cars.put(1, new Car(1, 20, "Truong"));
        cars.put(2, new Car(2, 21, "Trung"));
        cars.put(3, new Car(3, 22, "Trong"));
    }

    private void getAllCars(RoutingContext routingContext){
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        response.end(Json.encodePrettily(cars.values()));
    }

    private void getOneCar(RoutingContext routingContext){
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        String sid = routingContext.request().getParam("id");
        if(sid == null){
            routingContext.response().setStatusCode(400).end();
        }
        else{
            int id = Integer.parseInt(sid);
            Car carFound = (Car) cars.get(id);
            if (carFound != null){
                response.end(Json.encodePrettily(carFound));
            }
            else{
                response.putHeader("content-type", "text/html");
                response.end("car does not exist");
            }

        }
    }

    private void insertNewCar(RoutingContext routingContext){
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        try{
            Car car = Json.decodeValue(routingContext.getBody(), Car.class);
            boolean checkExist = cars.containsKey(car.getId());
            if (checkExist){
                response.putHeader("content-type", "text/html");
                response.end("car existed");
            }
            else{
                cars.put(car.getId(), car);
                response.end("Added successfully");
            }
        }
        catch (Exception ex){
            response.end(ex.getMessage());
        }
    }

    private void updateCar(RoutingContext routingContext){
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        Car car = Json.decodeValue(routingContext.getBody(), Car.class);
        if(cars.containsKey(car.getId())){
            cars.put(car.getId(), car);
            response.end("Updated successfully");
        }
        else{
            response.end("Updated fail");
        }
    }

    private void deleteCar(RoutingContext routingContext){
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        String sid = routingContext.request().getParam("id");
        int id = Integer.parseInt(sid);
        if(cars.containsKey(id)){
            cars.remove(id);
            response.end("Remove successfully");
        }
        else{
            response.end("Remove fail!");
        }
    }

    @Override
    public void start(Promise startPromise) throws Exception{
        createExampleData();
        Router router = Router.router(vertx);

        router.get("/api/cars").handler(this::getAllCars);
        router.get("/api/cars/:id").handler(this::getOneCar);
        router.route("/api/cars*").handler(BodyHandler.create());
        router.post("/api/cars").handler(this::insertNewCar);
        router.put("/api/cars").handler(this::updateCar);
        router.delete("/api/cars/:id").handler(this::deleteCar);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()){
                                startPromise.complete();
                            }
                            else{
                                startPromise.fail(result.cause());
                            }
                        });
    }
}
