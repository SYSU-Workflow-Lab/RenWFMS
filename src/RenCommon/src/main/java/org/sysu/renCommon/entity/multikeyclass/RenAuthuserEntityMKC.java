package org.sysu.renCommon.entity.multikeyclass;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * Author: Rinkako
 * Date  : 2018/6/1
 * Usage :
 */
public class RenAuthuserEntityMKC implements Serializable {
    private String username;
    private String domain;

    @Column(name = "username", nullable = false)
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "domain", nullable = false)
    @Id
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenAuthuserEntityMKC that = (RenAuthuserEntityMKC) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username, domain);
    }
}
