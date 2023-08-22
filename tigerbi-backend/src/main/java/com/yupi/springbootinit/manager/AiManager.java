package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用于对接AI平台
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * AI对话
     *
     * @param modelId
     * @param message
     * @return
     */
    public String doChat(Long modelId, String message) {
         // 构造请求参数
         DevChatRequest devChatRequest = new DevChatRequest();
         // 模型id，末尾加L，转换为long类型
         devChatRequest.setModelId(modelId);
         devChatRequest.setMessage(message);
         // 获取响应结果
         BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
         // 如果响应为null，就抛出系统异常，提示“AI响应错误”
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应错误");
        }
        return response.getData().getContent();
    }
}
