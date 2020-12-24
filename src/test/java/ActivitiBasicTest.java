import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class ActivitiBasicTest {

    @Test
    public void testCreateDB() {
        // 此种默认方式要求spring中存在名为processEngineConfiguration的StandaloneProcessEngineConfiguration类实例
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        System.out.println(processEngine);

        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // 非默认一般方式
        // ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml", "processEngineConfiguration");
        // ProcessEngine processEngine1 = processEngineConfiguration.buildProcessEngine();
    }

    /**
     * 1）act_re_deployment 部署表 部署ID和部署名称就存在这张表中
     * 2）act_re_procdef 流程定义表
     * 3）act_ge_bytearray 流程资源表：流程定义文档的存放地。每部署一个流程定义就会增加两条记录，
     * 一条是关于 bpmn 规则文件的，一条是图片的（如果部署时只指定了 bpmn 一个文件，activiti 会在部署时解析 bpmn 文件内容自动生成流程图）。
     * 两个文件不是很大，都是以二进制形式存储在数据库中。
     **/
    @Test
    public void deploymentProcessDefinition_classpath() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("出差申请流程")//添加部署的名称
                .addClasspathResource("bpmn/evection.bpmn")//从classpath的资源中加载，一次只能加载一个文件
                .addClasspathResource("bpmn/evection.png")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID：" + deployment.getId());
        System.out.println("部署名称：" + deployment.getName());

    }

    /**
     * 部署流程定义（从zip）
     * 一个zip中可能会包含多个流程定义文件
     */
    @Test
    public void deploymentProcessDefinition_zip() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("bpmn/evection.zip");
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("流程定义")//添加部署的名称
                .addZipInputStream(zipInputStream)//指定zip格式的文件完成部署
                .deploy();//完成部署
        System.out.println("部署ID：" + deployment.getId());//
        System.out.println("部署名称：" + deployment.getName());//
    }

    /**
     * 启动流程实例
     */
    @Test
    public void startProcessInstance() {
        //1、流程定义的key，通过这个key来启动流程实例
        String processDefinitionKey = "my_evection";
        //2、与正在执行的流程实例和执行对象相关的Service
        // startProcessInstanceByKey方法还可以设置其他的参数，比如流程变量。
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //使用流程定义的key启动流程实例，key对应bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);
        System.out.println("流程实例ID:" + processInstance.getId());//流程实例ID
        System.out.println("流程定义ID:" + processInstance.getProcessDefinitionId());//流程定义ID
        System.out.println("当前活动ID:" + processInstance.getActivityId());//
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
                .processDefinitionKey("my_evection")
                .taskAssignee("zhangsan")//指定个人任务查询，指定办理人
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


    /**
     * 完成任务
     * ACT_HI_TASKINST -- insert
     * ACT_HI_ACTINST -- insert
     * ACT_HI_IDENTITYLINK -- insert
     * ACT_RU_TASK -- insert
     * ACT_RU_IDENTITYLINK -- insert
     * ACT_HI_TASKINST -- update
     * ACT_RU_EXECUTION -- update id = rev =
     * ACT_HI_ACTINST -- update id =
     * ACT_RU_TASK -- delete id =
     */
    @Test
    public void completePersonalTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //任务ID，上一步查询得到的。
        String taskId = "2505";
        processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .complete(taskId);
        System.out.println("完成任务：任务ID：" + taskId);
    }

    /**
     * 查询流程定义
     */
    @Test
    public void findLastVersionProcessDefinition() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
//                .processDefinitionKey("my_evection")
                .orderByProcessDefinitionVersion().asc()//使用流程定义的版本升序排列
                .list();
//         map集合的特点：当map集合key值相同的情况下，后一次的值将替换前一次的值
        Map<String, ProcessDefinition> map = new LinkedHashMap<String, ProcessDefinition>();
        if (list != null && list.size() > 0) {
            for (ProcessDefinition pd : list) {
                map.put(pd.getKey(), pd);
            }
        }
        List<ProcessDefinition> pdList = new ArrayList<ProcessDefinition>(map.values());
        if (pdList != null && pdList.size() > 0) {
            for (ProcessDefinition pd : pdList) {
                System.out.println("流程定义ID:" + pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:" + pd.getName());//对应hello.bpmn文件中的name属性值
                System.out.println("流程定义的key:" + pd.getKey());//对应hello.bpmn文件中的id属性值
                System.out.println("流程定义的版本:" + pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:" + pd.getResourceName());
                System.out.println("资源名称png文件:" + pd.getDiagramResourceName());
                System.out.println("部署对象ID：" + pd.getDeploymentId());
                System.out.println("*********************************************************************************");
            }
        }
    }

    /**
     * 删除流程定义
     * 删除的就是流程部署的时候插入的表的记录
     * 不会删除历史记录相关的表记录
     */
    @Test
    public void deleteProcessDefinition() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //使用部署ID，完成删除，指定部署对象id为2501删除，就是act_re_deployment的主键
        String deploymentId = "2501";
        /**
         * 不带级联的删除
         *    只能删除没有处于活动中的流程定义（从没启动或所有启动的流程都完成了），如果存在处于活动中的流程，就会抛出异常
         */
//      processEngine.getRepositoryService()//
//                      .deleteDeployment(deploymentId);

        /**
         * 级联删除
         *    不管是否存在活动中的流程，都能可以删除
         */
        processEngine.getRepositoryService()//
                .deleteDeployment(deploymentId, true);
        System.out.println("删除成功！");
    }

    /**
     * 下载流程定义
     *
     * @throws IOException
     */
    @Test
    public void downloadDefinition() throws IOException {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("my_evection")
                .singleResult();

        String deploymentId = processDefinition.getDeploymentId();
        String pngName = processDefinition.getDiagramResourceName();
        String bpmnName = processDefinition.getResourceName();

        InputStream png = repositoryService.getResourceAsStream(deploymentId, pngName);
        InputStream bpmn = repositoryService.getResourceAsStream(deploymentId, bpmnName);

        File pngFile = new File("d:/my_evection.png");
        File bpmnFile = new File("d:/my_evection.bpmn");

        FileOutputStream pngOut = new FileOutputStream(pngFile);
        FileOutputStream bpmnOut = new FileOutputStream(bpmnFile);

        IOUtils.copy(png, pngOut);
        IOUtils.copy(bpmn, bpmnOut);

        png.close();
        bpmn.close();
        pngOut.close();
        bpmnOut.close();

    }

    /**
     * 查询历史流程
     */
    @Test
    public void findHistoryProcessInstance() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        String processInstanceId = "7501";
        List<HistoricActivityInstance> list = processEngine.getHistoryService()// 与历史数据（历史表）相关的Service
                .createHistoricActivityInstanceQuery()// 创建历史流程实例查询
                .processInstanceId(processInstanceId)// 使用流程实例ID查询
//                .processDefinitionId("")
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        for (HistoricActivityInstance hai : list) {
            System.out.println(hai.getId() + "    " + hai.getProcessDefinitionId() + "    " + hai.getStartTime() + "    "
                    + hai.getEndTime() + "     " + hai.getDurationInMillis() + "    " + hai.getActivityId() + "    " + hai.getActivityName());
            System.out.println("==============================");
        }
    }

}
