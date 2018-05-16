package edu.scut.cs.hm.agent.proxy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

@Slf4j
@Component
@AllArgsConstructor
public class WsEndpointDeployer implements ServletContextInitializer {

    private final AutowireCapableBeanFactory beanFactory;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        ServerContainer container = (ServerContainer) servletContext.getAttribute(ServerContainer.class.getName());
        try {
            // adding annotated endpoint cause wrapping it by server, that is not good in our case
            ServerEndpointConfig.Builder secb = ServerEndpointConfig.Builder.create(WsProxy.class, "/containers/{container}/attach/ws");
            secb.configurator(new SpringConfigurator());
            container.addEndpoint(secb.build());
        } catch (Exception e) {
            log.error("Can not deploy", e);
        }
    }

    private class SpringConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            return (T) beanFactory.createBean(endpointClass, AutowireCapableBeanFactory.AUTOWIRE_NO, true);
        }
    }
}
