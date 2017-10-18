package in.goods24.util;

import android.widget.EditText;

/**
 * Created by Shikhar on 10/16/2017.
 */

public class ValidationUtil {
    public static boolean isEmptyTextField(EditText toCheck){
        return (null==toCheck || "".equalsIgnoreCase(toCheck.getText().toString()));
    }
    public static boolean isValidEmail(EditText eMail){
        return (eMail.getText().toString().contains("@")&&eMail.getText().toString().contains("."));
    }
    public static boolean isValidPhoneNumber(EditText phone){
        return (phone.getText().toString().matches("\\d{10}"));
    }
    public static boolean passwordMatcher(EditText password,EditText rePassword){
        return (password.getText().toString().matches(rePassword.getText().toString()));
    }

}
