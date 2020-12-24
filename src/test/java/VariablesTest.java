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
     * 启动流程实例
     * ACT_RU_VARIABLE
     */
    @Test
    public void testStart(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Evection evection = new Evection();
        evection.setNum(5d);

        Map<String, Object> map = new HashMap<>();
        map.put("assignee0","张三1");
        map.put("assignee1","李经理1");
        map.put("assignee2","王总1");
        map.put("assignee3","赵财务1");

        map.put("evection",evection);

        runtimeService.startProcessInstanceByKey("my_evection_global",map);
    }

    @Test
    public void completeTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("my_evection_global")
                .taskAssignee("王总1")
                .singleResult();
        if (task != null) {
            System.out.println("流程实例ID："+task.getProcessInstanceId());
            System.out.println("流程任务ID："+task.getId());
            System.out.println("负责人："+task.getAssignee());
            System.out.println("任务名称："+task.getName());
            taskService.complete(task.getId());
        }
    }
}
