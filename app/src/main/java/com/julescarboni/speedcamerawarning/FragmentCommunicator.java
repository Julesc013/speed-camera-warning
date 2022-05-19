package com.julescarboni.speedcamerawarning;

import android.content.Context;
import android.content.Intent;

public interface FragmentCommunicator{
    public void passContextToFragment(Context context);
    public void passIntentToFragment(Intent intent);
}
