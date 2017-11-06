package com.zhangyi.filesystem.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient

class FileSystemVerticle extends AbstractVerticle {

    @Override
    void start() throws Exception {
        super.start()
    }

    static Future uploadFile(Vertx vertx, def fileName) {
            def client = WebClient.create(vertx)
            def url = "http://xxxx/core/taskbook/img/upload?resultType=json"
            def post = client.postAbs(url).timeout(60000)
            def fileBuf = vertx.fileSystem().readFileBlocking("xxxxx/${fileName}")
            def buffer = Buffer.buffer()
            String contentType = "application/octet-stream"
            String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO"
            String header =
                    "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Content-Transfer-Encoding: binary\r\n" +
                            "\r\n"
            buffer.appendString(header)
            buffer.appendBuffer(fileBuf)
            String footer = "\r\n--" + boundary + "--\r\n"
            buffer.appendString(footer)

            post.putHeader("content-length", String.valueOf(buffer.length()))
            post.putHeader("content-type", "multipart/form-data; boundary=" + boundary)
            post.sendBuffer(buffer, { ar ->
                if (ar.succeeded()) {
                    // Obtain response
                    def response = ar.result()
                    def body = response.bodyAsString()
                    println(body)
                } else {
                    println(ar.cause().message)
                }
                client.close()
            })

    }
}
