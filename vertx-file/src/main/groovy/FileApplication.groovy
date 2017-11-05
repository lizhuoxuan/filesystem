import com.zhangyi.filesystem.verticle.HttpVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx

class FileApplication extends AbstractVerticle {

    static void main(def args) {
        Vertx.vertx().deployVerticle(FileApplication.newInstance())
    }

    @Override
    void start() throws Exception {
        Future FileVerticleDeployment = Future.future()
        vertx.deployVerticle(FileApplication.class,
                new DeploymentOptions().setInstances(1),
                FileVerticleDeployment.completer()
        )

        FileVerticleDeployment.compose({ id ->
            Future httpVerticleDeployment = Future.future()
            vertx.deployVerticle(
                    HttpVerticle.class,
                    new DeploymentOptions().setInstances(1),
                    httpVerticleDeployment.completer()
            )
            return httpVerticleDeployment
        }).setHandler({ ar ->
            if (ar.succeeded()) {
                println("启动成功")
            } else {
                println("启动失败")
            }
        })
    }
}
