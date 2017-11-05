package com.zhangyi.filesystem.verticle

import com.zhangyi.filesystem.util.CommonTool
import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

class HttpVerticle extends AbstractVerticle {

    static def log = LoggerFactory.getLogger(this)

    static int port = 80

    @Override
    void start() throws Exception {

        if (CommonTool.isDebug) {
            port = 8888
        }

        def server = vertx.createHttpServer()

        def router = Router.router(vertx)

        def staticHandler = StaticHandler.create()
        staticHandler.setAllowRootFileSystemAccess(true)
        staticHandler.setWebRoot(addr())
        staticHandler.setCachingEnabled(true)
        staticHandler.setMaxAgeSeconds(86400 * 7) //7 days

        router.route("/static/*").blockingHandler(staticHandler, false)

        server.requestHandler(router.&accept).listen(port, { lh ->
            if (lh.succeeded()) {
                log.info("http server start success @ ${port}")
            } else {
                log.info(lh.cause().message)
            }
        })
    }

    String addr() {
        if (CommonTool.isDebug) {
            return "D:\\picture"
        } else {
            return "/zhangyi/file"
        }
    }
}
