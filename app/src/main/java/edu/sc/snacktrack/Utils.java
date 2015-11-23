package edu.sc.snacktrack;

import static com.parse.ParseException.*;
import com.parse.ParseException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities class for SnackTrack.
 */
public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    /**
     * This class should not be instantiated.
     */
    private Utils(){ }

    /**
     * Tries to determine a friendly error message to display to the user, given a ParseException.
     *
     * @param e The exception.
     */
    public static String getErrorMessage(ParseException e){
        if(e != null){
            switch(e.getCode()){
                case CONNECTION_FAILED:
                    return "Connection failed";
                case USERNAME_MISSING:
                    return "Username is missing";
                case PASSWORD_MISSING:
                    return "Password is missing";
                case EMAIL_MISSING:
                    return "Email is missing";
                case USERNAME_TAKEN:
                    return "Username is taken";
                case EMAIL_TAKEN:
                    return "Email is taken";
                case TIMEOUT:
                    return "Request timed out";

                default:
                    // If the default exception message isn't null, return it.
                    String exceptionMessage = e.getMessage();
                    if(exceptionMessage != null){
                        return exceptionMessage;
                    }

                    // Otherwise, check the cause's message
                    else{
                        Throwable cause;
                        String causeMessage;

                        cause = e.getCause();

                        // If the cause isn't null, get its message. Otherwise, set causeMessage to null
                        causeMessage = (cause == null) ? null : cause.getMessage();

                        // If the cause message isn't null, return it.
                        // Otherwise, just return the exception's error code.
                        if(causeMessage != null){
                            return String.format("%s (%d)", causeMessage, e.getCode());
                        } else{
                            return String.format("Error (%d)", e.getCode());
                        }
                    }
            }
        } else{
            return "No error";
        }
    }
}
