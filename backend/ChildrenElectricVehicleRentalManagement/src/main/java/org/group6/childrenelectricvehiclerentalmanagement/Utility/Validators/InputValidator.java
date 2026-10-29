package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class InputValidator {
    public static void isVehicleNameValid(String vehicleName) throws UserException {
        // Tên xe hợp lệ khi:
        // - Không trống/blank
        // - Không có khoảng trống
        // - Chỉ gồm a-z, 0-9 và dấu "-" (không dấu tiếng Việt, không ký tự đặc biệt khác)
        // - Không bắt đầu/kết thúc bằng "-",
        // - Không bắt đầu bằng số
        // - không có "--"
        vehicleName = vehicleName.trim();

        if (vehicleName.isBlank()) throw new UserException("vehicle's name is empty","Tên xe không được để trống");
        if (vehicleName.contains(" ")) throw new UserException("vehicle's name contain white space","Tên xe không được chứa khoảng trống");
        if (!vehicleName.matches("^[a-z0-9-]+$")) throw new UserException("vehicle's name contain invalid syntax","Tên xe chứa ký tự không hợp lệ (Chỉ chứa a-z, 0-9, '-')");
        if (vehicleName.startsWith("-")||vehicleName.endsWith("-")) throw new UserException("vehicle's name start or end with -","Tên xe không được bắt đầu hoặc kết thúc bằng '-' ");
        if (Character.isDigit(vehicleName.charAt(0))) throw new UserException("vehicle's name start with number","Tên xe không được bắt đầu bằng số");
        if (vehicleName.contains("--")) throw new UserException("vehicle's name contain '--'","Tên xe không được chứa hai dấu gạch ngang liền nhau");
    }

    public static void isVehicleStatusValid(String vehicleStatusString) throws UserException {
        try{
            VehicleStatus.valueOf(vehicleStatusString);
        }catch (IllegalArgumentException e){
            throw new UserException("vehicle status is not valid, check source code","Trạng thái xe không hợp lệ, vui lòng liên hệ quản trị");
        }
    }

    public static void isUsernameValid(String username) throws UserException {
        // Username hợp lệ là khi đáp ứng các đk:
        // - Không empty, blank
        // - Không chứa khoảng trắng
        // - Không bắt đầu bằng số
        // - Không ngắn hơn 3 ký tự và không dài hơn 20 ký tự
        // - Không chứa khoảng trống
        // - Không chứa ký tự đặc biệt
        // - Không chứa chữ hoa
        if (username==null||username.isBlank()) throw new UserException("username blank or empty","Tên tài khoản không được để trống");
        if (Character.isDigit(username.charAt(0))) throw new UserException("username start with number","Tên tài khoản không được bắt đầu bằng số");
        if (username.length() < 3) throw new UserException("username too short","Tên tài khoản quá ngắn");
        if (username.length() > 20) throw new UserException("username too long","Tên tài khoản quá dài");
        if (username.contains(" ")) throw new UserException("username contain whitespace","Tên tài khoản không được chứa khoảng trắng");
        String regex = "^[a-z0-9]+$";
        if (!username.matches(regex)) throw new UserException("username contain invalid character","Tên tài khoản không hợp lệ");
        Set<String> notAllowedUsernames = new HashSet<>(List.of("root","user","admin","administrator","true","false","system","null"));

        for(String notAllowedUserName:notAllowedUsernames){
            if (username.equalsIgnoreCase(notAllowedUserName)||username.contains(notAllowedUserName)) throw new UserException("user using blacklist username","Tên tài khoản không được phép sử dụng");
        }
    }

    public static void isFullNameValid(String fullname) throws UserException {
        // Tên của người dùng hợp lệ:
        // - Không null, blank
        // - Không ngắn hơn 2 ký tự
        // - Không có ký tự đặc biệt
        // - không có digit
        if (fullname==null||fullname.isBlank()) throw new UserException("","Tên không được để trống");
        if (fullname.length()<=5) throw new UserException("name too short","Tên quá ngắn, tối thiểu 5 ký tự");
        if (fullname.length()>40) throw new UserException("name too long","Tên quá dài");

        for (Character w:fullname.toCharArray()){
            if (Character.isDigit(w)) throw new UserException("name contain number","Tên không được chứa số");
            if (!Character.isLetterOrDigit(w)){
                if(!Character.isSpaceChar(w)) throw new UserException("name contain special character","Tên không được chứa ký tự đặc biệt");
            }
        }
    }

    public static void isPasswordValid(String password) throws UserException {
        // Password hợp lệ:
        // - Không empty, blank
        // - Không chứa khoảng trắng
        // - Không ngắn hơn 8 ký tự
        // - Có ký tự đặc biệt
        // - Có uppercase, lowercase letter
        // - Có digit

        //Code mới:
        String regex = "^[\\x21-\\x7E]+$";
        if (!password.matches(regex)) {
            throw new UserException("password contain invalud character","password chứa ký tự không hợp lệ");
        }

        if (password == null || password.isBlank()) throw new UserException("password is empty|blank","password không được để trống");
        if (password.length() < 8) throw new UserException("password too short","password quá ngắn");
        if (password.length() >= 64) throw new UserException("password too long","password quá dài");
        if (password.contains(" ")) throw new UserException("password contain whitespace","password không được chứa khoảng trắng");

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            hasUpper |= Character.isUpperCase(c);
            hasLower |= Character.isLowerCase(c);
            hasDigit |= Character.isDigit(c);
            hasSpecial |= !Character.isLetterOrDigit(c);

            if (hasUpper && hasLower && hasDigit && hasSpecial)
                return;
        }
        if (!hasUpper) throw new UserException("password doesn't contain uppercase letter","password phải có ít nhất 1 chữ viết hoa");
        if (!hasLower) throw new UserException("password doesn't contain lowercase letter","password phải có ít nhất 1 chữ thường");
        if (!hasDigit) throw new UserException("password doesn't contain number","password phải có ít nhất 1 số");
        throw new UserException("password doesn't contain special characters","password phải có ít nhất 1 ký tự đặc biệt");
    }

    public static void isPhonenumberValid(String phonenumber) throws UserException {
        // Số điện thoại hợp lệ:
        // - Không blank, empty
        // - Chính xác 10 hoặc 11 ký tự
        // - Chỉ có số
        // - Bắt đầu bằng 03, 05, 07, 08, 09 (Đầu số Việt Nam)
        if (phonenumber.isBlank()) throw new UserException("phone number is empty or blank","Số điện thoại không được để trống");
        if (!(phonenumber.length() == 10) && !(phonenumber.length() == 11)) throw new UserException("character count in phonenumber is not valid","Số ký tự trong SDT không hợp lệ");
        for (char n : phonenumber.toCharArray()) {
            if (!Character.isDigit(n))
                throw new UserException("phonenumber contain letter or special character","SDT không được chứa chữ hoặc ký tự đặc biệt");
        }
        if (
                !phonenumber.startsWith("03")
                        && !phonenumber.startsWith("052")
                        && !phonenumber.startsWith("056")
                        && !phonenumber.startsWith("058")
                        && !phonenumber.startsWith("055")
                        && !phonenumber.startsWith("059")
                        && !phonenumber.startsWith("07")
                        && !phonenumber.startsWith("08")
                        && !phonenumber.startsWith("09")
        ) throw new UserException("phonenumber starts with invalid number","Số điện thoại không hợp lệ");
    }

    public static void isEmailValid(String email) throws UserException {
        /*
         * Rule:
         * - Local part chỉ cho phép chữ, số và một số ký tự phổ biến
         * - Phải có đúng 1 ký tự @
         * - Domain phải có dấu chấm
         * - Không có hai dấu chấm liền kề
         * - Không cho phép khoảng trắng
         * - Không cho phép domain bắt đầu/kết thúc bằng dấu chấm
         * - TLD tối thiểu 2 ký tự
         */
        // Không được null hoặc blank
        if (email == null || email.isBlank()) throw new UserException("email is empty or blank","Email không không được để trống");
        if (email.length()>=50) throw new UserException("email is too long","Email quá dài");
        if (!Character.isLetter(email.charAt(0))) throw new UserException("email doesn't starts with letter","Email không hợp lệ");
        if (email.contains("..")) throw new UserException("email contain invalid character","Email không hợp lệ");

        String EMAIL_REGEX =
                "^[A-Za-z0-9._%+-]+@" +
                        "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*" +
                        "\\.[A-Za-z]{2,}$";

        Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

        // Kiểm tra email có match regex hay không
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new UserException("email wasn't matched regex","Email không hợp lệ");
        }
    }
}

