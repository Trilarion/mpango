package net.sourceforge.mpango.web.directory;

import java.util.Locale;

import javax.faces.context.FacesContext;

import net.sf.mpango.common.directory.service.AuthenticationException;
import net.sf.mpango.common.directory.service.IAuthenticationService;
import net.sourceforge.mpango.web.directory.jms.ForgotPasswordMessageCreator;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;

public class ResetPasswordBackingBean {

	private static final Logger logger = Logger.getLogger(ResetPasswordBackingBean.class);
	
    /** Result for the sending of the new password */
    protected static final String RESULT_EMAIL_SENT = "email_sent";
    /** Result for user not found when trying to recover the password */
    protected static final String RESULT_USER_NOT_FOUND = "user_not_found";

    private JmsTemplate jmsTemplate;
    private IAuthenticationService authService;
	private FacesContext facesContext;

	private String email;
    private String queueName;
    private String protocol;
    private String hostname;
    private String port;
    private String context;
    private String relativePath;
	private String args;

    /**
     * Method that sends the JMS message to the back end in order to send the email message for recovering the password.
     * @return String Outcome of the situation (email_sent = The email has been sent correctly, user_not_found = No user with that email found).
     */
	public String sendMessage() {
        assert email != null;
        assert authService != null;

        String result = null;
        try {
            authService.generateResetKey(email);
            logger.debug("Sending message to user with email: "+email);
            Locale locale = getFacesContext().getViewRoot().getLocale();
            jmsTemplate.send(queueName, new ForgotPasswordMessageCreator(email, calculateUrl(), locale.toString()));
            result = RESULT_EMAIL_SENT;
        } catch (AuthenticationException e) {
            logger.info("User not found with email: "+email);
            result = RESULT_USER_NOT_FOUND;
        }
        return result;
	}
	
	private String calculateUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(protocol).append("://").append(hostname);
		if ((port != null) && (port.length() > 0)) {
			buffer.append(":").append(port);
		}
		if ((context != null) && (context.length() > 0)) {
			buffer.append("/").append(context);
		}
		if ((relativePath != null) && (relativePath.length() > 0)) {
			buffer.append("/").append(relativePath);
		}
		if ((args != null) && (args.length() > 0)) {
			buffer.append("?").append(args);
		}
		return buffer.toString();
	}

	public FacesContext getFacesContext() {
		if (facesContext == null) {
			facesContext = FacesContext.getCurrentInstance();
		}
		return facesContext;
	}
	
	public void setFacesContext(FacesContext facesContext) {
		this.facesContext = facesContext;
	}

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public IAuthenticationService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthenticationService authService) {
        this.authService = authService;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
}