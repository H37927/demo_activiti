import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DynamicBpmnModel {

    /**
     * 开始任务节点
     *
     * @return
     */
    protected StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }


    /**
     * 结束任务节点
     *
     * @return
     */
    protected EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }


    /**
     * @param id       对应我们画流程图中节点任务id
     * @param name     节点任务名称
     * @param assignee 任务的执行者(这一块自行决定是否添加每一环节的执行者，若是动态分配的话，可以不用传值)
     * @return
     */
    protected UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }


    /**
     * 排他网关
     * @param id 网关id
     * @return
     */
    protected static ExclusiveGateway createExclusiveGateway(String id) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        return exclusiveGateway;
    }


    /**
     * 创建连线
     * @param from                连线来源节点
     * @param to                  连线目标节点
     * @param name                连线名称(可不填)
     * @param conditionExpression 网关每一种线路走向的条件表达式
     * @return
     */
    protected SequenceFlow createSequenceFlow(String from, String to, String name, String conditionExpression) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setName(name);
        if (StringUtils.isNotEmpty(conditionExpression)) {
            flow.setConditionExpression(conditionExpression);
        }
        return flow;
    }

    public void test() {
        // 1.实例化BpmnModel 对象
        BpmnModel model = new BpmnModel();

        // 2.构造process对象
        Process process = new Process();
        model.addProcess(process);
        process.setId("multiple-process3");

        // 判断是否仅为一个节点任务
        List<String> taskList = new ArrayList<String>();
        taskList.add("报销申请");
        taskList.add("主管审批");
        taskList.add("经理审批");
        taskList.add("总经理审批 ");
        //单节点任务
        if (taskList.size() == 1) {
            process.addFlowElement(createStartEvent());
            process.addFlowElement(createUserTask("task1", taskList.get(0), null));
            process.addFlowElement(createEndEvent());
            process.addFlowElement(createSequenceFlow("start", "task1", "", ""));
            process.addFlowElement(createSequenceFlow("task1", "end", "", ""));
        } else {
            // 多节点任务
            // 构造开始节点任务
            process.addFlowElement(createStartEvent());
            // 构造首个节点任务
            process.addFlowElement(createUserTask("task1", taskList.get(0), null));
            // 构造除去首尾节点的任务
            for (int i = 1; i < taskList.size() - 1; i++) {
                process.addFlowElement(createExclusiveGateway("createExclusiveGateway" + i));
                process.addFlowElement(createUserTask("task" + (i + 1), taskList.get(i), null));
            }
            // 构造尾节点任务
            process.addFlowElement(createExclusiveGateway("createExclusiveGateway" + (taskList.size() - 1)));
            process.addFlowElement(createUserTask("task" + taskList.size(), taskList.get(taskList.size() - 1), null));
            // 构造结束节点任务
            process.addFlowElement(createEndEvent());

            // 构造连线(加网关)
            process.addFlowElement(createSequenceFlow("start", "task1", "", ""));
            // 第一个节点任务到第二个百分百通过的，因此不存在网关
            process.addFlowElement(createSequenceFlow("task1", "task2", "", ""));
            for (int i = 1; i < taskList.size(); i++) {
                process.addFlowElement(createSequenceFlow("task" + (i + 1), "createExclusiveGateway" + i, "", ""));
                // 判断网关走向(同意则直接到下一节点即可，不同意需要判断回退层级，决定回退到哪个节点，returnLevel等于0，即回退到task1)
                // i等于几，即意味着回退的线路有几种可能，例如i等于1，即是task2,那么只能回退 到task1
                // 如果i等于2，即是task3,那么此时可以回退到task1和task2;returnLevel =1 ，即回退到task1，所以这里我是扩展了可以驳回到任意阶段节点任务
                for (int j = 1; j <= i; j++) {
                    process.addFlowElement(createSequenceFlow("createExclusiveGateway" + i, "task" + j, "不通过",
                            "${result == '0' && returnLevel== '" + j + "'}"));
                }
                // 操作结果为通过时，需要判断是否为最后一个节点任务，若是则直接到end
                if (i == taskList.size() - 1) {
                    process.addFlowElement(
                            createSequenceFlow("createExclusiveGateway" + i, "end", "通过", "${result == '1'} "));

                } else {
                    process.addFlowElement(createSequenceFlow("createExclusiveGateway" + i, "task" + (i + 2), "通过",
                            "${result == '1'}"));
                }
            }
        }

        // 3.生成图像信息
        new BpmnAutoLayout(model).execute();

        // 4.部署流程
        RepositoryService repositoryService = null;
        Deployment deployment = repositoryService.createDeployment().addBpmnModel("dynamic-model.bpmn", model)
                .name("multiple process deployment").deploy();

        // 5.启动流程
        RuntimeService runtimeService = null;
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("multiple-process3");
        System.out.println("流程实例ID---》》》" + processInstance.getId());

        // 6.保存png图片和xml文件(这一步可做可不做)
        // 6. Save process diagram to a file
        // InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        // FileUtils.copyInputStreamToFile(processDiagram, new File("target/multiple-process3-diagram.png"));

        // 7. Save resulting BPMN xml to a file
        // InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), "dynamic-model.bpmn");
        // FileUtils.copyInputStreamToFile(processBpmn, new File("target/multiple-process3.bpmn20.xml"));
    }

}
