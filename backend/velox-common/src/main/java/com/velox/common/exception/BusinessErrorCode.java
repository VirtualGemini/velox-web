package com.velox.common.exception;

/**
 * 业务错误码（10000-99999）
 * <p>
 * 业务逻辑相关的错误，按模块分段：
 * <ul>
 *   <li>10000-10999: 通用业务</li>
 *   <li>11000-11999: 用户模块</li>
 *   <li>12000-12999: 认证模块</li>
 *   <li>13000-13999: 文件模块</li>
 *   <li>20000+: 其他业务模块</li>
 * </ul>
 */
public enum BusinessErrorCode implements ErrorCode {

    // ---- 通用业务 (10000-10999) ----
    BUSINESS_ERROR(10000, "业务处理异常"),
    OPERATION_FAILED(10001, "操作失败"),
    DATA_NOT_FOUND(10002, "数据不存在"),
    DATA_ALREADY_EXISTS(10003, "数据已存在"),
    DATA_IN_USE(10004, "数据正在使用中，无法删除"),

    // ---- 用户模块 (11000-11999) ----
    USER_NOT_FOUND(11000, "用户不存在"),
    USER_DISABLED(11001, "用户已被禁用"),
    USER_PASSWORD_ERROR(11002, "密码错误"),
    USER_PASSWORD_EXPIRED(11003, "密码已过期"),

    // ---- 认证模块 (12000-12999) ----
    TOKEN_INVALID(12000, "Token无效"),
    TOKEN_EXPIRED(12001, "Token已过期"),
    REFRESH_TOKEN_INVALID(12002, "刷新Token无效"),
    LOGIN_FAILED(12003, "登录失败"),
    CAPTCHA_ERROR(12004, "验证码错误"),
    CAPTCHA_EXPIRED(12005, "验证码已过期"),
    PASSWORD_ERROR(12006, "密码错误"),
    ACCOUNT_DISABLED(12007, "账号已被禁用"),
    PASSWORD_MISMATCH(12008, "两次密码输入不一致"),
    USER_ALREADY_EXISTS(12009, "用户已存在"),
    ACCOUNT_LOCKED(12010, "账号已被锁定，请稍后重试"),
    PASSWORD_SAME_AS_OLD(12011, "新密码不能与当前密码相同"),
    EMAIL_REQUIRED(12012, "邮箱不能为空"),
    EMAIL_NOT_BOUND(12013, "该邮箱未绑定任何账号"),
    RESET_CODE_ERROR(12014, "重置验证码错误"),
    RESET_CODE_EXPIRED(12015, "重置验证码已过期"),
    RESET_CODE_SEND_TOO_FREQUENT(12016, "验证码发送过于频繁，请稍后重试"),
    EMAIL_SERVICE_DISABLED(12017, "邮件能力未启用，请联系管理员"),
    EMAIL_SEND_FAILED(12018, "邮件发送失败，请稍后重试"),
    LOGIN_CODE_ERROR(12019, "登录验证码错误"),
    LOGIN_CODE_EXPIRED(12020, "登录验证码已过期"),
    LOGIN_CODE_SEND_TOO_FREQUENT(12021, "登录验证码发送过于频繁，请稍后重试"),
    PHONE_LOGIN_NOT_SUPPORTED(12022, "手机号登录暂未开通"),
    PHONE_REQUIRED(12023, "手机号不能为空"),
    PHONE_NOT_BOUND(12024, "该手机号未绑定任何账号"),
    EMAIL_ALREADY_IN_USE(12025, "该邮箱已被其他账号绑定"),
    EMAIL_SAME_AS_CURRENT(12026, "新邮箱与当前邮箱相同"),
    REBIND_CODE_ERROR(12027, "换绑验证码错误"),
    REBIND_CODE_EXPIRED(12028, "换绑验证码已过期"),
    REBIND_CODE_SEND_TOO_FREQUENT(12029, "换绑验证码发送过于频繁，请稍后重试"),
    MFA_NOT_ENABLED(12030, "未开启邮箱二次验证"),
    MFA_CODE_ERROR(12031, "MFA 验证码错误"),
    MFA_CODE_EXPIRED(12032, "MFA 验证码已过期"),
    MFA_CODE_SEND_TOO_FREQUENT(12033, "MFA 验证码发送过于频繁，请稍后重试"),
    MFA_CHALLENGE_INVALID(12034, "MFA 挑战无效"),
    MFA_CHALLENGE_EXPIRED(12035, "MFA 挑战已过期"),
    LOGIN_METHOD_EMPTY(12036, "至少保留一种登录方式"),
    LOGIN_METHOD_NOT_ALLOWED(12037, "选择了系统未启用的登录方式"),
    PASSWORD_METHOD_REQUIRED(12038, "不允许关闭密码登录"),
    EMAIL_NOT_BOUND_TO_USER(12039, "当前账号未绑定邮箱"),
    LOGIN_METHOD_DISABLED(12040, "当前登录方式已被禁用"),
    MFA_ALREADY_ENABLED(12041, "已开启另一种虚拟 MFA 设备验证方式，请先关闭"),
    MFA_TOTP_NOT_ENABLED(12042, "未开启虚拟 MFA 设备验证"),
    MFA_TOTP_CODE_ERROR(12043, "动态口令错误"),
    MFA_TOTP_NOT_PROVISIONED(12044, "请先完成 TOTP 绑定"),
    MFA_TOTP_SERVICE_DISABLED(12045, "TOTP 能力未启用，请联系管理员"),
    REBIND_PROOF_REQUIRED(12046, "请先完成当前身份验证"),
    REBIND_PROOF_INVALID(12047, "当前身份验证已失效，请重新验证"),
    REBIND_PROOF_TYPE_MISMATCH(12048, "当前身份验证方式不匹配"),
    REBIND_CURRENT_EMAIL_CODE_ERROR(12049, "当前邮箱验证码错误"),
    REBIND_CURRENT_EMAIL_CODE_EXPIRED(12050, "当前邮箱验证码已过期"),
    REBIND_PASSWORD_ERROR(12051, "当前密码错误"),

    // ---- 文件模块 (13000-13999) ----
    FILE_UPLOAD_FAILED(13000, "文件上传失败"),
    FILE_NOT_FOUND(13001, "文件不存在"),
    FILE_TYPE_NOT_ALLOWED(13002, "文件类型不允许"),
    FILE_CONFIG_NOT_FOUND(13003, "文件配置不存在"),
    FILE_CONFIG_DELETE_FAIL_MASTER(13004, "不能删除主配置"),
    FILE_STORAGE_ERROR(13005, "文件存储服务异常"),
    FILE_PATH_REQUIRED(13006, "结尾的 path 路径必须传递"),
    FILE_CONFIG_INVALID(13007, "文件配置不合法: %s"),
    FILE_STORAGE_TYPE_UNSUPPORTED(13008, "不支持的存储类型: %s"),
    FILE_SERVICE_DISABLED(13009, "文件能力未启用，请联系管理员"),
    FILE_CONFIG_TEST_FAILED(13010, "文件配置测试失败: %s"),

    // ---- 权限模块 (20000-20999) ----
    ROLE_NOT_FOUND(20000, "角色不存在"),
    ROLE_REQUIRED(20001, "至少选择一个角色"),
    ROLE_INVALID(20002, "存在无效角色，请刷新后重试"),

    // ---- 菜单模块 (21000-21999) ----
    MENU_TYPE_INVALID(21000, "菜单类型只能是 menu 或 button"),
    MENU_BUTTON_PARENT_INVALID(21001, "按钮必须挂载在菜单下"),
    MENU_PATH_REQUIRED(21002, "菜单路由地址不能为空"),
    MENU_AUTH_MARK_REQUIRED(21003, "权限标识不能为空"),
    MENU_PARENT_REQUIRED(21004, "父级菜单不能为空"),

    // ---- 用户管理模块 (22000-22999) ----
    USER_PASSWORD_REQUIRED_FOR_CREATE(22000, "新增用户时密码不能为空"),
    SYSTEM_ROLE_DELETE_FORBIDDEN(22001, "系统角色不可删除"),
    SUPER_ROLE_EDIT_FORBIDDEN(22002, "最高权限角色不可编辑"),
    USER_OPERATE_SELF_FORBIDDEN(22003, "不可操作当前登录用户"),
    USER_DELETE_FORBIDDEN(22004, "无权限删除该用户"),
    USER_UPDATE_FORBIDDEN(22005, "无权限修改该用户"),
    SYSTEM_ROLE_CODE_IMMUTABLE(22006, "系统角色编码不允许修改"),
    ROLE_MENU_GRANT_BEYOND_SCOPE(22007, "无权授予超出自身范围的菜单权限"),
    ;

    private final int code;
    private final String message;

    BusinessErrorCode(int code, String message) {
        assertRange(code, 10000, 99999);
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    private static void assertRange(int code, int min, int max) {
        if (code < min || code > max) {
            throw new IllegalArgumentException(
                    "Business error code must be in range [" + min + ", " + max + "], got: " + code);
        }
    }
}
