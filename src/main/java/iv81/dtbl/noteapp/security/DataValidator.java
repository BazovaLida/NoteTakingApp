package iv81.dtbl.noteapp.security;

import java.util.regex.Pattern;

public class DataValidator {
    private static final String emailRegex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static Pattern emailPattern = Pattern.compile(emailRegex);
    private static final String pswdRegex = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{6,30})";
    private static Pattern pswdPattern = Pattern.compile(pswdRegex);

    public DataValidator() {}

    public boolean emailPswdIsValid(String email, String password) {
        return (emailPattern.matcher(email).matches() && pswdPattern.matcher(password).matches());
    }
}
