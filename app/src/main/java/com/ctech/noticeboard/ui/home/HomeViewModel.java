package com.ctech.noticeboard.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private String title, genre, date, description, imageUrl;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public HomeViewModel(String text){
        mText = new MutableLiveData<>();
        mText.setValue(text);
    }

    public HomeViewModel(String title, String genre, String date){
        this.title = title;
        this.genre = genre;
        this.date = date;
    }

    public HomeViewModel (String title, String genre, String date, String description, String imageUrl){
        this.title = title;
        this.genre = genre;
        this.date = date;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public HomeViewModel(String text, String title, String genre, String date, String description, String imageUrl){
        mText = new MutableLiveData<>();
        mText.setValue(text);
        this.title = title;
        this.genre = genre;
        this.date = date;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void setmText(String text){
        mText = new MutableLiveData<>();
        mText.setValue(text);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public void setGenre(String genre){
        this.genre = genre;
    }

    public String getGenre(){
        return genre;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getDate(){
        return date;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription (){
        return description;
    }

    public void setImageUrl (String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getImageUrl () {
        return imageUrl;
    }
}