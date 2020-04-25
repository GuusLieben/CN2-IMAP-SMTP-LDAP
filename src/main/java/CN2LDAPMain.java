import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

public class CN2LDAPMain {

    public static void main(String[] args) {
        final var LDAP_PATH = "ldap://ldap.itd.umich.edu";
        final var USER = "Amy Newman";

        try {
            var props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            props.put(Context.PROVIDER_URL, LDAP_PATH);
            props.put(Context.SECURITY_AUTHENTICATION, "none");

            var ctx = new InitialDirContext(props);

            var ctrls = new SearchControls();
            ctrls.setReturningAttributes(new String[]{"mail"});
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            var answers = ctx.search("", String.format("(cn=%s)", USER), ctrls);
            var result = answers.nextElement();

            System.out.println(result.getAttributes().get("mail"));
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

}
