import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UELTest {


    @Test
    public void deploymentProcessDefinition_classpath() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("出差申请流程")//添加部署的名称
                .addClasspathResource("bpmn/evection-uel.bpmn")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID：" + deployment.getId());
        System.out.println("部署名称：" + deployment.getName());
    }

    /**
     * 启动流程实例
     * ACT_RU_VARIABLE
     */
    @Test
    public void testStartAssigneeUel(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> map = new HashMap<>();
        map.put("assignee0","张三");
        map.put("assignee1","李经理");
        map.put("assignee2","王总");
        map.put("assignee3","赵财务");

        runtimeService.startProcessInstanceByKey("my_evection_uel",map);
    }
}
