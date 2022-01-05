package truong.com.server;

import io.vertx.core.Vertx;
import truong.com.server.verticle.CarVerticle;

public class Universal {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new CarVerticle());
    }
}
