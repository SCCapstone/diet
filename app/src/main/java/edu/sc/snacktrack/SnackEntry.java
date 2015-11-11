package edu.sc.snacktrack;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 *
 */
@ParseClassName("SnackEntry")
public class SnackEntry extends ParseObject{

    private static final String OWNER_KEY = "owner";
    private static final String MEAL_TYPE_KEY = "mealType";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PHOTO_KEY = "photo";

    public void setOwner(ParseUser owner){
        put(OWNER_KEY, owner);
    }

    public ParseUser getOwner(){
        return getParseUser(OWNER_KEY);
    }

    public void setTypeOfMeal(String mealType){
        put(MEAL_TYPE_KEY, mealType);
    }

    public String getMealType(){
        return getString(MEAL_TYPE_KEY);
    }

    public void setDescription(String description){
        put(DESCRIPTION_KEY, description);
    }

    public String getDescription(){
        return getString(DESCRIPTION_KEY);
    }

    public void setPhoto(final ParseFile imageFile){
        put(PHOTO_KEY, imageFile);
    }

    public ParseFile getPhoto(){
        return getParseFile(PHOTO_KEY);
    }
}