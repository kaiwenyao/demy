package dev.kaiwen.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 用户实体，对应数据库 users 表
 */
@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "email", length = 128)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "role", length = 32)
    private String role = "USER";

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "update_time")
    private Instant updateTime;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = Instant.now();
    }
}
