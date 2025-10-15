package com.example.propertymanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"roles"})
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @CreationTimestamp
    @Column(name = "gmt_create", updatable = false, nullable = false)
    private LocalDateTime gmtCreate;

    @UpdateTimestamp
    @Column(name = "gmt_modified", nullable = false)
    private LocalDateTime gmtModified;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Integer isDeleted = 0;

    /**
     * 用户角色关系（不使用物理外键，通过业务逻辑维护一致性）
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT)),
        inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // 反向关联由业务场景需要时再显式加载，避免在用户实体中维护过多集合导致额外查询。
}
