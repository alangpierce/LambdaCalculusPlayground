package com.alangpierce.lambdacalculusplayground.component;

public class ProducerController {
    private final ProducerView view;
    private final ProducerControllerParent parent;

    public ProducerController(ProducerView view,
            ProducerControllerParent parent) {
        this.view = view;
        this.parent = parent;
    }
}
