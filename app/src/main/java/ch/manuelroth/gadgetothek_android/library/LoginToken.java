package ch.manuelroth.gadgetothek_android.library;

/**
 * Created by mgfeller on 08.08.2014.
 */
public class LoginToken {
    private String customerId;
    private String securityToken;

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
