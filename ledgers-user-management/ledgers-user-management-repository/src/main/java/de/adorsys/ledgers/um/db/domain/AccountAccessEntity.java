package de.adorsys.ledgers.um.db.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import javax.persistence.*;

/*
*
*
* */

@Entity
@Table(name = "account_accesses")
public class AccountAccessEntity {

    @Id
    @Column(name = "account_access_id")
    private String id;


    @NotNull
    @Column(nullable = false)
    private String iban;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessTypeEntity accessTypeEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public AccessTypeEntity getAccessTypeEntity() {
        return accessTypeEntity;
    }

    public void setAccessTypeEntity(AccessTypeEntity accessTypeEntity) {
        this.accessTypeEntity = accessTypeEntity;
    }
}
