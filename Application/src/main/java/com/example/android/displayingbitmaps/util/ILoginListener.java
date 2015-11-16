package com.example.android.displayingbitmaps.util;

public interface ILoginListener {
    public void onLoginStarted();

    public void onLoginFinished();

    public void onLoginError(String errorMessage);

    public void onLogoutFinished();
}