package com.codebase.quicklocation.presenter;

/**
 * Created by AUrriola on 6/19/17.
 */

public interface LoginViewPresenter {
    void onCreateP();
    void validateCamp(String username,String password);
    void authentication(String username, String password);
    void recoveypassword(String emailUser);
    void onDestroyP();
    void onClearText();
    void diagloAlert(String msgDialog);
}
