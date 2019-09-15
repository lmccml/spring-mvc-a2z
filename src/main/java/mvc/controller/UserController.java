package mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/getUserInfo")
    @ResponseBody
    public String getUserInfo(HttpServletRequest request) {
        //servlet容器启动的时候会加载org.springframework.web.SpringServletContainerInitializer这个类，调用初始化类的onStartup方法
        SpringServletContainerInitializer springServletContainerInitializer = new SpringServletContainerInitializer();
       //进入到DispatcherServlet初始化
        return "test";
    }
}
