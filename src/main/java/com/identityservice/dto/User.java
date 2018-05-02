package com.identityservice.dto;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

public class User implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicLong counter = new AtomicLong();

    public User() {
    	this.id = counter.incrementAndGet();
    	this.status = Status.ACTIVE;
    }
    
    public User(String firstName, String lastName, String userName, Status status) {
    	this(firstName, lastName, userName);
    	this.status = status;
    }
    
    public User(String firstName, String lastName, String userName) {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.userName = userName;
    	this.createDate = Calendar.getInstance();
    	this.updateDate = Calendar.getInstance();
    	this.id = counter.incrementAndGet();
    	this.status = Status.ACTIVE;
    	this.email = String.format("%s@identityservice.com", userName);
    }
    
    public User(String firstName, String lastName, String userName, String password) {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.userName = userName;
    	this.createDate = Calendar.getInstance();
    	this.updateDate = Calendar.getInstance();
    	this.password = password;
    	this.id = counter.incrementAndGet();
    	this.status = Status.ACTIVE;
    	this.email = String.format("%s@identityservice.com", userName);
    }
    
    private Long id;
    
    @NotEmpty(message = "{user.firstName.required}")
    private String firstName;
    
    @NotEmpty(message = "{user.lastName.required}")
    private String lastName;
    
    @NotEmpty(message = "{user.userName.required}")
    private String userName;
    
    @NotEmpty(message = "{user.password.required}")
    private String password;
    
    private String email;
    
    private Status status;
    
    @DateTimeFormat(style = "M-")
    private Calendar createDate;
    
    @DateTimeFormat(style = "M-")
    private Calendar updateDate;


    /**
     * Gets firstName value
     *
     * @return String
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Sets firstName value
     *
     * @param firstName
     * @return User
     */
    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Gets lastName value
     *
     * @return String
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Sets lastName value
     *
     * @param lastName
     * @return User
     */
    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Gets userName value
     *
     * @return String
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Sets userName value
     *
     * @param userName
     * @return User
     */
    public User setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Gets password value
     *
     * @return String
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets password value
     *
     * @param password
     * @return User
     */
    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Gets email value
     *
     * @return String
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Sets email value
     *
     * @param email
     * @return User
     */
    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Gets status value
     *
     * @return Status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets status value
     *
     * @param status
     * @return User
     */
    public User setStatus(Status status) {
        this.status = status;
        return this;
    }

    /**
     * Gets createDate value
     *
     * @return Calendar
     */
    public Calendar getCreateDate() {
        return this.createDate;
    }

    /**
     * Sets createDate value
     *
     * @param createDate
     * @return User
     */
    public User setCreateDate(Calendar createDate) {
        this.createDate = createDate;
        return this;
    }

    /**
     * Gets updateDate value
     *
     * @return Calendar
     */
    public Calendar getUpdateDate() {
        return this.updateDate;
    }

    /**
     * Sets updateDate value
     *
     * @param updateDate
     * @return User
     */
    public User setUpdateDate(Calendar updateDate) {
        this.updateDate = updateDate;
        return this;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((updateDate == null) ? 0 : updateDate.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (status != other.status)
			return false;
		if (updateDate == null) {
			if (other.updateDate != null)
				return false;
		} else if (!updateDate.equals(other.updateDate))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", userName=" + userName
				+ ", password=" + password + ", email=" + email + ", status=" + status
				+ ", createDate=" + createDate + ", updateDate=" + updateDate + "]";
	}
	
	/** utility methods */
    public void completeDefaultsIfMissing() {
    	if (this.createDate == null)
    		this.createDate = Calendar.getInstance();
    	
    	if (this.updateDate == null)
    		this.updateDate = Calendar.getInstance();
    	
    	if (this.email == null)
    		this.email = String.format("%s@identityservice.com", userName);
    }

}
