package com.wizzardo.http.framework;

import com.wizzardo.epoll.readable.ReadableData;
import com.wizzardo.http.Handler;
import com.wizzardo.http.mapping.UrlMapping;
import com.wizzardo.http.mapping.UrlTemplate;
import com.wizzardo.tools.collections.CollectionTools.Closure;

/**
 * Created by wizzardo on 05.05.15.
 */
public class ControllerUrlMapping extends UrlMapping<Handler> {

    public <T extends Controller> ControllerUrlMapping append(String url, Class<T> controllerClass, String action) {
        return append(url, new ControllerHandler<>(controllerClass, action));
    }

    public <T extends Controller> ControllerUrlMapping append(String url, Class<T> controllerClass, String action, Closure<ReadableData, T> renderer) {
        return append(url, new ControllerHandler<>(controllerClass, action, renderer));
    }

    @Override
    public ControllerUrlMapping append(String url, Handler handler) {
        super.append(url, handler.name(), handler);
        return this;
    }

    @Override
    public ControllerUrlMapping append(String url, String name, Handler handler) {
        super.append(url, name, handler);
        return this;
    }

    public UrlTemplate getUrlTemplate(Class<? extends Controller> controllerClass, String action) {
        return getUrlTemplate(toMapping(controllerClass, action));
    }

    public UrlTemplate getUrlTemplate(String controller, String action) {
        return getUrlTemplate(toMapping(controller, action));
    }

    public String toMapping(Class<? extends Controller> controllerClass, String action) {
        return toMapping(Controller.getControllerName(controllerClass), action);
    }

    public String toMapping(String controller, String action) {
        return controller + "." + action;
    }
}
