package com.example.digitalcare.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Inorder to use this application. You again sign into your kid's device and allow the requested permission.\n The requested permission will be   \n1) permission to send SMS  \n2) permission to current location  \n3) permission to run the app in background \nThen enter your kids name, choose a DP to show in the maps and then enter the kids allowed locations Once these settings is done then your kids device will be locked inorder to unlock/remove/sign out from your kid's device you need to enter your user password");
    }

    public LiveData<String> getText() {
        return mText;
    }

}