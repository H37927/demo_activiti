import org.activiti.engine.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class ActivitiBusinessTest {

    /**
     * 启动流程实例，与业务键关联
     * ACT_RU_EXECUTION
     */
    @Test
    public void startProcessInstance_addBusinessKey() {
        //1、流程定义的key，通过这个key来启动流程实例
        String processDefinitionKey = "my_evection";
        String businessKey = "1001";
        //2、与正在执行的流程实例和执行对象相关的Service
        // startProcessInstanceByKey方法还可以设置其他的参数，比如流程变量。
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //使用流程定义的key启动流程实例，key对应bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey);
        System.out.println("流程实例ID:" + processInstance.getId());//流程实例ID
        System.out.println("流程定义ID:" + processInstance.getProcessDefinitionId());//流程定义ID
        System.out.println("当前活动ID:" + processInstance.getActivityId());//
        System.out.println("businessKey:" + processInstance.getBusinessKey());//
    }

    /**
     * 全部流程的挂起（暂停）与激活，操作的是ProcessDefinition
     * ACT_RU_EXECUTION
     * ACT_RU_TASK
     * ACT_RE_PROCDEF
     */
    @Test
    public void suspendProcess() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("my_evection")
                .singleResult();
        boolean suspended = processDefinition.isSuspended();
        String definitionId = processDefinition.getId();
        if (suspended) {
            // 参数1：流程定义ID，参数2：是否激活，参数3：激活时间
            repositoryService.activateProcessDefinitionById(definitionId, true, null);
            System.out.println("流程定义ID：" + definitionId + "，已激活");
        } else {
            // 参数1：流程定义ID，参数2：是否挂起，参数3：挂起时间
            repositoryService.suspendProcessDefinitionById(definitionId, true, null);
            System.out.println("流程定义ID：" + definitionId + "，已挂起");

        }
    }

    /**
     * 单个流程实例的挂起（暂停）与激活，操作的是ProcessInstance
     * ACT_RU_EXECUTION
     * ACT_RU_TASK
     */
    @Test
    public void suspendSingleProcess() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId("7501")
                .singleResult();
        boolean suspended = processInstance.isSuspended();
        String instanceId = processInstance.getId();

        if (suspended) {
            runtimeService.activateProcessInstanceById(instanceId);
            System.out.println("流程实例ID：" + instanceId + "，已激活");
        } else {
            runtimeService.suspendProcessInstanceById(instanceId);
            System.out.println("流程实例ID：" + instanceId + "，已挂起");
        }
    }

    @Test
    public void completeSuspendTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("7501")
                .taskAssignee("zhangsan")
                .singleResult();
        System.out.println("流程实例ID："+task.getProcessInstanceId());
        System.out.println("流程任务ID："+task.getId());
        System.out.println("负责人："+task.getAssignee());
        System.out.println("任务名称："+task.getName());
        taskService.complete(task.getId());
    }

}
