package org.sbpo2025.challenge.ThreadPoolController;

import java.util.function.Consumer;

public class OneParamRunnable<T> implements Runnable{

    private final T param;
    private final Consumer<T> action;

    public OneParamRunnable(T param, Consumer<T> action){
        this.param = param;
        this.action = action;
    }

    @Override
    public void run() {
        action.accept(param);
    }
}
