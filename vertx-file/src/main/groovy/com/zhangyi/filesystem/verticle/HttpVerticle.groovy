package com.zhangyi.filesystem.verticle

import com.zhangyi.filesystem.util.CommonTool
import groovy.json.JsonSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import net.coobird.thumbnailator.Thumbnails

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

        def fileAddr = addr()

        router.route().handler(BodyHandler.create().setUploadsDirectory("${fileAddr}/temp").setDeleteUploadedFilesOnEnd(true).setBodyLimit(1024 * 1024 * 200))

        router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.OPTIONS).allowedHeader('Content-Type'))

        def staticHandler = StaticHandler.create()
        staticHandler.setAllowRootFileSystemAccess(true)
        staticHandler.setWebRoot(fileAddr)
        staticHandler.setCachingEnabled(true)
        staticHandler.setMaxAgeSeconds(86400 * 7) //7 days

        router.route("/static/*").blockingHandler(staticHandler, false)

        def ipUrl = new URL("http://ip.taobao.com/service/getIpInfo.php?ip=myip").text
        def ip = new JsonSlurper().parseText(ipUrl).data.ip

        log.info("local ip : " + ip)

        router.post("/upload").blockingHandler({ ctx ->
            //简单验证
            def date = new Date().format('yyyy-MM-dd')

            //"realname" 实名认证, "taskbook" 任务书, "maihao" 买号, "showpicture" 晒图
            def type = ctx.request().getParam("type")

            date = date.substring(0, date.lastIndexOf('-')) - "-"
            def ids = []
            String errorMsg = ""
            ctx.fileUploads().each { fu ->
                def addr = fu.uploadedFileName()
                def postfix
                def fileName = fu.fileName()
                postfix = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.size())
                def id = UUID.randomUUID().toString()
                def imageUrl = "${id}.${postfix}".toString()
                def dir = "${fileAddr}/${type}/${date}"
                def x = new File(dir)
                if (!x.exists()) {
                    x.mkdirs()
                } else {
                    if (x.isFile()) {
                        x.mkdirs()
                    }
                }
                try {
                    if (postfix.toUpperCase() in ["JPG", "JPEG", "PNG", "BMP"]) {
                        Thumbnails.of(addr)
                                .scale(1f)
                                .outputQuality(0.5f)
                                .toFile("${dir}/${imageUrl}")
                    } else {
                        vertx.fileSystem().copyBlocking(addr, "${dir}/${imageUrl}")
                    }
                    ids << [img: "http://${ip}:${port}/${type}/${date}/${id}.${postfix}".toString()]
                } catch (e) {
                    vertx.fileSystem().copyBlocking(addr, "${dir}/${imageUrl}")
                    ids << [img: "http://${ip}:${port}/${type}/${date}/${id}.${postfix}".toString()]
                }
            }
            ctx.response().end()
        }, false)

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
