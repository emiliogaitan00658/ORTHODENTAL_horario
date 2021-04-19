package com.example.orthodentalsahorario;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static String var_user;
    public static String var_pass;
    EditText user;
    EditText pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar bar = getSupportActionBar();
        if (getSupportActionBar() == null) {

        } else {
            getSupportActionBar().hide();
        }

        checkCameraPermission();
        // permisos de archivos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Check permissions for Android 6.0+
            if(!checkExternalStoragePermission()){
                return;
            }
        }

        String[] archivos = fileList();
        if (existe(archivos, "datos.dll")) {
            try {
                InputStreamReader archivo = new InputStreamReader(
                        openFileInput("datos.dll"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                String todo = "";
                while (linea != null) {
                    todo = todo + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
                if (todo.equals("")) {

                } else {
                    MainActivity.this.finish();
                    Intent intent = new Intent(MainActivity.this, principal.class);
                    startActivity(intent);
                }
            } catch (IOException e) {

            }
        }

    }

    private boolean existe(String[] archivos, String archbusca) {
        for (int f = 0; f < archivos.length; f++) {
            if (archbusca.equals(archivos[f])) {
                return true;
            }
        }
        return false;
    }

    public void grabar(String v) {
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(
                    "datos.dll", MainActivity.MODE_PRIVATE));
            archivo.write(v);
            archivo.flush();
            archivo.close();
        } catch (IOException e) {
        }
    }

    public void ingresar(View view) {
        user = findViewById(R.id.editTextuser);
        String valor_uno = user.getText().toString();

        pass = findViewById(R.id.editTextTextPassword);
        String valor_dos = pass.getText().toString();

        if (valor_uno.equals("")) {
            user.setError("Ingresar Usuario");
        } else if (valor_dos.equals("")) {
            pass.setError("Ingresar Contraseña");
        } else {
            var_user = valor_uno;
            var_pass = valor_dos;
            ejecutarServices("https://www.orthodentalnic.com/accesohorarios/seguridad/usuario.php");
        }
    }


    private void ejecutarServices(String url) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response.equals("no")) {
                        Toast.makeText(getApplicationContext(), "Usuario y Contraseña? No valido", Toast.LENGTH_SHORT).show();
                    } else {
                        String someValue = response;
                        Log.d("ooo :", String.valueOf(response));
                        while (((String) someValue).contains(".")) {
                            someValue = someValue.replace(".", "");
                        }

                        int i=Integer.parseInt(response.replaceAll("[\\D]", ""));
                        Toast.makeText(getApplicationContext(), String.valueOf(i), Toast.LENGTH_SHORT).show();
                        Log.d("newValue :", String.valueOf(i));
                        grabar(String.valueOf(i));
                        finish();
                        Intent intent = new Intent(MainActivity.this, principal.class);
                        startActivity(intent);
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Usuario y Contraseña? No valido", Toast.LENGTH_SHORT).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "No Connection/Communication Error!", Toast.LENGTH_SHORT).show();

                } else if (volleyError instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Authentication/ Auth Error!", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametross = new HashMap<String, String>();
                parametross.put("user", var_user.toString());
                parametross.put("pass", var_pass.toString());
                return parametross;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public static int countEmptyLines(String str) {
        final BufferedReader br = new BufferedReader(new StringReader(str));
        String line;
        int empty = 0;
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    ++empty;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return empty;
    }
    private boolean checkExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
        } else {
            return true;
        }

        return false;
    }

    private void checkCameraPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para la camara!.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 225);
        } else {
            Log.i("Mensaje", "Tienes permiso para usar la camara.");
        }
    }
}

