import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

public class CandidateTest {

    @Test
    public void deploymentProcessDefinition_classpath() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("出差申请流程-组任务")//添加部署的名称
                .addClasspathResource("bpmn/evection-candidate.bpmn")//从classpath的资源中加载，一次只能加载一个文件
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

        runtimeService.startProcessInstanceByKey("my-evection-candidate");
    }

    /**
     * 查询组任务
     * ACT_RU_IDENTITYLINK
     */
    @Test
    public void findGroupTaskList() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 任务办理人
        String candidateUser = "wangwu";
        List<Task> list = processEngine.getTaskService()//
                .createTaskQuery()//
                .processDefinitionKey("my-evection-candidate")
                .taskCandidateUser(candidateUser)// 根据候选人查询任务
                .list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的负责人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }
    }

    /**
     * 候选人拾取任务
     */
    @Test
    public void claimTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String taskId = "";
        String candidateUser = "wangwu";
        Task task = processEngine.getTaskService()//
                .createTaskQuery()//
                .processDefinitionKey("my-evection-candidate")
                .taskCandidateUser(candidateUser)// 根据候选人查询任务
                .singleResult();
        if (task != null) {
            taskService.claim(task.getId(), candidateUser);
            System.out.println("任务：" + task.getId() + "被用户：" + candidateUser + "拾取完成");
        }
    }

    /**
     * 候选人归还任务
     */
    @Test
    public void backTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String taskId = "42502";
        String user = "wangwu";
        Task task = processEngine.getTaskService()//
                .createTaskQuery()//
                .processDefinitionKey("my-evection-candidate")
                .taskId(taskId)
                .taskAssignee(user)
                .singleResult();
        if (task != null) {
            // 归还任务就是把assignee设为null
            taskService.setAssignee(task.getId(), null);
            System.out.println("任务：" + task.getId() + "被用户：" + user + "归还完成");
        }
    }

    @Test
    public void transferTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String taskId = "42502";
        String user = "wangwu";
        String transferUser = "lisi";
        Task task = processEngine.getTaskService()//
                .createTaskQuery()//
                .processDefinitionKey("my-evection-candidate")
                .taskId(taskId)
                .taskAssignee(user)
                .singleResult();
        if (task != null) {
            // 交接任务就是把assignee设为接手人
            taskService.setAssignee(task.getId(), transferUser);
            System.out.println("任务：" + task.getId() + "被用户：" + user + "交接给：" + transferUser);
        }
    }

    /**
     * 查询某人的任务
     * act_ru_task 流程当前活动的任务
     * 完成的任务进入act_hi_taskinst表
     */
    @Test
    public void findPersonalTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<Task> list = processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                /**查询条件（where部分）*/
                .processDefinitionKey("my-evection-candidate")
                .taskAssignee("wangwu")//指定个人任务查询，指定办理人
//                      .taskCandidateUser(candidateUser)//组任务的办理人查询
//                      .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                      .processInstanceId(processInstanceId)//使用流程实例ID查询
//                      .executionId(executionId)//使用执行对象ID查询
                /**排序*/
                .orderByTaskCreateTime().asc()//使用创建时间的升序排列
                /**返回结果集*/
//                      .singleResult()//返回惟一结果集
//                      .count()//返回结果集的数量
//                      .listPage(firstResult, maxResults);//分页查询
                .list();//返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("********************************************");
            }
        }
    }

    @Test
    public void completeTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String assignee = "zhangsan";
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("my-evection-candidate")
                .taskAssignee(assignee)
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
