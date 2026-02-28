package com.inventorynewton;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class OptionsFragment extends Fragment {

    public static final String SHARED_PREFS = "sharedPrefs";
    Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);

        Button logoutButton = rootView.findViewById(R.id.button2);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });

        return rootView;
    }

    private void navigateToLogin() {
        // Use the context of the activity to access shared preferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", "");
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Toast.makeText(requireActivity(), "You've been signed out!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void clearUserData() {
        // Use the context of the activity to access shared preferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_data", MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    public void logout(View view) {
        clearUserData();
        navigateToLogin();
    }
}
