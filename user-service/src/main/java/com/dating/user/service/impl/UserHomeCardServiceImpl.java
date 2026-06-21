package com.dating.user.service.impl;

import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.UserHomeCardService;
import com.dating.user.service.support.HomeCardSelfValidator;
import com.dating.user.service.support.HomeCardTargetVisibilityEvaluator;
import com.dating.user.service.support.ProfileViewConverter;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.vo.HomeCardProfileVO;
import com.dating.user.vo.UserProfileViewVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 主页卡片查询业务实现。
 */
@Service
@Profile("!test")
public class UserHomeCardServiceImpl implements UserHomeCardService {

    private static final Logger log = LoggerFactory.getLogger(UserHomeCardServiceImpl.class);

    private final UserManager userManager;
    private final UserProfileManager userProfileManager;
    private final HomeCardSelfValidator homeCardSelfValidator;
    private final HomeCardTargetVisibilityEvaluator homeCardTargetVisibilityEvaluator;
    private final ProfileViewConverter profileViewConverter;
    private final SlowCallLogger slowCallLogger;

    public UserHomeCardServiceImpl(UserManager userManager,
                                   UserProfileManager userProfileManager,
                                   HomeCardSelfValidator homeCardSelfValidator,
                                   HomeCardTargetVisibilityEvaluator homeCardTargetVisibilityEvaluator,
                                   ProfileViewConverter profileViewConverter,
                                   SlowCallLogger slowCallLogger) {
        this.userManager = userManager;
        this.userProfileManager = userProfileManager;
        this.homeCardSelfValidator = homeCardSelfValidator;
        this.homeCardTargetVisibilityEvaluator = homeCardTargetVisibilityEvaluator;
        this.profileViewConverter = profileViewConverter;
        this.slowCallLogger = slowCallLogger;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeCardProfileVO getHomeCardProfile(GetHomeCardProfileQuery query) {
        long startNano = System.nanoTime();
        boolean success = true;
        String errorCode = null;
        Long selfUserId = query == null ? null : query.getSelfUserId();
        try {
            validateQuery(query);
            selfUserId = query.getSelfUserId();
            long targetUserId = query.getTargetUserId();

            // self 校验：查看者必须 ACTIVE
            UserEntity selfUser = userManager.findByUserId(selfUserId);
            homeCardSelfValidator.validateSelfViewer(selfUser);

            // target 校验：存在且基础可见；不做 match / visit / post 聚合
            UserEntity targetUser = userManager.findByUserId(targetUserId);
            UserProfileEntity targetProfile = userProfileManager.findByUserId(targetUserId);
            homeCardTargetVisibilityEvaluator.validateTargetVisible(targetUser, targetProfile);

            // 复用 ProfileViewConverter 组装 Swagger UserProfileView，含 AvatarVO / pending
            UserProfileViewVO targetView = profileViewConverter.toView(targetUser, targetProfile);

            HomeCardProfileVO result = new HomeCardProfileVO();
            result.setSelfUserId(selfUserId);
            result.setTargetProfile(targetView);

            log.info("HomeCard 查询成功, selfUserId={}, targetUserId={}", selfUserId, targetUserId);
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } catch (Exception ex) {
            success = false;
            errorCode = UserErrorCode.HOME_CARD_QUERY_FAILED.getCode();
            log.error("HomeCard 查询失败, selfUserId={}", selfUserId, ex);
            throw new UserBizException(UserErrorCode.HOME_CARD_QUERY_FAILED);
        } finally {
            slowCallLogger.logIfSlow("getHomeCardProfile", startNano, selfUserId, success, errorCode);
        }
    }

    private void validateQuery(GetHomeCardProfileQuery query) {
        if (query == null) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "请求不能为空");
        }
        if (query.getSelfUserId() == null || query.getSelfUserId() <= 0) {
            throw new UserBizException(UserErrorCode.INVALID_USER_ID);
        }
        if (query.getTargetUserId() == null || query.getTargetUserId() <= 0) {
            throw new UserBizException(UserErrorCode.INVALID_TARGET_USER_ID);
        }
    }
}
