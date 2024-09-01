package com.iflove.filter;

import com.iflove.entity.Const;
import com.iflove.entity.RestBean;
import com.iflove.utils.FlowUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@Component
@Order(Const.ORDER_FLOW_LIMIT)
public class FlowLimitFilter extends HttpFilter {
    @Resource
    FlowUtil flowUtil;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (!flowUtil.tryCount(address)) {
            // 限制
            this.writeBlockMessage(response);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 为响应编写拦截内容，提示用户操作频繁
     * @param response 响应
     * @throws IOException 可能的异常
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(RestBean.forbidden("操作频繁，请稍后再试").asJSONString());
    }
}
