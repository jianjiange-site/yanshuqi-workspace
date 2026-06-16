package com.dating.user.constant;

/**
 * 资料状态枚举。
 */
public enum ProfileStatus {

    /** 未完善。 */
    INIT,

    /** 基础资料完成。 */
    BASIC_DONE,

    /** 头像完成。 */
    PHOTO_DONE,

    /** 资料完整。 */
    COMPLETED,

    /** 资料被阻断。 */
    BLOCKED
}
