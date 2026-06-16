package com.dating.user.service.support;

import com.dating.user.constant.ProfileStatus;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 用户资料 JSON 数组字段序列化支持。
 */
@Component
public class ProfileJsonSupport {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    /**
     * 构造资料 JSON 支持组件。
     */
    public ProfileJsonSupport() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 将字符串列表序列化为 JSON 数组字符串。
     *
     * @param values 字符串列表，可为 null 或空
     * @return JSON 数组字符串；空列表返回 null
     * @throws UserBizException 当序列化失败时
     */
    public String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new UserBizException(UserErrorCode.INTERNAL_ERROR, "资料 JSON 序列化失败");
        }
    }

    /**
     * 将 JSON 数组字符串反序列化为字符串列表。
     *
     * @param json JSON 数组字符串
     * @return 字符串列表，空或 null 时返回空列表
     * @throws UserBizException 当反序列化失败时
     */
    public List<String> fromJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException ex) {
            throw new UserBizException(UserErrorCode.INTERNAL_ERROR, "资料 JSON 反序列化失败");
        }
    }
}
