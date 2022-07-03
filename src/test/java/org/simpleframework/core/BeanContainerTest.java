package org.simpleframework.core;

import com.imooc.controller.DispatcherServlet;
import com.imooc.controller.frontend.MainPageController;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeanContainerTest {
    private static BeanContainer beanContainer;

    @BeforeAll
    static void init() {
        beanContainer = BeanContainer.getInstance();
    }

    @DisplayName("加载目标类及其实例BeanContainer :loadBeanTest")
    @Test
    @Order(1)
    public void loadBeanTest() {
        Assertions.assertEquals(false,beanContainer.isLoaded());
        beanContainer.loadBeans("com.imooc");
        Assertions.assertEquals(6,beanContainer.size());
        Assertions.assertEquals(true,beanContainer.isLoaded());
    }

    @DisplayName("根据类获取其实例：getBeanTest")
    @Test
    @Order(2)
    public void getBeanTest() {

        MainPageController controller = (MainPageController)beanContainer.getBean(MainPageController.class);
        Assertions.assertEquals(true,controller instanceof MainPageController);
        DispatcherServlet dispatcherServlet = (DispatcherServlet)beanContainer.getBean(DispatcherServlet.class);
        Assertions.assertEquals(null,dispatcherServlet);
    }
}
