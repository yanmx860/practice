package com.practice.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/** * 系统用户实体 * @author ymx * @since 2026-01-20 */
@Schema(description = "系统用户")
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Integer id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码(BCrypt)")
    private String password;
    @Schema(description = "权限列表(逗号分隔)")
    private String permissions;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}
