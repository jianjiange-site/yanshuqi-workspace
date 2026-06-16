package com.dating.user.service.support;

import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 密码哈希服务，使用 BCrypt 保存 password_hash。
 */
@Service
public class PasswordHashService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 校验注册密码格式是否合法。
     *
     * @param rawPassword 明文密码，禁止写入日志
     * @throws UserBizException 当密码为空或格式非法时
     */
    public void validateRegisterPassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new UserBizException(UserErrorCode.PASSWORD_INVALID, "密码不能为空");
        }
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new UserBizException(UserErrorCode.PASSWORD_INVALID, "密码长度需 8-64 位且包含字母和数字");
        }
    }

    /**
     * 对明文密码进行 BCrypt 哈希。
     *
     * @param rawPassword 明文密码，禁止写入日志
     * @return password_hash
     */
    public String hash(String rawPassword) {
        validateRegisterPassword(rawPassword);
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 校验登录密码是否为空。
     *
     * @param rawPassword 明文密码，禁止写入日志
     * @throws UserBizException 当密码为空时
     */
    public void validateLoginPassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new UserBizException(UserErrorCode.PASSWORD_INVALID, "密码不能为空");
        }
    }

    /**
     * 使用 BCrypt 校验明文密码与 password_hash 是否匹配。
     *
     * @param rawPassword  明文密码，禁止写入日志
     * @param passwordHash 数据库中的 password_hash
     * @return 匹配返回 true，否则 false
     */
    public boolean matches(String rawPassword, String passwordHash) {
        validateLoginPassword(rawPassword);
        if (!StringUtils.hasText(passwordHash)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
