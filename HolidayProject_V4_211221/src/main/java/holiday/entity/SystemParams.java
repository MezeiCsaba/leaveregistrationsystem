package holiday.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sysparams")
public class SystemParams {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

//	Email 
	private String mailHost;
	private Integer mailPort;
	private String mailUsername;
	private String mailPassword;

	private String transportProtocol; // smtp
	private String smtpAuth; // true
	private String startSslEnable; // true
	private String mailDebug;

//	Database
	private String dbPassword;
	private String dbUrl;
	private String dbDriverClassName;
	private String dbUser;

	public SystemParams() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbDriverClassName() {
		return dbDriverClassName;
	}

	public void setDbDriverClassName(String dbDriverClassName) {
		this.dbDriverClassName = dbDriverClassName;
	}

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	public Integer getMailPort() {
		return mailPort;
	}

	public void setMailPort(Integer mailPort) {
		this.mailPort = mailPort;
	}

	public String getMailUsername() {
		return mailUsername;
	}

	public void setMailUsername(String mailUsername) {
		this.mailUsername = mailUsername;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getTransportProtocol() {
		return transportProtocol;
	}

	public void setTransportProtocol(String transportProtocol) {
		this.transportProtocol = transportProtocol;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getStartSslEnable() {
		return startSslEnable;
	}

	public void setStartSslEnable(String startSslEnable) {
		this.startSslEnable = startSslEnable;
	}

	public String getMailDebug() {
		return mailDebug;
	}

	public void setMailDebug(String mailDebug) {
		this.mailDebug = mailDebug;
	}

	@Override
	public String toString() {
		return "SystemParams [id=" + id + ", mailHost=" + mailHost + ", mailPort=" + mailPort + ", mailUsername="
				+ mailUsername + ", mailPassword=" + mailPassword + ", transportProtocol=" + transportProtocol
				+ ", smtpAuth=" + smtpAuth + ", startSslEnable=" + startSslEnable + ", mailDebug=" + mailDebug
				+ ", dbPassword=" + dbPassword + ", dbUrl=" + dbUrl + ", dbDriverClassName=" + dbDriverClassName
				+ ", dbUser=" + dbUser + "]";
	}

}
