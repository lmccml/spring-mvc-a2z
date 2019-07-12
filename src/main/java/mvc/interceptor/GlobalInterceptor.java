package mvc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
全局拦截器
 */
public class GlobalInterceptor implements HandlerInterceptor {
    private static Logger log = LoggerFactory.getLogger(GlobalInterceptor.class);

    private static Map<String, Object> exclude = new HashMap<>();// 不需要校验的接口
    private static Map<String, Object> taskExclude = new HashMap<>();// 不需要sign校验的接口

    static {
        //todo 前期放开
        //exclude.put("/", "例如这样子的");
        exclude.put("不需要登录鉴权的接口", "");
        exclude.put("/index", "例如这样子的");
        exclude.put("/user/toLogin", "例如这样子的");
    }


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        if (handler instanceof HandlerMethod) {
            StringBuilder sb = new StringBuilder();

            HandlerMethod h = (HandlerMethod) handler;
            sb.append("业务类：").append(h.getBean().getClass().getName());
            sb.append("；调用方法：").append(h.getMethod().getName());
            sb.append("；访问参数：").append(getParamString(request.getParameterMap()));
            sb.append("；访问路径：").append(request.getRequestURI());
            log.info("======================================" + sb.toString());

            String apiUrl = request.getRequestURI();
            String contxtPath = request.getContextPath();
            apiUrl = apiUrl.replace(contxtPath, "");

            boolean isSignCheck = true;

            for (String key : taskExclude.keySet()) {
                if (apiUrl.startsWith(key)) {
                    isSignCheck = false;
                    break;
                }
            }



            boolean isCheck = true;

            for (String key : exclude.keySet()) {
                if (apiUrl.startsWith(key)) {
                    isCheck = false;
                    break;
                }
            }


            if (isCheck) {
                String curUserCode = request.getParameter("current_user_code");
                String login_key = request.getParameter("login_key");
                if ("".equals(curUserCode) || "".equals(login_key)) {
                    log.info("登录校验参数为空===="+request.getRequestURI());
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("401:您的账号正在另一个客户端登陆，当前登录账号将会自动退出登录！");
                    return false;
                }

            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    private String getParamString(Map<String, String[]> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> e : map.entrySet()) {
            sb.append(e.getKey()).append("=");
            String[] value = e.getValue();
            if (value != null && value.length == 1) {
                sb.append(value[0]).append("\t");
            } else {
                sb.append(Arrays.toString(value)).append("\t");
            }
        }
        return sb.toString();
    }
}
