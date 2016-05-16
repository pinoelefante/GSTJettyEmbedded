package util.os;

public class DirectoryNotAvailableException extends Exception{
	private static final long serialVersionUID = 1L;

	public DirectoryNotAvailableException() {
		super();
	}

	public DirectoryNotAvailableException(String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public DirectoryNotAvailableException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DirectoryNotAvailableException(String arg0) {
		super(arg0);
	}

	public DirectoryNotAvailableException(Throwable arg0) {
		super(arg0);
	}

}
