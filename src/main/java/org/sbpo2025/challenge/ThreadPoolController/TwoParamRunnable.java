package org.sbpo2025.challenge.ThreadPoolController;

import java.util.function.BiConsumer;

public class TwoParamRunnable<Type1, Type2> implements Runnable{

    private final Type1 param1;
    private final Type2 param2;
    private final BiConsumer<Type1, Type2> action;

    public TwoParamRunnable(Type1 param1, Type2 param2, BiConsumer<Type1, Type2> action){
        this.param1 = param1;
        this.param2 = param2;
        this.action = action;
    }

    @Override
    public void run() {
        action.accept(param1, param2);
    }
}
