package edu.sc.snacktrack.snacks;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * This class represents a single snack entry with a owner, photo, meal type, and description.
 * This class subclasses ParseObject.
 */
@ParseClassName("SnackEntry")
public class SnackEntry extends ParseObject{

    private static final String OWNER_KEY = "owner";
    private static final String MEAL_TYPE_KEY = "mealType";
    private static final String DESCRIPTION_KEY = "description";
    private static final String PHOTO_KEY = "photo";
    private static final String SCAN_DETAILS_KEY = "scanDetails";
    private static final String SCAN_CONTENT_KEY = "scanContent";

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

    public void setScanDetails(String description){
        put(SCAN_DETAILS_KEY, description);
    }

    public String getScanDetails(){
        return getString(SCAN_DETAILS_KEY);
    }

    public void setScanContent(String description){
        put(SCAN_CONTENT_KEY, description);
    }

    public String getScanContent(){
        return getString(SCAN_CONTENT_KEY);
    }

    public void setPhoto(final ParseFile imageFile){
        put(PHOTO_KEY, imageFile);
    }

    public ParseFile getPhoto(){
        return getParseFile(PHOTO_KEY);
    }
}
