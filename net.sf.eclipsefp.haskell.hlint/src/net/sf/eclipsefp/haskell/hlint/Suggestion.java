package net.sf.eclipsefp.haskell.hlint;

public class Suggestion {
	private SourceLocation location;
	private Severity severity;
	private String message;
	private CodeModification pre;
	private CodeModification post;
	
	public SourceLocation getLocation() {
		return location;
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public String getMessage() {
		return message;
	}
	
	public CodeModification getPre() {
		return pre;
	}
	
	public CodeModification getPost() {
		return post;
	}

	public void setLocation(SourceLocation location) {
		this.location = location;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setPre(CodeModification pre) {
		this.pre = pre;
	}

	public void setPost(CodeModification post) {
		this.post = post;
	}
}