package top.auntie.gateway.util;

import com.alibaba.fastjson.JSON;
import org.springframework.http.MediaType;
import top.auntie.gateway.model.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class ResponseUtil {
    private ResponseUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 通过流写到前端
     *
     * @param response
     * @param msg        返回信息
     * @param httpStatus 返回状态码
     * @throws IOException
     */
    public static void responseWriter(HttpServletResponse response, String msg, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try (
                Writer writer = response.getWriter()
        ) {
            writer.write(JSON.toJSONString(Result.failed(httpStatus, msg)));
            writer.flush();
        }
    }
}
