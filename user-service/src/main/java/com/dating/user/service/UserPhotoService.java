package com.dating.user.service;

import com.dating.user.dto.BindPhotoCommand;
import com.dating.user.dto.ListUserPhotosQuery;
import com.dating.user.exception.UserBizException;
import com.dating.user.vo.BindPhotoResult;
import com.dating.user.vo.UserPhotoVO;

import java.util.List;

/**
 * 用户照片业务服务。
 */
public interface UserPhotoService {

    /**
     * 绑定头像或相册 object key。
     *
     * @param command 绑定命令
     * @return 绑定结果
     * @throws UserBizException 当参数非法、用户状态不允许或 object key 非法时
     */
    BindPhotoResult bindUserPhoto(BindPhotoCommand command);

    /**
     * 查询用户照片列表。
     *
     * @param query 查询条件
     * @return 照片 VO 列表
     * @throws UserBizException 当参数非法或用户不存在时
     */
    List<UserPhotoVO> listUserPhotos(ListUserPhotosQuery query);
}
