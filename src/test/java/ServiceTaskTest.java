import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ServiceTaskTest {

    @Test
    public void deploymentProcessDefinition_classpath() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("服务型任务测试")//添加部署的名称
                .addClasspathResource("bpmn/service-task-test.bpmn")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID：" + deployment.getId());
        System.out.println("部署名称：" + deployment.getName());
    }

    /**
     * 启动流程实例
     * ACT_RU_VARIABLE
     */
    @Test
    public void testStart() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> map = new HashMap<>();
        map.put("businessKey", "37927");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("my_service_task", map);

    }

    @Test
    public void completeTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("my_service_task")
                .taskAssignee("张三")
                .singleResult();
        if (task != null) {
            System.out.println("流程实例ID：" + task.getProcessInstanceId());
            System.out.println("流程任务ID：" + task.getId());
            System.out.println("负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
            taskService.complete(task.getId());
        }
    }
}
