import com.test.pojo.Evection;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class VariablesTest {

    @Test
    public void deploymentProcessDefinition_classpath() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("出差申请流程-variable-global")//添加部署的名称
                .addClasspathResource("bpmn/evection-global.bpmn")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID：" + deployment.getId());
        System.out.println("部署名称：" + deployment.getName());
    }

    /**
     * 启动流程实例时设置变量 GLOBAL
     * ACT_RU_VARIABLE
     */
    @Test
    public void testStart() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Evection evection = new Evection();
        evection.setNum(5d);

        Map<String, Object> map = new HashMap<>();
        map.put("assignee0", "张三1");
        map.put("assignee1", "李经理1");
        map.put("assignee2", "王总1");
        map.put("assignee3", "赵财务1");

        map.put("evection", evection);

        runtimeService.startProcessInstanceByKey("my_evection_global", map);
    }

    /**
     * 在完成当前节点任务时，设置节点完成后续流程所需的变量 GLOBAL
     */
    @Test
    public void completeTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();

        Map<String, Object> map = new HashMap<>();
        Evection evection = new Evection();
        evection.setNum(5d);
        map.put("evection", evection);

        Task task = taskService.createTaskQuery()
                .processDefinitionKey("my_evection_global")
                .taskAssignee("王总1")
                .singleResult();
        if (task != null) {
            System.out.println("流程实例ID：" + task.getProcessInstanceId());
            System.out.println("流程任务ID：" + task.getId());
            System.out.println("负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
            taskService.complete(task.getId(), map);
        }
    }

    /**
     * 通过当前流程实例设置变量 GLOBAL
     */
    @Test
    public void setGlobalVariableByExecutionId() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 当前流程实例执行id，通常设置为当前执行的流程实例
        // executionId 必须当前末结束流程实例的执行id,通常此id设置流程实例的id.也可以通过
        // RuntimeService.getVariable()获取流程变量
        String executionId = "2601";
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Evection evection = new Evection();
        evection.setNum(5d);
        //通过流程实例id设置流程变量
        runtimeService.setVariable(executionId, "evection", evection);
        //一次设置多个值
        //runtimeService.setVariables(executionId, variables);
    }


    /**
     * 通过当前任务设置变量 GLOBAL
     */
    @Test
    public void setGlobalVariableByTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        // 当前待办任务id
        // 任务id必须是当前待办任务id. act_ru_task中存在。如果该任务已结束，会报错
        // 也可以通过 taskService.getVariable获取流程变量
        String taskId = "1404";
        TaskService taskService = processEngine.getTaskService();
        Evection evection = new Evection();
        evection.setNum(5d);
        //通过任务设置流程变量
        taskService.setVariable(taskId, "evection", evection);
        // 一次设置多个值
        // taskService.setVariables(taskI,variables);
    }


    /**
     * 任务办理时设置local流程变量，当前运行的流程实例只能在该任务结束前使用，任务结束该变量无法在当前流程
     * 实例使用，可以通过查询历史任务查询
     */
    @Test
    public void completeTaskLocal() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();

        Map<String, Object> map = new HashMap<>();
        Evection evection = new Evection();
        evection.setNum(5d);
        map.put("evection", evection);

        Task task = taskService.createTaskQuery()
                .processDefinitionKey("my_evection_global")
                .taskAssignee("王总1")
                .singleResult();
        if (task != null) {
            System.out.println("流程实例ID：" + task.getProcessInstanceId());
            System.out.println("流程任务ID：" + task.getId());
            System.out.println("负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());

            taskService.setVariablesLocal(task.getId(), map);

            taskService.complete(task.getId());
        }
    }

    /**
     * 通过当前任务设置变量 LOCAL
     */
    @Test
    public void setGlobalVariableByTaskLocal() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        // 当前待办任务id
        // 任务id必须是当前待办任务id. act_ru_task中存在。如果该任务已结束，会报错
        // 也可以通过 taskService.getVariable获取流程变量
        String taskId = "1404";
        TaskService taskService = processEngine.getTaskService();
        Evection evection = new Evection();
        evection.setNum(5d);
        //通过任务设置流程变量
        taskService.setVariableLocal(taskId, "evection", evection);
        // 一次设置多个值
        // taskService.setVariablesLocal(taskId,variables);
    }
}
