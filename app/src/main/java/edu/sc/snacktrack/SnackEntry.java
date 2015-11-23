package edu.sc.snacktrack;

import com.parse.ParseFile;

import java.util.Date;

/**
 * Created by dowdw on 11/9/2015.
 */
public class SnackEntry {
    private String name;
    private String photo;
    private Date date;

//    public SnackEntry(String name, String photo){// ,Date date){
//        super();
//        this.name = name;
//        this.photo = photo;
//        //this.date = date;
//    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto(){
        return  photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
