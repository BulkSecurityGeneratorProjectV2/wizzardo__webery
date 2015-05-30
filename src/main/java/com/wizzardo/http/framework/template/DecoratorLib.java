package com.wizzardo.http.framework.template;

import com.wizzardo.tools.misc.Unchecked;

import java.util.*;

/**
 * Created by wizzardo on 24.04.15.
 */
public class DecoratorLib {

    private List<Decorator> decorators = new ArrayList<>();

    public DecoratorLib(List<Class> l) {
        for (Class c : l) {
            if (Decorator.class.isAssignableFrom(c) && c != Decorator.class) {
                decorators.add(Unchecked.call(() -> (Decorator) c.newInstance()));
            }
        }
    }

    public List<Decorator> list() {
        return decorators;
    }
}
