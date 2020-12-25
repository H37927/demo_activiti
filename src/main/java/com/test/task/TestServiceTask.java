package com.test.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class TestServiceTask implements JavaDelegate {
    private Expression name;
    private Expression businessKey;

    public void execute(DelegateExecution execution) {
        String result = name.getValue(execution).toString() + businessKey.getValue(execution).toString();
        System.out.println("执行程序流程：" + result);
        execution.setVariable("resultVar", result);
    }
}
