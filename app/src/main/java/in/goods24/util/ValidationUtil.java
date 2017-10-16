package in.goods24.util;

import android.widget.EditText;

/**
 * Created by Shikhar on 10/16/2017.
 */

public class ValidationUtil {
    public static boolean isEmptyTextField(EditText toCheck){
        return (null==toCheck && "".equalsIgnoreCase(toCheck.toString()));
    }
    public static boolean isValidEmail(EditText eMail){
        return (eMail.toString().contains("@")&&eMail.toString().contains("."));
    }
    public static boolean isValidPhoneNumber(EditText phone){
        return (phone.toString().matches("\\d{10}"));
    }
    public static boolean passwordMatcher(EditText password,EditText rePassword){
        return (password.toString().matches(rePassword.toString()));
    }

}
